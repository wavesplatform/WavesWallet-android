package com.wavesplatform.wallet.v2.ui.home.dex.trade.last_trades

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.data.model.remote.response.LastTrade
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.TradeActivity
import com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.TradeBuyAndSellBottomSheetFragment
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.fragment_trade_last_trades.*
import kotlinx.android.synthetic.main.layout_empty_data.view.*
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.inflate
import pers.victor.ext.visiable
import javax.inject.Inject


class TradeLastTradesFragment : BaseFragment(), TradeLastTradesView {

    @Inject
    @InjectPresenter
    lateinit var presenter: TradeLastTradesPresenter

    @Inject
    lateinit var adapter: TradeLastTradesAdapter

    companion object {
        fun newInstance(watchMarket: WatchMarket?): TradeLastTradesFragment {
            val args = Bundle()
            args.classLoader = WatchMarket::class.java.classLoader
            args.putParcelable(TradeActivity.BUNDLE_MARKET, watchMarket)
            val fragment = TradeLastTradesFragment()
            fragment.arguments = args
            return fragment
        }
    }


    @ProvidePresenter
    fun providePresenter(): TradeLastTradesPresenter = presenter

    override fun configLayoutRes() = R.layout.fragment_trade_last_trades


    override fun onViewReady(savedInstanceState: Bundle?) {
        presenter.watchMarket = arguments?.getParcelable<WatchMarket>(TradeActivity.BUNDLE_MARKET)

        swipe_container.setColorSchemeResources(R.color.submit400)

        presenter.watchMarket?.market.notNull {
            adapter.market = it
        }

        swipe_container.setOnRefreshListener {
            presenter.loadLastTrades()
        }

        recycle_last_trades.layoutManager = LinearLayoutManager(baseActivity)
        adapter.bindToRecyclerView(recycle_last_trades)

        linear_buy.click {
            val dialog = TradeBuyAndSellBottomSheetFragment.newInstance(presenter.watchMarket, TradeBuyAndSellBottomSheetFragment.BUY_TYPE)
            dialog.show(fragmentManager, dialog::class.java.simpleName)
        }

        linear_sell.click {
            val dialog = TradeBuyAndSellBottomSheetFragment.newInstance(presenter.watchMarket, TradeBuyAndSellBottomSheetFragment.SELL_TYPE)
            dialog.show(fragmentManager, dialog::class.java.simpleName)
        }

        presenter.loadLastTrades()
    }

    private fun getEmptyView(): View {
        val view = inflate(R.layout.layout_empty_data)
        view.text_empty.text = getString(R.string.last_trades_empty)
        return view
    }


    override fun afterSuccessLoadLastTrades(data: List<LastTrade>) {
        swipe_container.isRefreshing = false
        adapter.setNewData(data)
        adapter.emptyView = getEmptyView()
        if (data.isNotEmpty()) {
            linear_fields_name.visiable()
        } else {
            linear_fields_name.gone()
        }
    }

    override fun afterFailedLoadLastTrades() {
        swipe_container.isRefreshing = false
        if (adapter.data.isEmpty()) {
            adapter.emptyView = getEmptyView()
            linear_fields_name.gone()
        }
    }
}
