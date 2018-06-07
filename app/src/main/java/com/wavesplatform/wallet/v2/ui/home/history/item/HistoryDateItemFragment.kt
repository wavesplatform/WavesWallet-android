package com.wavesplatform.wallet.v2.ui.home.history.item

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.history.TestObject
import com.wavesplatform.wallet.v2.ui.home.history.adapter.HistoryItem
import com.wavesplatform.wallet.v2.ui.home.history.adapter.HistoryItemAdapter
import kotlinx.android.synthetic.main.fragment_history_date.*
import java.util.*
import javax.inject.Inject

class HistoryDateItemFragment : BaseFragment(), HistoryDateItemView {

    @Inject
    @InjectPresenter
    lateinit var presenter: HistoryDateItemPresenter

    @ProvidePresenter
    fun providePresenter(): HistoryDateItemPresenter = presenter

    @Inject
    lateinit var adapter: HistoryItemAdapter

    override fun configLayoutRes(): Int = R.layout.fragment_history_date

    companion object {

        const val all = "All"
        const val send = "Sent"
        const val received = "Received"

        /**
         * @return HistoryDateItemFragment instance
         * */
        fun newInstance(type: String): HistoryDateItemFragment {
            val historyDateItemFragment = HistoryDateItemFragment()
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

        presenter.loadBundle(arguments)
    }

    override fun showData(data: ArrayList<HistoryItem>, type: String) {
        adapter.setType(type)
        adapter.setNewData(data)
    }
}
