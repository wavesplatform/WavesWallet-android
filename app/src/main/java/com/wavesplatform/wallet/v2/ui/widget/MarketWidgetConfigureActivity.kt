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
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback
import com.chad.library.adapter.base.listener.OnItemDragListener
import com.ethanhua.skeleton.RecyclerViewSkeletonScreen
import com.ethanhua.skeleton.Skeleton
import com.wavesplatform.sdk.model.request.data.PairRequest
import com.wavesplatform.sdk.model.response.data.AssetInfoResponse
import com.wavesplatform.sdk.model.response.data.SearchPairResponse
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.sdk.utils.WavesConstants
import com.wavesplatform.sdk.utils.isWavesId
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.manager.DataServiceManager
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.custom.FadeInWithoutDelayAnimator
import com.wavesplatform.wallet.v2.ui.widget.adapters.TokenAdapter
import com.wavesplatform.wallet.v2.ui.widget.model.MarketWidgetActiveAsset
import com.wavesplatform.wallet.v2.ui.widget.model.MarketWidgetStyle
import com.wavesplatform.wallet.v2.ui.widget.model.MarketWidgetUpdateInterval
import com.wavesplatform.wallet.v2.util.EnvironmentManager
import com.wavesplatform.wallet.v2.util.showError
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import kotlinx.android.synthetic.main.content_empty_data.view.*
import kotlinx.android.synthetic.main.market_widget_configure.*
import pers.victor.ext.click
import pers.victor.ext.inflate
import javax.inject.Inject


/**
 * The configuration screen for the [MarketWidget] AppWidget.
 */
class MarketWidgetConfigureActivity : BaseActivity(), TabLayout.OnTabSelectedListener {

    @Inject
    lateinit var dataServiceManager: DataServiceManager
    @Inject
    lateinit var adapter: TokenAdapter
    private var themeName = MarketWidgetStyle.CLASSIC
    private var intervalUpdate = MarketWidgetUpdateInterval.MIN_10
    private var assets = arrayListOf<String>()
    private var skeletonScreen: RecyclerViewSkeletonScreen? = null
    private var widgetAssetPairs = arrayListOf<MarketWidgetActiveAsset>()
    private var canAddPair = false

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
            // It is the responsibility of the configuration activity to update the app widget
            val appWidgetManager = AppWidgetManager.getInstance(this)
            MarketWidget.updateWidget(this, appWidgetManager, widgetId)

            // Make sure we pass back the original appWidgetId
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }


        tokensList.layoutManager = LinearLayoutManager(this)
        tokensList.itemAnimator = FadeInWithoutDelayAnimator()
        adapter.bindToRecyclerView(tokensList)

        adapter.onItemChildClickListener =
                BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
                    when (view.id) {
                        R.id.image_delete -> {
                            adapter.remove(position)
                            updateWidgetAssetPairs()
                            checkCanAddPair()
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

        Constants.defaultCrypto().toList().forEach {
            if (it.isWavesId()) {
                assets.add(WavesConstants.WAVES_ASSET_ID_FILLED)
            } else {
                assets.add(it)
            }
        }

        loadAssets(assets)
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

    private fun updateWidgetAssetPairs() {
        widgetAssetPairs.clear()
        adapter.data.forEach {
            // todo something
            widgetAssetPairs.add(MarketWidgetActiveAsset(
                    it.assetInfo.name,
                    widgetId.toString(),
                    it.pair.amountAsset ?: "",
                    it.pair.priceAsset ?: ""))
        }
    }

    private fun loadAssets(assets: List<String>) {

        fun getFilledPairs(pairs: SearchPairResponse, assets: MutableList<String>)
                : MutableList<SearchPairResponse.Pair> {
            val filledResult = mutableListOf<SearchPairResponse.Pair>()
            for (index in 0 until pairs.data.size) {
                if (pairs.data[index].data != null) {
                    val pair = assets[index].split("/")
                    pairs.data[index].amountAsset = pair[0]
                    pairs.data[index].priceAsset = pair[1]
                    filledResult.add(pairs.data[index])
                }
            }
            return filledResult
        }

        fun createPairs(assets: List<String>): MutableList<String> {
            val initPairsList = mutableListOf<String>()

            val usdAsset = EnvironmentManager.defaultAssets.firstOrNull {
                it.issueTransaction?.name == "US Dollar"
            }

            for (priceAssetId in assets) {
                if (priceAssetId.isWavesId()) {
                    initPairsList.add("${WavesConstants.WAVES_ASSET_ID_FILLED}/${usdAsset?.assetId}")
                    continue
                } else {
                    initPairsList.add("${WavesConstants.WAVES_ASSET_ID_FILLED}/$priceAssetId")
                    initPairsList.add("$priceAssetId/${WavesConstants.WAVES_ASSET_ID_FILLED}")
                }
            }
            return initPairsList
        }

        skeletonScreen?.show()
        setSkeletonGradient()
        val pairsList = createPairs(assets)
        eventSubscriptions.add(
                Observable.zip(dataServiceManager.assets(ids = assets),
                        dataServiceManager.loadPairs(PairRequest(pairs = pairsList, limit = 200))
                                .flatMap { pairs ->
                                    Observable.just(getFilledPairs(pairs, pairsList))
                                },
                        BiFunction { t1: List<AssetInfoResponse>, t2: List<SearchPairResponse.Pair> ->
                            return@BiFunction Pair(t1, t2)
                        })
                        .compose(RxUtil.applyObservableDefaultSchedulers())
                        .subscribe({ pair ->
                            val tokenPairList
                                    = arrayListOf<TokenAdapter.TokenPair>()
                            pair.second.forEach { assetPair ->
                                tokenPairList.add(TokenAdapter.TokenPair(
                                        pair.first.first { it.id == assetPair.amountAsset },
                                        assetPair))
                            }
                            adapter.setNewData(tokenPairList)
                            checkCanAddPair()
                            adapter.emptyView = getEmptyView()
                            skeletonScreen?.hide()
                            updateWidgetAssetPairs()
                        }, {
                            afterFailGetMarkets()
                            it.printStackTrace()
                            skeletonScreen?.hide()
                        })
        )
    }

    private fun checkCanAddPair() {
        canAddPair = adapter.data.size < 10

        val count = SpannableStringBuilder("${adapter.data.size}/10")
        count.setSpan(StyleSpan(Typeface.BOLD), 0, count.length - 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tokensCounter.text = count

        val addTab = tab_navigation.getTabAt(ADD_TAB)
        val imageTab = addTab?.customView?.findViewById<ImageView>(R.id.image_tab)
        val textTab = addTab?.customView?.findViewById<TextView>(R.id.text_tab)

        if (canAddPair) {
            textTab?.setTextColor(ContextCompat.getColor(baseContext, R.color.black))
            imageTab?.setImageDrawable(
                    ContextCompat.getDrawable(baseContext, R.drawable.ic_widget_addtoken_22))
        } else {
            textTab?.setTextColor(ContextCompat.getColor(baseContext, R.color.basic500))
            imageTab?.setImageDrawable(
                    ContextCompat.getDrawable(baseContext, R.drawable.ic_widget_maxtoken_22))
        }
    }

    private fun loadPair(assetInfo: AssetInfoResponse) {
        skeletonScreen?.show()
        setSkeletonGradient()
        eventSubscriptions.add(dataServiceManager.loadPairs(searchByAsset = assetInfo.id)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe(
                        { result ->
                            if (result.data.isEmpty()) {
                                skeletonScreen?.hide()
                                showError(R.string.market_widget_config_cant_find_currency_pair, R.id.root)
                                return@subscribe
                            }

                            val data = adapter.data
                            val mostValuablePair = result.data[0]
                            data.add(TokenAdapter.TokenPair(assetInfo, mostValuablePair))
                            adapter.setNewData(data)
                            checkCanAddPair()
                            adapter.emptyView = getEmptyView()
                            skeletonScreen?.hide()
                            updateWidgetAssetPairs()
                        },
                        {
                            afterFailGetMarkets()
                            it.printStackTrace()
                            skeletonScreen?.hide()
                        }))
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
        val position = when (intervalUpdate) {
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
                intervalUpdate = when (optionPosition) {
                    0 -> MarketWidgetUpdateInterval.MIN_1
                    1 -> MarketWidgetUpdateInterval.MIN_5
                    2 -> MarketWidgetUpdateInterval.MIN_10
                    3 -> MarketWidgetUpdateInterval.MANUALLY
                    else -> MarketWidgetUpdateInterval.MIN_10
                }
                MarketWidgetUpdateInterval.setInterval(
                        this@MarketWidgetConfigureActivity, widgetId, intervalUpdate)
            }
        }
        val ft = supportFragmentManager.beginTransaction()
        ft.add(optionDialog, optionDialog::class.java.simpleName)
        ft.commitAllowingStateLoss()
    }

    private fun showThemeDialog() {
        val position = when (themeName) {
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
                themeName = when (optionPosition) {
                    0 -> MarketWidgetStyle.CLASSIC
                    1 -> MarketWidgetStyle.DARK
                    else -> MarketWidgetStyle.CLASSIC
                }
                MarketWidgetStyle.setTheme(
                        this@MarketWidgetConfigureActivity, widgetId, themeName)
            }
        }
        val ft = supportFragmentManager.beginTransaction()
        ft.add(optionDialog, optionDialog::class.java.simpleName)
        ft.commitAllowingStateLoss()
    }

    private fun showAssetsDialog() {
        if (!canAddPair) {
            return
        }

        val assetsDialog = AssetsBottomSheetFragment.newInstance()
        val ft = supportFragmentManager.beginTransaction()
        ft.add(assetsDialog, assetsDialog::class.java.simpleName)
        ft.commitAllowingStateLoss()
        assetsDialog.onChooseListener = object : AssetsBottomSheetFragment.OnChooseListener {
            override fun onChoose(asset: AssetInfoResponse) {
                this@MarketWidgetConfigureActivity.assets.add(asset.id)
                val token = adapter.data.firstOrNull { it.assetInfo.id == asset.id }
                if (token == null) {
                    loadPair(asset)
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

