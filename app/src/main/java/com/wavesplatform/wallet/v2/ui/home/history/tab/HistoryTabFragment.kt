package com.wavesplatform.wallet.v2.ui.home.history.tab

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.history.HistoryItem
import com.wavesplatform.wallet.v2.ui.home.history.HistoryItemAdapter
import com.wavesplatform.wallet.v2.ui.home.history.details.HistoryDetailsBottomSheetFragment
import kotlinx.android.synthetic.main.fragment_history_date.*
import pers.victor.ext.gone
import pers.victor.ext.visiable
import javax.inject.Inject
import kotlin.collections.ArrayList


class HistoryTabFragment : BaseFragment(), HistoryTabView {

    @Inject
    @InjectPresenter
    lateinit var presenter: HistoryTabPresenter

    @ProvidePresenter
    fun providePresenter(): HistoryTabPresenter = presenter

    @Inject
    lateinit var adapter: HistoryItemAdapter

    override fun configLayoutRes(): Int = R.layout.fragment_history_date

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
        recycle_history.layoutManager = LinearLayoutManager(baseActivity)
        recycle_history.adapter = adapter
        recycle_history.isNestedScrollingEnabled = false

        presenter.loadBundle(arguments?.getString("type"))

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

    override fun showData(data: ArrayList<HistoryItem>, type: String?) {
        adapter.setNewData(data)
    }
}
