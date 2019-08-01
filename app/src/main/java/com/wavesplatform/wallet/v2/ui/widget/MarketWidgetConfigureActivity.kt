/*
 * Created by Eduard Zaydel on 18/7/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback
import com.chad.library.adapter.base.listener.OnItemDragListener
import com.ethanhua.skeleton.RecyclerViewSkeletonScreen
import com.ethanhua.skeleton.Skeleton
import com.wavesplatform.sdk.model.response.data.AssetInfoResponse
import com.wavesplatform.sdk.model.response.data.SearchPairResponse
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.manager.DataServiceManager
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.custom.FadeInWithoutDelayAnimator
import com.wavesplatform.wallet.v2.ui.widget.adapters.TokenAdapter
import com.wavesplatform.wallet.v2.ui.widget.model.MarketWidgetActiveAsset
import com.wavesplatform.wallet.v2.ui.widget.model.MarketWidgetStyle
import com.wavesplatform.wallet.v2.ui.widget.model.MarketWidgetUpdateInterval
import com.wavesplatform.wallet.v2.util.showError
import io.reactivex.Observable
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

        adapter.onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.asset_delete -> {
                    adapter.remove(position)
                    /*presenter.needToUpdate = true

                    val item = this.adapter.getItem(position)
                    item.notNull { item ->
                        MarketResponseDb(item).delete { equalTo("id", item.id) }

                        // remove from current list
                        this.adapter.data.removeAt(position)
                        this.adapter.notifyItemRemoved(position)
                    }*/
                }
            }
        }

        // configure drag and drop
        val itemDragAndSwipeCallback = ItemDragAndSwipeCallback(adapter)
        val itemTouchHelper = ItemTouchHelper(itemDragAndSwipeCallback)
        itemTouchHelper.attachToRecyclerView(tokensList)

        // allow drag and manage background of view
        adapter.enableDragItem(itemTouchHelper, R.id.image_drag, false)
        adapter.setOnItemDragListener(object : OnItemDragListener {
            override fun onItemDragStart(viewHolder: RecyclerView.ViewHolder, pos: Int) {
                /*if (view_drag_bg.height == 0) {
                    view_drag_bg.setHeight(viewHolder.itemView.card_market.height - dp2px(1))
                    view_drag_bg.setWidth(viewHolder.itemView.card_market.width - dp2px(1))
                }

                val originalPos = IntArray(2)
                viewHolder.itemView.card_market.getLocationOnScreen(originalPos)
                view_drag_bg.y = originalPos[1].toFloat() - getStatusBarHeight()
                view_drag_bg.visiable()

                viewHolder.itemView.card_market.cardElevation = dp2px(4).toFloat()*/
            }

            override fun onItemDragMoving(source: RecyclerView.ViewHolder, from: Int, target: RecyclerView.ViewHolder, to: Int) {
                /*presenter.needToUpdate = true

                val originalPos = IntArray(2)
                target.itemView.card_market.getLocationOnScreen(originalPos)
                view_drag_bg.y = originalPos[1].toFloat() - getStatusBarHeight()*/
            }

            override fun onItemDragEnd(viewHolder: RecyclerView.ViewHolder, pos: Int) {
                /*viewHolder.itemView.card_market.cardElevation = dp2px(2).toFloat()
                view_drag_bg.gone()*/
            }
        })

        skeletonScreen = Skeleton.bind(tokensList)
                .adapter(adapter)
                .shimmer(true)
                .count(5)
                .color(R.color.basic50)
                .load(R.layout.item_skeleton_assets)
                .frozen(false)
                .show()

        loadAssets(Constants.defaultGateways().toList())

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

    private fun loadAssets(assets: List<String>) {
        skeletonScreen?.show()
        val pairList = arrayListOf<SearchPairResponse.Pair>()
        eventSubscriptions.add(dataServiceManager.assets(ids = assets)
                .flatMap {
                    adapter.allData = it
                    Observable.fromIterable(assets)
                }
                .flatMap { dataServiceManager.loadPairs(searchByAsset = it) }
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ result ->
                    pairList.add(result.data[0])
                }, {
                    afterFailGetMarkets()
                    it.printStackTrace()
                    skeletonScreen?.hide()
                }, {
                    adapter.setNewData(pairList)
                    adapter.emptyView = getEmptyView()
                    skeletonScreen?.hide()

                    pairList.forEach {
                        val assetInfo = adapter.allData.firstOrNull()
                        // todo something
                        MarketWidgetActiveAsset(
                                assetInfo?.name ?: "-",
                                widgetId.toString(),
                                it.amountAsset ?: "",
                                it.priceAsset ?: "")
                    }



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
        val assetsDialog = AssetsBottomSheetFragment.newInstance(assets)
        val ft = supportFragmentManager.beginTransaction()
        ft.add(assetsDialog, assetsDialog::class.java.simpleName)
        ft.commitAllowingStateLoss()
        assetsDialog.onChooseListener = object : AssetsBottomSheetFragment.OnChooseListener {
            override fun onChoose(assets: ArrayList<String>) {
                this@MarketWidgetConfigureActivity.assets = assets
                loadAssets(assets)
            }
        }
    }

    companion object {
        const val INTERVAL_TAB = 0
        const val ADD_TAB = 1
        const val THEME_TAB = 2
    }
}

