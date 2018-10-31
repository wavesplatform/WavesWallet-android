package com.wavesplatform.wallet.v2.ui.home.dex.trade.my_orders

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.MyOrderItem
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.TradeActivity
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.fragment_trade_my_orders.*
import kotlinx.android.synthetic.main.layout_empty_data.*
import javax.inject.Inject


class TradeMyOrdersFragment : BaseFragment(), TradeMyOrdersView {
    @Inject
    @InjectPresenter
    lateinit var presenter: TradeMyOrdersPresenter

    @Inject
    lateinit var adapter: TradeMyOrdersAdapter

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

    @ProvidePresenter
    fun providePresenter(): TradeMyOrdersPresenter = presenter

    override fun configLayoutRes() = R.layout.fragment_trade_my_orders


    override fun onViewReady(savedInstanceState: Bundle?) {
        presenter.watchMarket = arguments?.getParcelable<WatchMarket>(TradeActivity.BUNDLE_MARKET)

        text_empty.text = getString(R.string.my_orders_empty)

        recycle_my_orders.layoutManager = LinearLayoutManager(baseActivity)
        presenter.watchMarket?.market.notNull {
            adapter.market = it
        }
        adapter.bindToRecyclerView(recycle_my_orders)

        presenter.loadMyOrders()
    }

    override fun afterSuccessMyOrders(data: ArrayList<MyOrderItem>) {
        adapter.setNewData(data)
    }

    override fun afterFailedMyOrders() {

    }

}
