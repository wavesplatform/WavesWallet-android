package com.wavesplatform.wallet.v2.ui.home.history.tab

import android.os.Bundle
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.history.HistoryItem
import com.wavesplatform.wallet.v2.ui.home.history.HistoryItemAdapter
import com.wavesplatform.wallet.v2.ui.home.history.details.HistoryDetailsBottomSheetFragment
import kotlinx.android.synthetic.main.fragment_history_tab.*
import kotlinx.android.synthetic.main.view_load_more.view.*
import pers.victor.ext.dp2px
import pers.victor.ext.gone
import pers.victor.ext.inflate
import pers.victor.ext.visiable
import pyxis.uzuki.live.richutilskt.utils.runAsync
import pyxis.uzuki.live.richutilskt.utils.runDelayed
import javax.inject.Inject


class HistoryTabFragment : BaseFragment(), HistoryTabView {

    @Inject
    @InjectPresenter
    lateinit var presenter: HistoryTabPresenter

    @ProvidePresenter
    fun providePresenter(): HistoryTabPresenter = presenter

    @Inject
    lateinit var adapter: HistoryItemAdapter
    lateinit var layoutManager: LinearLayoutManager


    override fun configLayoutRes(): Int = R.layout.fragment_history_tab

    companion object {

        const val all = "All"
        const val send = "Sent"
        const val received = "Received"
        const val exchanged = "Exchanged"
        const val leased = "Leased"
        const val issued = "Issued"

        const val leasing_all = "Leasing All"
        const val leasing_active_now = "Active now"
        const val leasing_canceled = "Canceled"

        /**
         * @return HistoryTabFragment instance
         * */
        fun newInstance(type: String): HistoryTabFragment {
            val historyDateItemFragment = HistoryTabFragment()
            val bundle = Bundle()
            bundle.putString("type", type)
            historyDateItemFragment.arguments = bundle
            return historyDateItemFragment
        }
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        layoutManager = LinearLayoutManager(baseActivity)
        recycle_history.layoutManager = layoutManager
        recycle_history.adapter = adapter
        recycle_history.isNestedScrollingEnabled = false

        presenter.type = arguments?.getString("type")

        runAsync {
            presenter.loadTransactions()
        }

        if (adapter.footerLayout != null) {
            if (adapter.footerLayout.parent != null) {
                (adapter.footerLayout.parent as ViewGroup).removeView(adapter.footerLayout)
            }
        }
        adapter.addFooterView(getLoadingView())

        nested_scroll_view.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (v.getChildAt(v.childCount - 1) != null) {
                if (scrollY >= v.getChildAt(v.childCount - 1).measuredHeight - v.measuredHeight - dp2px(50) && scrollY > oldScrollY) {
                    if (presenter.needLoadMore) {
                        if (layoutManager.childCount + layoutManager.findFirstVisibleItemPosition() >= layoutManager.itemCount) {
                            if (presenter.loadMoreCompleted) {
                                presenter.loadMoreCompleted = false
                                adapter.footerLayout.load_more_loading_view.visiable()
                                runAsync({
                                    presenter.loadMore(adapter.data.size)
                                })
                            }
                        }
                    } else {
                        adapter.loadMoreEnd()
                    }
                }
            }
        })

        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val historyItem = adapter.getItem(position) as HistoryItem
            if (!historyItem.isHeader) {
                val bottomSheetFragment = HistoryDetailsBottomSheetFragment()
                bottomSheetFragment.selectedItem = historyItem.t
                bottomSheetFragment.historyType = arguments?.getString("type")
                val data = adapter?.data as ArrayList<HistoryItem>
                bottomSheetFragment.allItems = data.filter { !it.isHeader }.map { it.t }

                var sectionSize = 0
                for (i in 0..position) {
                    if (data[i].isHeader) sectionSize++
                }

                bottomSheetFragment.selectedItemPosition = position - sectionSize
                bottomSheetFragment.show(fragmentManager, bottomSheetFragment.tag)
            }
        }
    }

    private fun getLoadingView(): View? {
        return inflate(R.layout.view_load_more, null, false)
    }

    override fun goneLoadMoreView() {
        adapter.footerLayout.load_more_loading_view.gone()
    }

    override fun showData(data: ArrayList<HistoryItem>, type: String?) {
        // stop over scroll
        nested_scroll_view.fling(0)

        runDelayed(350, {
            adapter.addData(data)
            adapter.footerLayout.load_more_loading_view.gone()
            presenter.loadMoreCompleted = true
        })

    }
}
