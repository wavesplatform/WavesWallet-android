package com.wavesplatform.wallet.v2.ui.home.dex.trade.my_orders

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.data.model.remote.response.OrderResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.TradeActivity
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.fragment_trade_my_orders.*
import kotlinx.android.synthetic.main.layout_empty_data.view.*
import pers.victor.ext.gone
import pers.victor.ext.inflate
import pers.victor.ext.visiable
import javax.inject.Inject

class TradeMyOrdersFragment : BaseFragment(), TradeMyOrdersView {

    @Inject
    @InjectPresenter
    lateinit var presenter: TradeMyOrdersPresenter

    @Inject
    lateinit var adapter: TradeMyOrdersAdapter

    @ProvidePresenter
    fun providePresenter(): TradeMyOrdersPresenter = presenter

    override fun configLayoutRes() = R.layout.fragment_trade_my_orders

    override fun onViewReady(savedInstanceState: Bundle?) {
        arguments?.getParcelable<WatchMarket>(TradeActivity.BUNDLE_MARKET)?.let {
            presenter.watchMarket = it
            adapter.market = it.market
        }

        eventSubscriptions.add(rxEventBus.filteredObservable(Events.NeedUpdateMyOrdersScreen::class.java)
                .subscribe {
                    loadOrders()
                })

        swipe_container.setColorSchemeResources(R.color.submit400)
        swipe_container.setOnRefreshListener {
            loadOrders()
        }

        recycle_my_orders.layoutManager = LinearLayoutManager(baseActivity)
        adapter.bindToRecyclerView(recycle_my_orders)
        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->

        }

        loadOrders()
    }

    override fun afterSuccessLoadMyOrders(data: List<OrderResponse>) {
        swipe_container.isRefreshing = false
        adapter.setNewData(data)
        adapter.emptyView = getEmptyView()
        if (data.isNotEmpty()) {
            linear_fields_name.visiable()
        } else {
            linear_fields_name.gone()
        }
    }

    private fun getEmptyView(): View {
        val view = inflate(R.layout.layout_empty_data)
        view.text_empty.text = getString(R.string.my_orders_empty)
        return view
    }

    override fun afterFailedLoadMyOrders() {
        swipe_container.isRefreshing = false
    }

    override fun afterSuccessCancelOrder() {
        loadOrders()
    }

    private fun loadOrders() {
        swipe_container.isRefreshing = true
        presenter.loadMyOrders()
    }

    companion object {
        fun newInstance(watchMarket: WatchMarket?): TradeMyOrdersFragment {
            val args = Bundle()
            args.classLoader = WatchMarket::class.java.classLoader
            args.putParcelable(TradeActivity.BUNDLE_MARKET, watchMarket)
            val fragment = TradeMyOrdersFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
