/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.dex.sorting

import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.View
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback
import com.chad.library.adapter.base.listener.OnItemDragListener
import com.vicpin.krealmextensions.delete
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.sdk.model.response.matcher.MarketResponse
import com.wavesplatform.wallet.v2.data.model.db.userdb.MarketResponseDb
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.custom.FadeInWithoutDelayAnimator
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.AssetsFragment.Companion.RESULT_NEED_UPDATE
import com.wavesplatform.sdk.utils.notNull
import kotlinx.android.synthetic.main.activity_active_markets_sorting.*
import kotlinx.android.synthetic.main.item_dex_active_markets_sorting.view.*
import kotlinx.android.synthetic.main.content_empty_data.view.*
import pers.victor.ext.*
import javax.inject.Inject

class ActiveMarketsSortingActivity : BaseActivity(), ActiveMarketsSortingView {

    @Inject
    @InjectPresenter
    lateinit var presenter: ActiveMarketsSortingPresenter

    @Inject
    lateinit var adapter: ActiveMarketsSortingAdapter

    @ProvidePresenter
    fun providePresenter(): ActiveMarketsSortingPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_active_markets_sorting

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        setupToolbar(toolbar_view, true, getString(R.string.dex_sorting_toolbar_title), R.drawable.ic_toolbar_back_black)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.basic50)

        recycle_markets.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        recycle_markets.itemAnimator = FadeInWithoutDelayAnimator()
        adapter.bindToRecyclerView(recycle_markets)

        recycle_markets.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                appbar_layout.isSelected = recycle_markets.canScrollVertically(-1)
            }
        })

        adapter.onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.image_delete -> {
                    presenter.needToUpdate = true

                    val item = this.adapter.getItem(position)
                    item.notNull { item ->
                        MarketResponseDb(item).delete { equalTo("id", item.id) }

                        // remove from current list
                        this.adapter.data.removeAt(position)
                        this.adapter.notifyItemRemoved(position)
                    }
                }
            }
        }

        // configure drag and drop
        val itemDragAndSwipeCallback = ItemDragAndSwipeCallback(adapter)
        val itemTouchHelper = ItemTouchHelper(itemDragAndSwipeCallback)
        itemTouchHelper.attachToRecyclerView(recycle_markets)

        // allow drag and manage background of view
        adapter.enableDragItem(itemTouchHelper, R.id.image_drag, false)
        adapter.setOnItemDragListener(object : OnItemDragListener {
            override fun onItemDragStart(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, pos: Int) {
                if (view_drag_bg.height == 0) {
                    view_drag_bg.setHeight(viewHolder.itemView.card_market.height - dp2px(1))
                    view_drag_bg.setWidth(viewHolder.itemView.card_market.width - dp2px(1))
                }

                val originalPos = IntArray(2)
                viewHolder.itemView.card_market.getLocationOnScreen(originalPos)
                view_drag_bg.y = originalPos[1].toFloat() - getStatusBarHeight()
                view_drag_bg.visiable()

                viewHolder.itemView.card_market.cardElevation = dp2px(4).toFloat()
            }

            override fun onItemDragMoving(source: androidx.recyclerview.widget.RecyclerView.ViewHolder, from: Int, target: androidx.recyclerview.widget.RecyclerView.ViewHolder, to: Int) {
                presenter.needToUpdate = true

                val originalPos = IntArray(2)
                target.itemView.card_market.getLocationOnScreen(originalPos)
                view_drag_bg.y = originalPos[1].toFloat() - getStatusBarHeight()
            }

            override fun onItemDragEnd(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, pos: Int) {
                viewHolder.itemView.card_market.cardElevation = dp2px(2).toFloat()
                view_drag_bg.gone()
            }
        })

        presenter.loadMarkets()
    }

    override fun onBackPressed() {
        presenter.saveSortedPositions(adapter.data)

        setResult(Constants.RESULT_OK, Intent().apply {
            putExtra(RESULT_NEED_UPDATE, presenter.needToUpdate)
        })
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }

    override fun afterSuccessLoadMarkets(list: List<MarketResponse>) {
        adapter.setNewData(list)
        adapter.emptyView = getEmptyView()
    }

    private fun getEmptyView(): View {
        val view = inflate(R.layout.content_empty_data)
        view.text_empty.text = getString(R.string.dex_sorting_empty)
        return view
    }
}
