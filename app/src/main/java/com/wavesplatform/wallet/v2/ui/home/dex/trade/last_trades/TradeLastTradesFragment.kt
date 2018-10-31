package com.wavesplatform.wallet.v2.ui.home.dex.trade.last_trades

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.TradeActivity
import com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.TradeBuyAndSellBottomSheetFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.chart.TradeChartFragment
import com.wavesplatform.wallet.v2.ui.home.history.TestObject
import kotlinx.android.synthetic.main.fragment_trade_last_trades.*
import kotlinx.android.synthetic.main.layout_empty_data.*
import pers.victor.ext.click
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

        text_empty.text = getString(R.string.last_trades_empty)

        recycle_last_trades.layoutManager = LinearLayoutManager(baseActivity)
        recycle_last_trades.adapter = adapter
        recycle_last_trades.isNestedScrollingEnabled = false

        linear_buy.click {
            val dialog = TradeBuyAndSellBottomSheetFragment()
            dialog.arguments = Bundle().apply {
                putInt(TradeBuyAndSellBottomSheetFragment.BUNDLE_OPEN, TradeBuyAndSellBottomSheetFragment.OPEN_BUY)
            }
            dialog.show(fragmentManager, dialog::class.java.simpleName)
        }

        linear_sell.click {
            val dialog = TradeBuyAndSellBottomSheetFragment()
            dialog.arguments = Bundle().apply {
                putInt(TradeBuyAndSellBottomSheetFragment.BUNDLE_OPEN, TradeBuyAndSellBottomSheetFragment.OPEN_SELL)
            }
            dialog.show(fragmentManager, dialog::class.java.simpleName)
        }

        presenter.loadLastTrades()
    }

    override fun afterSuccessLoadLastTrades(data: ArrayList<TestObject>) {
        adapter.setNewData(data)
    }

}
