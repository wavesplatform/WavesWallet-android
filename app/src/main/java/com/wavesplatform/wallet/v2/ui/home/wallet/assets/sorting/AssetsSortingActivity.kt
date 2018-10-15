package com.wavesplatform.wallet.v2.ui.home.wallet.assets.sorting

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Menu
import android.view.MenuItem
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback
import com.chad.library.adapter.base.listener.OnItemDragListener
import com.vicpin.krealmextensions.save
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.custom.FadeInWithoutDelayAnimator
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.AssetsFragment.Companion.RESULT_NEED_UPDATE
import kotlinx.android.synthetic.main.activity_assets_sorting.*
import kotlinx.android.synthetic.main.wallet_asset_sorting_item.view.*
import pers.victor.ext.*
import pyxis.uzuki.live.richutilskt.utils.runAsync
import javax.inject.Inject


class AssetsSortingActivity : BaseActivity(), AssetsSortingView {

    @Inject
    @InjectPresenter
    lateinit var presenter: AssetsSortingPresenter

    @ProvidePresenter
    fun providePresenter(): AssetsSortingPresenter = presenter

    @Inject
    lateinit var adapter: AssetsSortingAdapter

    @Inject
    lateinit var adapterFavorites: AssetsFavoriteSortingAdapter

    override fun configLayoutRes() = R.layout.activity_assets_sorting


    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        setupToolbar(toolbar_view, true, getString(R.string.wallet_sorting_toolbar_title), R.drawable.ic_toolbar_back_black)

        recycle_favorite_assets.layoutManager = LinearLayoutManager(this)
        recycle_favorite_assets.adapter = adapterFavorites
        recycle_favorite_assets.isNestedScrollingEnabled = false
        recycle_favorite_assets.itemAnimator = FadeInWithoutDelayAnimator()

        recycle_assets.layoutManager = LinearLayoutManager(this)
        recycle_assets.adapter = adapter
        recycle_assets.isNestedScrollingEnabled = false
        recycle_assets.itemAnimator = FadeInWithoutDelayAnimator()

        adapter.onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.image_favorite -> {
                    presenter.needToUpdate = true

                    // manage UI
                    val item = this.adapter.getItem(position) as AssetBalance

                    item.isFavorite = true
                    item.isHidden = false

                    // add to favorite list
                    this.adapterFavorites.addData(item)

                    // remove from current list
                    this.adapter.data.removeAt(position)
                    this.adapter.notifyItemRemoved(position)

                    // Save to DB
                    item.save()

                    checkIfNeedToShowLine()
                }
            }
        }

        adapterFavorites.onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.image_favorite -> {
                    presenter.needToUpdate = true

                    // manage UI

                    val item = this.adapterFavorites.getItem(position) as AssetBalance
                    if (!item.assetId.isNullOrEmpty()) {
                        item.isFavorite = false

                        // add to not favorite list
                        this.adapter.addData(0, item)

                        // remove from favorite list
                        this.adapterFavorites.data.removeAt(position)
                        this.adapterFavorites.notifyItemRemoved(position)

                        // Save to DB
                        item.save()

                        checkIfNeedToShowLine()
                    }
                }
            }
        }

        adapter.onHiddenChangeListener = object : AssetsSortingAdapter.OnHiddenChangeListener {
            override fun onHiddenStateChanged(item: AssetBalance, checked: Boolean) {
                presenter.needToUpdate = true
                item.isHidden = !checked
                item.save()
            }
        }

        // configure drag and drop
        val itemDragAndSwipeCallback = ItemDragAndSwipeCallback(adapter)
        val itemTouchHelper = ItemTouchHelper(itemDragAndSwipeCallback)
        itemTouchHelper.attachToRecyclerView(recycle_assets)

        // allow drag and manage background of view
        adapter.enableDragItem(itemTouchHelper, R.id.image_drag, false)
        adapter.setOnItemDragListener(object : OnItemDragListener {
            override fun onItemDragStart(viewHolder: RecyclerView.ViewHolder, pos: Int) {
                if (view_drag_bg.height == 0) {
                    view_drag_bg.setHeight(viewHolder.itemView.card_asset.height - dp2px(1))
                    view_drag_bg.setWidth(viewHolder.itemView.card_asset.width - dp2px(1))
                }

                val originalPos = IntArray(2)
                viewHolder.itemView.card_asset.getLocationOnScreen(originalPos)
                view_drag_bg.y = originalPos[1].toFloat() - getStatusBarHeight()
                view_drag_bg.visiable()

                if (adapter.getItem(pos)?.isHidden != true) {
                    viewHolder.itemView.card_asset.cardElevation = dp2px(4).toFloat()
                }
            }

            override fun onItemDragMoving(source: RecyclerView.ViewHolder, from: Int, target: RecyclerView.ViewHolder, to: Int) {
                presenter.needToUpdate = true
                val originalPos = IntArray(2)
                target.itemView.card_asset.getLocationOnScreen(originalPos)
                view_drag_bg.y = originalPos[1].toFloat() - getStatusBarHeight()
            }

            override fun onItemDragEnd(viewHolder: RecyclerView.ViewHolder, pos: Int) {
                if (adapter.getItem(pos)?.isHidden == true) {
                    viewHolder.itemView.card_asset.cardElevation = dp2px(0).toFloat()
                } else {
                    viewHolder.itemView.card_asset.cardElevation = dp2px(2).toFloat()
                }
                view_drag_bg.gone()
            }
        })


        // load assets from DB
        runAsync {
            presenter.loadAssets()
        }
    }

    override fun showFavoriteAssets(favorites: List<AssetBalance>) {
        adapterFavorites.setNewData(favorites)
        progress_bar.hide()
    }

    override fun showNotFavoriteAssets(notFavorites: List<AssetBalance>) {
        adapter.setNewData(notFavorites)
    }

    override fun checkIfNeedToShowLine() {
        if (adapter.data.isEmpty() or adapterFavorites.data.isEmpty()) view_divider.gone()
        else view_divider.visiable()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_assets_visibility -> {
                if (item.title == getString(R.string.wallet_sorting_toolbar_visibility_action)) {
                    item.title = getString(R.string.wallet_sorting_toolbar_position_action)
                } else {
                    item.title = getString(R.string.wallet_sorting_toolbar_visibility_action)
                }
                adapter.data.forEach {
                    it.configureVisibleState = !it.configureVisibleState
                }
                adapter.notifyDataSetChanged()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_assets_sorting, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        presenter.saveSortedPositions(adapter.data)

        setResult(Constants.RESULT_OK, Intent().apply {
            putExtra(RESULT_NEED_UPDATE, presenter.needToUpdate)
        })
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        progress_bar.hide()
    }

}
