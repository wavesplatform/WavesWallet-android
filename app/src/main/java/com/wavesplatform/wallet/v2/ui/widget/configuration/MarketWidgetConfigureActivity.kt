/*
 * Created by Eduard Zaydel on 8/8/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget.configuration

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import androidx.annotation.StringRes
import com.google.android.material.tabs.TabLayout
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback
import com.chad.library.adapter.base.listener.OnItemDragListener
import com.ethanhua.skeleton.RecyclerViewSkeletonScreen
import com.ethanhua.skeleton.Skeleton
import com.wavesplatform.sdk.model.response.data.AssetInfoResponse
import com.wavesplatform.sdk.utils.isWaves
import com.wavesplatform.sdk.utils.notNull
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.analytics.AnalyticEvents
import com.wavesplatform.wallet.v2.data.analytics.analytics
import com.wavesplatform.wallet.v2.data.manager.widget.MarketWidgetActiveAssetStore
import com.wavesplatform.wallet.v2.data.model.local.widget.MarketWidgetActiveAsset
import com.wavesplatform.wallet.v2.data.model.local.widget.MarketWidgetSettings
import com.wavesplatform.wallet.v2.data.model.local.widget.MarketWidgetStyle
import com.wavesplatform.wallet.v2.data.model.local.widget.MarketWidgetUpdateInterval
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.custom.FadeInWithoutDelayAnimator
import com.wavesplatform.wallet.v2.ui.widget.MarketPulseAppWidgetProvider
import com.wavesplatform.wallet.v2.ui.widget.configuration.assets.MarketWidgetConfigurationAssetsBottomSheetFragment
import com.wavesplatform.wallet.v2.ui.widget.configuration.options.OptionsBottomSheetFragment
import com.wavesplatform.wallet.v2.util.EnvironmentManager
import com.wavesplatform.wallet.v2.util.isFiat
import com.wavesplatform.wallet.v2.util.showError
import kotlinx.android.synthetic.main.content_empty_data.view.*
import kotlinx.android.synthetic.main.market_widget_configure.*
import pers.victor.ext.click
import pers.victor.ext.inflate
import pers.victor.ext.isNetworkConnected
import javax.inject.Inject


/**
 * The configuration screen for the [MarketPulseAppWidgetProvider] AppWidget.
 */
class MarketWidgetConfigureActivity : BaseActivity(), TabLayout.OnTabSelectedListener,
        MarketWidgetConfigureView {

    @Inject
    @InjectPresenter
    lateinit var presenter: MarketWidgetConfigurePresenter
    @Inject
    lateinit var adapter: MarketWidgetConfigurationMarketsAdapter
    private var skeletonScreen: RecyclerViewSkeletonScreen? = null

    @ProvidePresenter
    fun providePresenter(): MarketWidgetConfigurePresenter = presenter

    private val widgetId: Int by lazy {
        intent.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                ?: AppWidgetManager.INVALID_APPWIDGET_ID
    }

    private val onUpdateCompleteListener =
            object : EnvironmentManager.Companion.OnUpdateCompleteListener {

                override fun onComplete() {
                    loadAssetsPairs()
                }

                override fun onError() {
                    showNetworkError()
                }
            }

    override fun configLayoutRes(): Int = R.layout.market_widget_configure

    override fun askPassCode() = false

    override fun needToShowNetworkMessage() = true

    override fun onNetworkConnectionChanged(networkConnected: Boolean) {
        super.onNetworkConnectionChanged(networkConnected)
        loadAssetsPairs()
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)

        checkWidgetId()

        tab_navigation.addTab(tab_navigation.newTab().setCustomView(
                getCustomView(R.drawable.ic_widget_interval_22,
                        R.string.market_widget_config_interval)).setTag("set_interval"))
        tab_navigation.addTab(tab_navigation.newTab().setCustomView(
                getCustomView(R.drawable.ic_widget_addtoken_22,
                        R.string.market_widget_config_add_token)).setTag("add_token"))
        tab_navigation.addTab(tab_navigation.newTab().setCustomView(
                getCustomView(R.drawable.ic_widget_style_22,
                        R.string.market_widget_config_style)).setTag("set_style"))

        tab_navigation.addOnTabSelectedListener(this)

        toolbar_close.click {
            saveAppWidget()
            finish()
        }


        tokensList.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        tokensList.itemAnimator = FadeInWithoutDelayAnimator()
        adapter.bindToRecyclerView(tokensList)
        adapter.onItemChildClickListener =
                BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
                    when (view.id) {
                        R.id.image_delete -> {
                            if (adapter.data.size > 1) {
                                this.adapter.getItem(position).notNull {
                                    presenter.assets.remove(it.assetInfo.id)
                                }
                                adapter.remove(position)
                                updateWidgetAssetPairs()
                                checkCanAddPair()
                                if (adapter.data.size == 1) {
                                    adapter.notifyItemChanged(0)
                                }
                            }
                        }
                    }
                }

        val itemDragAndSwipeCallback = ItemDragAndSwipeCallback(adapter)
        val itemTouchHelper = ItemTouchHelper(itemDragAndSwipeCallback)
        itemTouchHelper.attachToRecyclerView(tokensList)

        adapter.enableDragItem(itemTouchHelper, R.id.image_drag, false)
        adapter.setOnItemDragListener(object : OnItemDragListener {
            override fun onItemDragStart(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, pos: Int) {
                // do nothing
            }

            override fun onItemDragMoving(source: androidx.recyclerview.widget.RecyclerView.ViewHolder, from: Int,
                                          target: androidx.recyclerview.widget.RecyclerView.ViewHolder, to: Int) {
                // do nothing
            }

            override fun onItemDragEnd(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, pos: Int) {
                updateWidgetAssetPairs()
            }
        })

        skeletonScreen = Skeleton.bind(tokensList)
                .adapter(adapter)
                .shimmer(true)
                .count(5)
                .color(R.color.basic50)
                .load(R.layout.item_skeleton_widget_drag_assets)
                .frozen(false)
                .show()
        setSkeletonGradient()

        if (intent.hasExtra(EXTRA_APPWIDGET_CHANGE)) {
            analytics.trackEvent(AnalyticEvents.MarketPulseActiveEvent)
            presenter.themeName = MarketWidgetStyle.getTheme(this, widgetId)
            presenter.intervalUpdate = MarketWidgetUpdateInterval.getInterval(this, widgetId)
        }

        setTabText(INTERVAL_TAB, presenter.intervalUpdate.itemTitle())
        setTabText(THEME_TAB, presenter.themeName.itemTitle())
    }

    override fun onBackPressed() {
        if (!intent.hasExtra(EXTRA_APPWIDGET_CHANGE)) {
            MarketWidgetActiveAssetStore.clear(this, widgetId)
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }

    override fun onPause() {
        super.onPause()
        EnvironmentManager.removeOnUpdateCompleteListener(onUpdateCompleteListener)
        MarketPulseAppWidgetProvider.updateWidgetByBroadcast(this, widgetId)
    }

    private fun saveAppWidget() {
        if (!isNetworkConnected() || presenter.assets.size == 0 || adapter.data.size == 0) {
            return
        }

        if (intent.hasExtra(EXTRA_APPWIDGET_CHANGE)) {
            analytics.trackEvent(AnalyticEvents.MarketPulseSettingsChangedEvent(
                    presenter.themeName.toString().toLowerCase().capitalize(),
                    "${presenter.intervalUpdate.interval}m",
                    presenter.assets))
        }

        MarketWidgetActiveAssetStore.saveAll(this, widgetId, presenter.widgetAssetPairs)

        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        setResult(Activity.RESULT_OK, resultValue)
    }

    private fun loadAssetsPairs() {
        if (isNetworkConnected()) {
            if (EnvironmentManager.isUpdateCompleted()) {
                if (adapter.data.isEmpty()) {
                    presenter.loadAssetsPairs(this, widgetId)
                }
            } else {
                EnvironmentManager.update()
                EnvironmentManager.addOnUpdateCompleteListener(onUpdateCompleteListener)
            }
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        onTabSelected(tab)
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        // do nothing
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        if (!isNetworkConnected()) {
            return
        }

        when (tab?.position) {
            INTERVAL_TAB -> showIntervalDialog()
            ADD_TAB -> showAssetsDialog()
            THEME_TAB -> showThemeDialog()
        }
    }

    override fun onAddPairs(assetPairList: List<MarketWidgetConfigurationMarketsAdapter.TokenPair>) {
        if (assetPairList.isEmpty()) {
            skeletonScreen?.hide()
            showError(R.string.market_widget_config_cant_find_currency_pair, R.id.errorSnackBarRoot)
            return
        }

        val data = adapter.data
        data.addAll(assetPairList)
        onUpdatePairs(data)
    }

    override fun onUpdatePairs(assetPairList: List<MarketWidgetConfigurationMarketsAdapter.TokenPair>) {
        adapter.setNewData(assetPairList)
        checkCanAddPair()
        adapter.emptyView = getEmptyView()
        skeletonScreen?.hide()
        updateWidgetAssetPairs()
        setSkeletonGradient(false)
    }

    override fun onFailGetMarkets() {
        afterFailGetMarkets()
        skeletonScreen?.hide()
        setSkeletonGradient(false)
    }

    private fun updateWidgetAssetPairs() {
        presenter.widgetAssetPairs.clear()

        adapter.data.forEach {

            val id = when {
                it.pair.priceAsset?.isWaves() == true ->
                    it.pair.amountAsset
                isFiat(it.pair.priceAsset ?: "") ->
                    it.pair.amountAsset
                else ->
                    it.pair.priceAsset
            }

            presenter.widgetAssetPairs.add(MarketWidgetActiveAsset(
                    it.assetInfo.name,
                    id ?: "",
                    it.pair.amountAsset ?: "",
                    it.pair.priceAsset ?: ""))

            saveAppWidget()
        }
    }

    private fun checkCanAddPair() {
        presenter.canAddPair = adapter.data.size < 10

        val count = SpannableStringBuilder("${adapter.data.size} / 10")
        count.setSpan(StyleSpan(Typeface.BOLD), 0, count.length - 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tokensCounter.text = count

        val addTab = tab_navigation.getTabAt(ADD_TAB)
        val imageTab = addTab?.customView?.findViewById<ImageView>(R.id.image_tab)
        val textTab = addTab?.customView?.findViewById<TextView>(R.id.text_tab)

        if (presenter.canAddPair) {
            textTab?.setTextColor(ContextCompat.getColor(baseContext, R.color.black))
            imageTab?.setImageDrawable(
                    ContextCompat.getDrawable(baseContext, R.drawable.ic_widget_addtoken_22))
        } else {
            textTab?.setTextColor(ContextCompat.getColor(baseContext, R.color.basic500))
            imageTab?.setImageDrawable(
                    ContextCompat.getDrawable(baseContext, R.drawable.ic_widget_maxtoken_22))
        }
    }

    private fun setTabText(tabPosition: Int, @StringRes textRes: Int) {
        val tab = tab_navigation.getTabAt(tabPosition)
        val textTab = tab?.customView?.findViewById<TextView>(R.id.text_tab)
        textTab?.text = getString(textRes)
    }


    private fun getEmptyView(): View {
        val view = inflate(R.layout.content_address_book_empty_state)
        view.text_empty.text = getString(R.string.dex_market_empty)
        return view
    }

    private fun afterFailGetMarkets() {
        skeletonScreen?.hide()
        showError(R.string.common_server_error, R.id.errorSnackBarRoot)
    }

    private fun getCustomView(tabIcon: Int, tabText: Int): View? {
        val customTab = LayoutInflater.from(this)
                .inflate(R.layout.content_widget_configure_navigation_tab, null)
        val imageTab = customTab.findViewById<ImageView>(R.id.image_tab)
        val textTab = customTab.findViewById<AppCompatTextView>(R.id.text_tab)

        imageTab.setImageResource(tabIcon)
        textTab.text = getString(tabText)

        return customTab
    }

    private fun checkWidgetId() {
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(Activity.RESULT_CANCELED)

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
    }

    private fun showIntervalDialog() {
        val optionBottomSheetFragment = OptionsBottomSheetFragment<MarketWidgetUpdateInterval>()

        val options = MarketWidgetUpdateInterval.values().toMutableList()
        val defaultPosition = options.indexOf(MarketWidgetSettings.intervalSettings().getInterval(this, widgetId))

        optionBottomSheetFragment.configureDialog(options,
                getString(R.string.market_widget_config_update_interval),
                defaultPosition)

        optionBottomSheetFragment.onOptionSelectedListener = object : OptionsBottomSheetFragment.OnSelectedListener<MarketWidgetUpdateInterval> {
            override fun onSelected(data: MarketWidgetUpdateInterval) {
                presenter.intervalUpdate = data
                MarketWidgetSettings
                        .intervalSettings()
                        .setInterval(this@MarketWidgetConfigureActivity, widgetId, presenter.intervalUpdate)
                setTabText(INTERVAL_TAB, presenter.intervalUpdate.itemTitle())
                saveAppWidget()
            }
        }

        optionBottomSheetFragment.show(supportFragmentManager, optionBottomSheetFragment::class.java.simpleName)
    }

    private fun showThemeDialog() {
        val optionBottomSheetFragment = OptionsBottomSheetFragment<MarketWidgetStyle>()

        val options = MarketWidgetStyle.values().toMutableList()
        val defaultPosition = options.indexOf(MarketWidgetSettings.themeSettings().getTheme(this, widgetId))

        optionBottomSheetFragment.configureDialog(options,
                getString(R.string.market_widget_config_widget_style),
                defaultPosition)

        optionBottomSheetFragment.onOptionSelectedListener = object : OptionsBottomSheetFragment.OnSelectedListener<MarketWidgetStyle> {
            override fun onSelected(data: MarketWidgetStyle) {
                presenter.themeName = data
                MarketWidgetSettings
                        .themeSettings()
                        .setTheme(this@MarketWidgetConfigureActivity, widgetId, presenter.themeName)
                setTabText(THEME_TAB, presenter.themeName.itemTitle())
                saveAppWidget()
            }
        }

        optionBottomSheetFragment.show(supportFragmentManager, optionBottomSheetFragment::class.java.simpleName)
    }

    private fun showAssetsDialog() {
        if (!presenter.canAddPair) {
            return
        }

        val assetsDialog = MarketWidgetConfigurationAssetsBottomSheetFragment.newInstance(presenter.assets)
        val ft = supportFragmentManager.beginTransaction()
        ft.add(assetsDialog, assetsDialog::class.java.simpleName)
        ft.commitAllowingStateLoss()
        assetsDialog.onChooseListener = object : MarketWidgetConfigurationAssetsBottomSheetFragment.OnChooseListener {
            override fun onChoose(asset: AssetInfoResponse) {
                presenter.assets.add(asset.id)
                val token = adapter.data.firstOrNull { it.assetInfo.id == asset.id }
                if (token == null) {
                    skeletonScreen?.show()
                    setSkeletonGradient()
                    presenter.loadAssetPair(asset)
                } else {
                    showError(R.string.market_widget_config_error_add_asset, R.id.errorSnackBarRoot)
                }
            }
        }
    }

    private fun setSkeletonGradient(enable: Boolean = true) {
        tokensList?.post {
            if (enable) {
                tokensList?.layoutManager?.findViewByPosition(1)?.alpha = 0.7f
                tokensList?.layoutManager?.findViewByPosition(2)?.alpha = 0.5f
                tokensList?.layoutManager?.findViewByPosition(3)?.alpha = 0.4f
                tokensList?.layoutManager?.findViewByPosition(4)?.alpha = 0.2f
            } else {
                tokensList?.layoutManager?.findViewByPosition(1)?.alpha = 1.0f
                tokensList?.layoutManager?.findViewByPosition(2)?.alpha = 1.0f
                tokensList?.layoutManager?.findViewByPosition(3)?.alpha = 1.0f
                tokensList?.layoutManager?.findViewByPosition(4)?.alpha = 1.0f
            }
        }
    }

    companion object {
        const val INTERVAL_TAB = 0
        const val ADD_TAB = 1
        const val THEME_TAB = 2
        const val EXTRA_APPWIDGET_CHANGE = "com.wavesplatform.wallet.EXTRA_APPWIDGET_CHANGE"
    }
}