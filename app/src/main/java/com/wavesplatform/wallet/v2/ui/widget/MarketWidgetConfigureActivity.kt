/*
 * Created by Eduard Zaydel on 18/7/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback
import com.chad.library.adapter.base.listener.OnItemDragListener
import com.ethanhua.skeleton.RecyclerViewSkeletonScreen
import com.ethanhua.skeleton.Skeleton
import com.wavesplatform.sdk.model.response.data.AssetInfoResponse
import com.wavesplatform.sdk.model.response.data.SearchPairResponse
import com.wavesplatform.sdk.utils.WavesConstants
import com.wavesplatform.sdk.utils.isWavesId
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.manager.DataServiceManager
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.custom.FadeInWithoutDelayAnimator
import com.wavesplatform.wallet.v2.ui.widget.adapters.TokenAdapter
import com.wavesplatform.wallet.v2.ui.widget.model.MarketWidgetActiveAsset
import com.wavesplatform.wallet.v2.ui.widget.model.MarketWidgetActiveAssetPrefStore
import com.wavesplatform.wallet.v2.ui.widget.model.MarketWidgetStyle
import com.wavesplatform.wallet.v2.ui.widget.model.MarketWidgetUpdateInterval
import com.wavesplatform.wallet.v2.util.showError
import kotlinx.android.synthetic.main.content_empty_data.view.*
import kotlinx.android.synthetic.main.market_widget_configure.*
import pers.victor.ext.click
import pers.victor.ext.inflate
import javax.inject.Inject


/**
 * The configuration screen for the [MarketWidget] AppWidget.
 */
class MarketWidgetConfigureActivity : BaseActivity(), TabLayout.OnTabSelectedListener,
        MarketWidgetConfigureView {

    @Inject
    @InjectPresenter
    lateinit var presenter: MarketWidgetConfigurePresenter
    @Inject
    lateinit var adapter: TokenAdapter
    private var skeletonScreen: RecyclerViewSkeletonScreen? = null

    @ProvidePresenter
    fun providePresenter(): MarketWidgetConfigurePresenter = presenter

    private val widgetId: Int by lazy {
        intent.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                ?: AppWidgetManager.INVALID_APPWIDGET_ID
    }

    override fun configLayoutRes(): Int = R.layout.market_widget_configure

    override fun askPassCode() = false

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
        }


        tokensList.layoutManager = LinearLayoutManager(this)
        tokensList.itemAnimator = FadeInWithoutDelayAnimator()
        adapter.bindToRecyclerView(tokensList)
        adapter.onItemChildClickListener =
                BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
                    when (view.id) {
                        R.id.image_delete -> {
                            if (adapter.data.size > 1) {
                                adapter.remove(position)
                                updateWidgetAssetPairs()
                                checkCanAddPair()
                            } else if (adapter.data.size == 1) {
                                adapter.notifyItemChanged(0)
                            }
                        }
                    }
                }

        val itemDragAndSwipeCallback = ItemDragAndSwipeCallback(adapter)
        val itemTouchHelper = ItemTouchHelper(itemDragAndSwipeCallback)
        itemTouchHelper.attachToRecyclerView(tokensList)

        adapter.enableDragItem(itemTouchHelper, R.id.image_drag, false)
        adapter.setOnItemDragListener(object : OnItemDragListener {
            override fun onItemDragStart(viewHolder: RecyclerView.ViewHolder, pos: Int) {
                // do nothing
            }

            override fun onItemDragMoving(source: RecyclerView.ViewHolder, from: Int,
                                          target: RecyclerView.ViewHolder, to: Int) {
                // do nothing
            }

            override fun onItemDragEnd(viewHolder: RecyclerView.ViewHolder, pos: Int) {
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

        presenter.loadAssets(this, widgetId)
    }

    private fun saveAppWidget() {
        MarketWidgetActiveAssetPrefStore.saveAll(this, widgetId, presenter.widgetAssetPairs)
        // It is the responsibility of the configuration activity to update the app widget
        val appWidgetManager = AppWidgetManager.getInstance(this)
        MarketWidget.updateWidget(this, appWidgetManager, widgetId)

        // Make sure we pass back the original appWidgetId
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }

    override fun onBackPressed() {
        saveAppWidget()
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        onTabSelected(tab)
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        // do nothing
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        when (tab?.position) {
            INTERVAL_TAB -> showIntervalDialog()
            ADD_TAB -> showAssetsDialog()
            THEME_TAB -> showThemeDialog()
        }
    }

    override fun onUpdatePairs(assetPairList: ArrayList<TokenAdapter.TokenPair>) {
        adapter.setNewData(assetPairList)
        checkCanAddPair()
        adapter.emptyView = getEmptyView()
        skeletonScreen?.hide()
        updateWidgetAssetPairs()
    }

    override fun onUpdatePair(assetInfo: AssetInfoResponse, searchPairResponse: SearchPairResponse) {
        if (searchPairResponse.data.isEmpty()) {
            skeletonScreen?.hide()
            showError(R.string.market_widget_config_cant_find_currency_pair, R.id.root)
        }

        val data = adapter.data
        val mostValuablePair = searchPairResponse.data[0]
        data.add(TokenAdapter.TokenPair(assetInfo, mostValuablePair))
        adapter.setNewData(data)
        checkCanAddPair()
        adapter.emptyView = getEmptyView()
        skeletonScreen?.hide()
        updateWidgetAssetPairs()
    }

    override fun onFailGetMarkets() {
        afterFailGetMarkets()
        skeletonScreen?.hide()
    }

    private fun updateWidgetAssetPairs() {
        presenter.widgetAssetPairs.clear()
        adapter.data.forEach {
            presenter.widgetAssetPairs.add(MarketWidgetActiveAsset(
                    it.assetInfo.name,
                    it.pair.amountAsset ?: "",
                    it.pair.amountAsset ?: "",
                    it.pair.priceAsset ?: ""))
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


    private fun getEmptyView(): View {
        val view = inflate(R.layout.content_address_book_empty_state)
        view.text_empty.text = getString(R.string.dex_market_empty)
        return view
    }

    private fun afterFailGetMarkets() {
        skeletonScreen?.hide()
        showError(R.string.common_server_error, R.id.root)
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
        val position = when (presenter.intervalUpdate) {
            MarketWidgetUpdateInterval.MIN_1 -> 0
            MarketWidgetUpdateInterval.MIN_5 -> 1
            MarketWidgetUpdateInterval.MIN_10 -> 2
            MarketWidgetUpdateInterval.MANUALLY -> 3
        }

        val optionDialog = OptionBottomSheetFragment.newInstance(
                arrayListOf(
                        getString(R.string.market_widget_config_interval_1_min),
                        getString(R.string.market_widget_config_interval_5_min),
                        getString(R.string.market_widget_config_interval_10_min),
                        getString(R.string.market_widget_config_interval_manually)),
                getString(R.string.market_widget_config_update_interval),
                position
        )
        optionDialog.onChangeListener = object : OptionBottomSheetFragment.OnChangeListener {
            override fun onChange(optionPosition: Int) {
                presenter.intervalUpdate = when (optionPosition) {
                    0 -> MarketWidgetUpdateInterval.MIN_1
                    1 -> MarketWidgetUpdateInterval.MIN_5
                    2 -> MarketWidgetUpdateInterval.MIN_10
                    3 -> MarketWidgetUpdateInterval.MANUALLY
                    else -> MarketWidgetUpdateInterval.MIN_10
                }
                MarketWidgetUpdateInterval.setInterval(
                        this@MarketWidgetConfigureActivity, widgetId, presenter.intervalUpdate)
            }
        }
        val ft = supportFragmentManager.beginTransaction()
        ft.add(optionDialog, optionDialog::class.java.simpleName)
        ft.commitAllowingStateLoss()
    }

    private fun showThemeDialog() {
        val position = when (presenter.themeName) {
            MarketWidgetStyle.CLASSIC -> 0
            MarketWidgetStyle.DARK -> 1
        }

        val optionDialog = OptionBottomSheetFragment.newInstance(
                arrayListOf(
                        getString(R.string.market_widget_config_classic),
                        getString(R.string.market_widget_config_dark)),
                getString(R.string.market_widget_config_widget_style),
                position
        )
        optionDialog.onChangeListener = object : OptionBottomSheetFragment.OnChangeListener {
            override fun onChange(optionPosition: Int) {
                presenter.themeName = when (optionPosition) {
                    0 -> MarketWidgetStyle.CLASSIC
                    1 -> MarketWidgetStyle.DARK
                    else -> MarketWidgetStyle.CLASSIC
                }
                MarketWidgetStyle.setTheme(
                        this@MarketWidgetConfigureActivity, widgetId, presenter.themeName)
            }
        }
        val ft = supportFragmentManager.beginTransaction()
        ft.add(optionDialog, optionDialog::class.java.simpleName)
        ft.commitAllowingStateLoss()
    }

    private fun showAssetsDialog() {
        if (!presenter.canAddPair) {
            return
        }

        val assetsDialog = AssetsBottomSheetFragment.newInstance(presenter.assets)
        val ft = supportFragmentManager.beginTransaction()
        ft.add(assetsDialog, assetsDialog::class.java.simpleName)
        ft.commitAllowingStateLoss()
        assetsDialog.onChooseListener = object : AssetsBottomSheetFragment.OnChooseListener {
            override fun onChoose(asset: AssetInfoResponse) {
                presenter.assets.add(asset.id)
                val token = adapter.data.firstOrNull { it.assetInfo.id == asset.id }
                if (token == null) {
                    skeletonScreen?.show()
                    setSkeletonGradient()
                    presenter.loadPair(asset)
                } else {
                    showError(R.string.market_widget_config_error_add_asset, R.id.root)
                }
            }
        }
    }

    private fun setSkeletonGradient() {
        tokensList?.post {
            tokensList?.layoutManager?.findViewByPosition(1)?.alpha = 0.7f
            tokensList?.layoutManager?.findViewByPosition(2)?.alpha = 0.5f
            tokensList?.layoutManager?.findViewByPosition(3)?.alpha = 0.4f
            tokensList?.layoutManager?.findViewByPosition(4)?.alpha = 0.2f
        }
    }

    companion object {
        const val INTERVAL_TAB = 0
        const val ADD_TAB = 1
        const val THEME_TAB = 2
    }
}

