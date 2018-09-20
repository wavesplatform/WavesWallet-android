package com.wavesplatform.wallet.v2.ui.home.dex.trade.orderbook

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.OrderbookItem
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.TradeBuyAndSellBottomSheetFragment
import kotlinx.android.synthetic.main.fragment_trade_orderbook.*
import kotlinx.android.synthetic.main.layout_empty_data.*
import pers.victor.ext.click
import java.util.*
import javax.inject.Inject


class TradeOrderbookFragment : BaseFragment(), TradeOrderbookView {

    @Inject
    @InjectPresenter
    lateinit var presenter: TradeOrderbookPresenter

    @Inject
    lateinit var adapter: TradeOrderbookAdapter

    @ProvidePresenter
    fun providePresenter(): TradeOrderbookPresenter = presenter

    override fun configLayoutRes() = R.layout.fragment_trade_orderbook


    override fun onViewReady(savedInstanceState: Bundle?) {
        text_empty.text = getString(R.string.orderbook_empty)

        recycle_orderbook.layoutManager = LinearLayoutManager(baseActivity)
        recycle_orderbook.adapter = adapter
        recycle_orderbook.isNestedScrollingEnabled = false

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

        presenter.loadOrderbook()
    }

    override fun afterSuccessOrderbook(data: ArrayList<OrderbookItem>) {
        adapter.setNewData(data)
    }
}
