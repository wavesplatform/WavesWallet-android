/*
 * Created by Eduard Zaydel on 23/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.assets.sorting.tab

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.MenuItem
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.vicpin.krealmextensions.save
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.local.AssetSortingItem
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.userdb.AssetBalanceStore
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.custom.FadeInWithoutDelayAnimator
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.AssetsFragment.Companion.RESULT_NEED_UPDATE
import com.wavesplatform.wallet.v2.util.drag_helper.ItemDragListener
import com.wavesplatform.wallet.v2.util.drag_helper.SimpleItemTouchHelperCallback
import kotlinx.android.synthetic.main.fragment_assets_sorting_tab.*
import kotlinx.android.synthetic.main.wallet_asset_sorting_item.view.*
import pers.victor.ext.*
import javax.inject.Inject


class AssetsSortingTabFragment : BaseFragment(), AssetsSortingTabView {
    @Inject
    @InjectPresenter
    lateinit var presenter: AssetsSortingTabPresenter

    @Inject
    lateinit var adapter: AssetsSortingAdapter

    var shadowListener: ToolbarShadowListener? = null
    private var mItemTouchHelper: ItemTouchHelper? = null

    @ProvidePresenter
    fun providePresenter(): AssetsSortingTabPresenter = presenter

    override fun configLayoutRes(): Int = R.layout.fragment_assets_sorting_tab


    override fun onViewReady(savedInstanceState: Bundle?) {
        arguments?.let { bundle ->
            presenter.screenType = bundle.getInt(BUNDLE_TYPE, TYPE_POSITION)
        }

        setupUI()
    }

    private fun setupUI() {
        recycle_assets.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                shadowListener?.showToolbarShadow(recycle_assets.canScrollVertically(-1))
            }
        })

        adapter.bindToRecyclerView(recycle_assets)
        recycle_assets.layoutManager = LinearLayoutManager(activity)
        recycle_assets.itemAnimator = FadeInWithoutDelayAnimator()

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
        mItemTouchHelper?.attachToRecyclerView(recycle_assets)

        adapter.mDragStartListener = object : ItemDragListener {
            override fun onStartDrag(viewHolder: RecyclerView.ViewHolder, position: Int) {
                mItemTouchHelper?.startDrag(viewHolder)
                if (view_drag_bg.height == 0) {
                    view_drag_bg.setHeight(viewHolder.itemView.card_asset.height - dp2px(1))
                    view_drag_bg.setWidth(viewHolder.itemView.card_asset.width - dp2px(1))
                }

                val originalPos = IntArray(2)
                viewHolder.itemView.card_asset.getLocationOnScreen(originalPos)
                view_drag_bg.y = originalPos[1].toFloat() - getStatusBarHeight()
                view_drag_bg.visiable()
            }

            override fun onMoved(fromHolder: View?, fromPosition: Int, toHolder: View?, toPosition: Int) {
                presenter.needToUpdate = true
                val originalPos = IntArray(2)
                toHolder?.card_asset?.getLocationOnScreen(originalPos)
                view_drag_bg.y = originalPos[1].toFloat() - getStatusBarHeight()
            }

            override fun onEndDrag() {
                view_drag_bg.gone()
            }
        }

        presenter.loadAssets()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_assets_visibility -> {
                if (item.title == getString(R.string.wallet_sorting_toolbar_visibility_action)) {
                    presenter.visibilityConfigurationActive = true
                    item.title = getString(R.string.wallet_sorting_toolbar_position_action)
                } else {
                    presenter.visibilityConfigurationActive = false
                    item.title = getString(R.string.wallet_sorting_toolbar_visibility_action)
                }
                adapter.data.forEach {
                    it.asset.configureVisibleState = !it.asset.configureVisibleState
                }
                adapter.notifyDataSetChanged()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        presenter.saveSortedPositions(adapter.data)

        activity?.setResult(Constants.RESULT_OK, Intent().apply {
            putExtra(RESULT_NEED_UPDATE, presenter.needToUpdate)
        })
        finish()
        activity?.overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }

    override fun onDestroy() {
        super.onDestroy()
        progress_bar.hide()
    }

    override fun showAssets(assets: MutableList<AssetSortingItem>) {
        adapter.setNewData(assets)
        progress_bar.hide()
    }

    interface ToolbarShadowListener {
        fun showToolbarShadow(show: Boolean)
    }

    companion object {
        const val BUNDLE_TYPE = "type"

        const val TYPE_POSITION = 1
        const val TYPE_VISIBILITY = 2

        fun newInstance(type: Int): AssetsSortingTabFragment {
            val fragment = AssetsSortingTabFragment()
            fragment.arguments = Bundle().apply {
                putInt(BUNDLE_TYPE, type)
            }
            return fragment
        }
    }

}
