package com.wavesplatform.wallet.v2.ui.home.dex.sorting

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback
import com.chad.library.adapter.base.listener.OnItemDragListener
import com.vicpin.krealmextensions.delete
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.MarketResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.custom.FadeInWithoutDelayAnimator
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.AssetsFragment.Companion.RESULT_NEED_UPDATE
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.activity_active_markets_sorting.*
import kotlinx.android.synthetic.main.dex_active_markets_sorting_item.view.*
import kotlinx.android.synthetic.main.layout_empty_data.view.*
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

        recycle_markets.layoutManager = LinearLayoutManager(this)
        recycle_markets.itemAnimator = FadeInWithoutDelayAnimator()
        adapter.bindToRecyclerView(recycle_markets)

        recycle_markets.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                appbar_layout.isSelected = recycle_markets.canScrollVertically(-1)
            }
        })

        adapter.onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.image_delete -> {
                    presenter.needToUpdate = true

                    val item = this.adapter.getItem(position)
                    item.notNull { item ->
                        item.delete { equalTo("id", item.id) }

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
            override fun onItemDragStart(viewHolder: RecyclerView.ViewHolder, pos: Int) {
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

            override fun onItemDragMoving(source: RecyclerView.ViewHolder, from: Int, target: RecyclerView.ViewHolder, to: Int) {
                presenter.needToUpdate = true

                val originalPos = IntArray(2)
                target.itemView.card_market.getLocationOnScreen(originalPos)
                view_drag_bg.y = originalPos[1].toFloat() - getStatusBarHeight()
            }

            override fun onItemDragEnd(viewHolder: RecyclerView.ViewHolder, pos: Int) {
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
        val view = inflate(R.layout.layout_empty_data)
        view.text_empty.text = getString(R.string.dex_sorting_empty)
        return view
    }
}
