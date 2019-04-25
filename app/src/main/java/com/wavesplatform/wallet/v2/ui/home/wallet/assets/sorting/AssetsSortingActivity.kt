/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.assets.sorting

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.flyco.tablayout.listener.CustomTabEntity
import com.flyco.tablayout.listener.OnTabSelectListener
import com.vicpin.krealmextensions.save
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.local.AssetSortingItem
import com.wavesplatform.wallet.v2.data.model.local.TabItem
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.userdb.AssetBalanceStore
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.custom.FadeInWithoutDelayAnimator
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.AssetsFragment
import com.wavesplatform.wallet.v2.util.drag_helper.ItemDragListener
import com.wavesplatform.wallet.v2.util.drag_helper.SimpleItemTouchHelperCallback
import kotlinx.android.synthetic.main.activity_assets_sorting.*
import kotlinx.android.synthetic.main.wallet_asset_sorting_item.view.*
import java.util.*
import javax.inject.Inject


class AssetsSortingActivity : BaseActivity(), AssetsSortingView {

    @Inject
    @InjectPresenter
    lateinit var presenter: AssetsSortingPresenter
    @Inject
    lateinit var adapter: AssetsSortingAdapter
    lateinit var mItemTouchHelper: ItemTouchHelper


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

        recycle_assets.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                appbar_layout.isSelected = recycle_assets.canScrollVertically(-1)
            }
        })

        common_tab_layout.setTabData(tabs)
        common_tab_layout.setOnTabSelectListener(object : OnTabSelectListener {
            override fun onTabSelect(position: Int) {
                setScreenType(position)
            }

            override fun onTabReselect(position: Int) {

            }
        })

        recycle_assets.layoutManager = LinearLayoutManager(this)
        recycle_assets.itemAnimator = FadeInWithoutDelayAnimator()

        adapter.bindToRecyclerView(recycle_assets)
        adapter.onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.image_favorite -> {
                    presenter.needToUpdate = true

                    val globalItem = this.adapter.getItem(position) as AssetSortingItem
                    val asset = globalItem.asset

                    when (globalItem.itemType) {
                        AssetSortingItem.TYPE_FAVORITE -> {
                            val linePosition = getLinePosition(false)

                            asset.isFavorite = false
                            asset.configureVisibleState = presenter.visibilityConfigurationActive

                            // remove from favorite list
                            this.adapter.data.removeAt(position)
                            this.adapter.notifyItemRemoved(position)

                            // add to not favorite list
                            globalItem.type = AssetSortingItem.TYPE_NOT_FAVORITE
                            globalItem.asset = asset
                            this.adapter.addData(linePosition, globalItem)

                            // Save to DB
                            asset.save()
                            AssetBalanceStore(asset.assetId, asset.isHidden, asset.position, asset.isFavorite).save()
                        }
                        AssetSortingItem.TYPE_NOT_FAVORITE -> {
                            // remove from current list
                            this.adapter.data.removeAt(position)
                            this.adapter.notifyItemRemoved(position)

                            val linePosition = getLinePosition(true)

                            asset.isFavorite = true
                            asset.isHidden = false

                            // add to favorite list
                            globalItem.type = AssetSortingItem.TYPE_FAVORITE
                            globalItem.asset = asset
                            this.adapter.addData(linePosition, globalItem)

                            // Save to DB
                            asset.save()
                            AssetBalanceStore(asset.assetId, asset.isHidden, asset.position,
                                    asset.isFavorite).save()
                        }
                    }
                }
            }
        }

        adapter.onHiddenChangeListener = object : AssetsSortingAdapter.OnHiddenChangeListener {
            override fun onHiddenStateChanged(item: AssetBalance, checked: Boolean) {
                presenter.needToUpdate = true
                item.isHidden = !checked
                item.save()
                AssetBalanceStore(item.assetId, item.isHidden, item.position,
                        item.isFavorite).save()
            }
        }

        // configure drag and drop
        val callback = SimpleItemTouchHelperCallback(adapter)
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper.attachToRecyclerView(recycle_assets)

        adapter.mDragStartListener = object : ItemDragListener {
            override fun onStartDrag(viewHolder: RecyclerView.ViewHolder, position: Int) {
                mItemTouchHelper.startDrag(viewHolder)

                val originalPos = IntArray(2)
                viewHolder.itemView.card_asset.getLocationOnScreen(originalPos)
            }

            override fun onMoved(fromHolder: View?, fromPosition: Int, toHolder: View?, toPosition: Int) {
                presenter.needToUpdate = true
                val originalPos = IntArray(2)
                toHolder?.card_asset?.getLocationOnScreen(originalPos)
            }

            override fun onEndDrag() {
            }
        }

        presenter.loadAssets()

        common_tab_layout.currentTab = 0
    }

    private fun setScreenType(position: Int) {
        adapter.data.forEach {
            it.asset.configureVisibleState = position == TYPE_VISIBILITY
        }
        adapter.notifyDataSetChanged()
    }

    private fun getLinePosition(toFavorite: Boolean): Int {
        var position = adapter.data.indexOfFirst { it.itemType == AssetSortingItem.TYPE_LINE }
        if (position == -1) {
            // add line
            val line = AssetSortingItem(AssetSortingItem.TYPE_LINE)

            if (toFavorite) {
                adapter.addData(0, line)
            } else {
                adapter.addData(line)
            }
            position = adapter.data.indexOfFirst { it.itemType == AssetSortingItem.TYPE_LINE }
        }
        return position
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
        const val TYPE_POSITION = 0
        const val TYPE_VISIBILITY = 1
    }
}
