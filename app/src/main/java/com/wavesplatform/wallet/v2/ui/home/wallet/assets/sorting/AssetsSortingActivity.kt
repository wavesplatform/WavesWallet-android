/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.assets.sorting

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import android.widget.LinearLayout
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.flyco.tablayout.listener.CustomTabEntity
import com.flyco.tablayout.listener.OnTabSelectListener
import com.vicpin.krealmextensions.save
import com.wavesplatform.sdk.model.response.node.AssetBalanceResponse
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.db.AssetBalanceDb
import com.wavesplatform.wallet.v2.data.analytics.AnalyticEvents
import com.wavesplatform.wallet.v2.data.analytics.analytics
import com.wavesplatform.wallet.v2.data.model.local.AssetSortingItem
import com.wavesplatform.wallet.v2.data.model.db.userdb.AssetBalanceStoreDb
import com.wavesplatform.wallet.v2.data.model.local.TabItem
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.custom.FadeInWithoutDelayAnimator
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.AssetsFragment
import com.wavesplatform.wallet.v2.util.drag_helper.ItemDragListener
import com.wavesplatform.wallet.v2.util.drag_helper.SimpleItemTouchHelperCallback
import kotlinx.android.synthetic.main.activity_assets_sorting.*
import pers.victor.ext.children
import pers.victor.ext.get
import pyxis.uzuki.live.richutilskt.utils.runDelayed
import java.util.*
import javax.inject.Inject


class AssetsSortingActivity : BaseActivity(), AssetsSortingView {

    @Inject
    @InjectPresenter
    lateinit var presenter: AssetsSortingPresenter
    @Inject
    lateinit var adapter: AssetsSortingAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper
    private var itemAnimator = FadeInWithoutDelayAnimator()


    private val tabs: ArrayList<CustomTabEntity> by lazy {
        arrayListOf<CustomTabEntity>(
                TabItem(getString(R.string.wallet_sorting_toolbar_position_action),
                        R.drawable.ic_position_18_black,
                        R.drawable.ic_position_18_basic_500),
                TabItem(getString(
                        R.string.wallet_sorting_toolbar_visibility_action),
                        R.drawable.ic_visibility_18_black,
                        R.drawable.ic_visibility_18_basic_500))
    }

    private val tabSelectListener = object : OnTabSelectListener {
        override fun onTabSelect(position: Int) {
            setScreenType(position)
        }

        override fun onTabReselect(position: Int) {

        }
    }

    @ProvidePresenter
    fun providePresenter(): AssetsSortingPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_assets_sorting

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        setupToolbar(toolbar_view, true, getString(com.wavesplatform.wallet.R.string.wallet_sorting_toolbar_title), R.drawable.ic_toolbar_back_black)

        recycle_assets.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                appbar_layout.isSelected = recycle_assets.canScrollVertically(-1)
            }
        })

        common_tab_layout.setTabData(tabs)
        common_tab_layout.setOnTabSelectListener(tabSelectListener)

        recycle_assets.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        recycle_assets.itemAnimator = itemAnimator

        adapter.bindToRecyclerView(recycle_assets)
        adapter.onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.image_favorite -> {
                    presenter.needToUpdate = true

                    val globalItem = this.adapter.getItem(position) as AssetSortingItem
                    val asset = globalItem.asset

                    when (globalItem.itemType) {
                        AssetSortingItem.TYPE_FAVORITE_ITEM -> {
                            val linePosition = this.adapter.getLinePosition()

                            asset.isFavorite = false
                            asset.configureVisibleState = presenter.visibilityConfigurationActive

                            // remove from favorite list
                            this.adapter.data.removeAt(position)
                            this.adapter.notifyItemRemoved(position)

                            // add to not favorite list
                            addToListOf(AssetSortingItem.TYPE_DEFAULT_ITEM, globalItem, asset, linePosition)

                            AssetBalanceDb(asset).save()
                            AssetBalanceStoreDb(asset.assetId, asset.isHidden, asset.position, asset.isFavorite).save()
                        }
                        AssetSortingItem.TYPE_DEFAULT_ITEM, AssetSortingItem.TYPE_HIDDEN_ITEM -> {
                            // remove from current list
                            this.adapter.data.removeAt(position)
                            this.adapter.notifyItemRemoved(position)

                            val linePosition = this.adapter.getLinePosition()

                            asset.isFavorite = true
                            asset.isHidden = false

                            // add to favorite list
                            addToListOf(AssetSortingItem.TYPE_FAVORITE_ITEM, globalItem, asset, linePosition)

                            AssetBalanceDb(asset).save()
                            AssetBalanceStoreDb(asset.assetId, asset.isHidden, asset.position,
                                    asset.isFavorite).save()
                        }
                    }
                }
            }
        }

        adapter.onHiddenChangeListener = object : AssetsSortingAdapter.OnHiddenChangeListener {
            override fun onHiddenStateChanged(item: AssetSortingItem, checked: Boolean, position: Int) {
                if (position != POSITION_NONE) {

                    presenter.needToUpdate = true

                    // remove from current list
                    adapter.data.removeAt(position)
                    adapter.notifyItemRemoved(position)

                    val linePosition = adapter.getHiddenLinePosition()

                    item.asset.isFavorite = false
                    item.asset.isHidden = !checked

                    if (checked) {
                        item.asset.isHidden = false
                        item.type = AssetSortingItem.TYPE_DEFAULT_ITEM
                        adapter.addData(linePosition, item)
                        adapter.checkEmptyViews()
                    } else {
                        item.asset.isHidden = true
                        item.type = AssetSortingItem.TYPE_HIDDEN_ITEM
                        adapter.addData(linePosition + 1, item)
                        adapter.checkEmptyViews()
                    }

                    // Save to DB
                    AssetBalanceDb(item.asset).save()
                    AssetBalanceStoreDb(item.asset.assetId, item.asset.isHidden, item.asset.position,
                            item.asset.isFavorite).save()
                }
            }
        }

        // configure drag and drop
        val callback = SimpleItemTouchHelperCallback(adapter)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recycle_assets)

        adapter.dragStartListener = object : ItemDragListener {
            override fun onStartDrag(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
                setTabsEnable(false)
                itemTouchHelper.startDrag(viewHolder)
            }

            override fun onMoved(fromHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder?, fromPosition: Int, toHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder?, toPosition: Int) {
                presenter.needToUpdate = true
            }

            override fun onEndDrag(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder) {
                applyCorrectCardBg(viewHolder)
                setTabsEnable(true)
            }
        }

        presenter.loadAssets()

        common_tab_layout.currentTab = 0
    }

    private fun addToListOf(listType: Int, globalItem: AssetSortingItem, asset: AssetBalanceResponse, linePosition: Int) {
        globalItem.type = listType
        globalItem.asset = asset

        this.adapter.addData(linePosition, globalItem)
        this.adapter.checkEmptyViews()
    }

    private fun applyCorrectCardBg(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder) {
        if (holder.adapterPosition != POSITION_NONE) {
            val item = adapter.getItem(holder.adapterPosition) as AssetSortingItem
            val type = adapter.resolveType(holder.adapterPosition)

            item.type = type

            when (type) {
                AssetSortingItem.TYPE_FAVORITE_ITEM -> {
                    item.asset.isFavorite = true
                    item.asset.isHidden = false
                }
                AssetSortingItem.TYPE_DEFAULT_ITEM -> {
                    item.asset.isFavorite = false
                    item.asset.isHidden = false
                }
                AssetSortingItem.TYPE_HIDDEN_ITEM -> {
                    item.asset.isFavorite = false
                    item.asset.isHidden = true
                }
            }

            // disable animation before update item to fix blink bug
            recycle_assets.itemAnimator = null

            // update item and empty views
            adapter.setData(holder.adapterPosition, item)
            adapter.checkEmptyViews()

            // enable animation after update
            runDelayed(150) {
                recycle_assets.itemAnimator = itemAnimator
            }

            AssetBalanceDb(item.asset).save()
            AssetBalanceStoreDb(item.asset.assetId, item.asset.isHidden, item.asset.position,
                    item.asset.isFavorite).save()
        }
    }

    private fun setScreenType(position: Int) {
        adapter.data.forEach {
            it.asset.configureVisibleState = position == TYPE_VISIBILITY
            presenter.visibilityConfigurationActive = position == TYPE_VISIBILITY
        }
        adapter.notifyDataSetChanged()

        logAnalyticEvents(position)
    }

    private fun logAnalyticEvents(position: Int) {
        when (position) {
            TYPE_POSITION -> {
                analytics.trackEvent(AnalyticEvents.WalletTokenSortingPositionEvent)
            }
            TYPE_VISIBILITY -> {
                analytics.trackEvent(AnalyticEvents.WalletTokenSortingVisabilityEvent)
            }
        }
    }

    fun setTabsEnable(enable: Boolean) {
        // to fix quick click to tab when drag & drop
        (common_tab_layout[0] as? LinearLayout)?.children?.forEach { view ->
            view.isClickable = enable
            view.isEnabled = enable
        }
    }

    override fun onBackPressed() {
        presenter.saveSortedPositions(adapter.data)

        setResult(Constants.RESULT_OK, Intent().apply {
            putExtra(AssetsFragment.RESULT_NEED_UPDATE, presenter.needToUpdate)
        })
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }

    override fun onDestroy() {
        super.onDestroy()
        progress_bar?.hide()
    }

    override fun showAssets(assets: MutableList<AssetSortingItem>) {
        adapter.setNewData(assets)
        progress_bar?.hide()
    }

    companion object {
        const val POSITION_NONE = -1

        const val TYPE_POSITION = 0
        const val TYPE_VISIBILITY = 1
    }
}