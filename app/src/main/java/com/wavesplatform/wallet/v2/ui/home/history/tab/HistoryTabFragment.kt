package com.wavesplatform.wallet.v2.ui.home.history.tab

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.ViewGroup
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.custom.CustomLoadMoreView
import com.wavesplatform.wallet.v2.ui.home.history.HistoryFragment
import com.wavesplatform.wallet.v2.ui.home.history.HistoryItem
import com.wavesplatform.wallet.v2.ui.home.history.HistoryItemAdapter
import com.wavesplatform.wallet.v2.ui.home.history.details.HistoryDetailsBottomSheetFragment
import kotlinx.android.synthetic.main.fragment_history_tab.*
import pyxis.uzuki.live.richutilskt.utils.runAsync
import pyxis.uzuki.live.richutilskt.utils.runDelayed
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import java.util.*
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
    var changeTabBarVisibilityListener: ChangeTabBarVisibilityListener? = null

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

        const val TYPE = "type"

        const val PRE_LOAD_NUMBER = 7

        /**
         * @return HistoryTabFragment instance
         * */
        fun newInstance(type: String, asset: AssetBalance?): HistoryTabFragment {
            val historyDateItemFragment = HistoryTabFragment()
            val bundle = Bundle()
            bundle.putString(TYPE, type)
            bundle.putParcelable(HistoryFragment.BUNDLE_ASSET, asset)
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
        presenter.assetBalance = arguments?.getParcelable<AssetBalance>(HistoryFragment.BUNDLE_ASSET)

        runAsync {
            if (savedInstanceState == null) {
                presenter.loadTransactions()
            }
        }

        adapter.setLoadMoreView(CustomLoadMoreView())
        adapter.setPreLoadNumber(PRE_LOAD_NUMBER)
        adapter.setOnLoadMoreListener({
            if (!presenter.needLoadMore) {
                //Data are all loaded.
                adapter.loadMoreEnd()
            } else {
                runAsync {
                    presenter.loadMore(adapter.data.size)
                }
            }
        }, recycle_history)

        presenter.loadTransactions()

        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val historyItem = adapter.getItem(position) as HistoryItem
            if (!historyItem.isHeader) {
                val bottomSheetFragment = HistoryDetailsBottomSheetFragment()
                bottomSheetFragment.selectedItem = historyItem.t
                bottomSheetFragment.historyType = arguments?.getString(TYPE)
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

    override fun goneLoadMoreView() {
        runOnUiThread {
            adapter.loadMoreComplete()
        }
    }

    override fun afterSuccessLoadTransaction(data: ArrayList<HistoryItem>, type: String?) {
        configureTabLayout(type, data)

        configureEmptyView(data)

        adapter.setNewData(data)
    }

    private fun configureEmptyView(data: ArrayList<HistoryItem>) {
        if (data.isEmpty()) {
            // TODO: Fix(delete) after optimization bottom tab navigation
            if (adapter.emptyView != null) {
                if (adapter.emptyView.parent != null) {
                    (adapter.emptyView.parent as ViewGroup).removeView(adapter.emptyView)
                }
            }
            adapter.setEmptyView(R.layout.layout_empty_data)
        }
    }

    private fun configureTabLayout(type: String?, data: ArrayList<HistoryItem>) {
        // hide tab bar layout if not data available and show empty view
        if (type == all && data.isEmpty()) {
            changeTabBarVisibilityListener?.changeTabBarVisibility(false)
        } else if (type == all && data.isNotEmpty()) {
            changeTabBarVisibilityListener?.changeTabBarVisibility(true)
        }
    }

    override fun afterSuccessLoadMoreTransaction(data: ArrayList<HistoryItem>, type: String?) {
        adapter.loadMoreComplete()
        adapter.addData(data)
    }

    interface ChangeTabBarVisibilityListener {
        fun changeTabBarVisibility(show: Boolean)
    }
}
