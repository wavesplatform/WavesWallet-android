package com.wavesplatform.wallet.v2.ui.home.dex.trade.last_trades

import android.icu.lang.UProperty.INT_START
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import javax.inject.Inject

import com.arellomobile.mvp.presenter.InjectPresenter

import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment;

import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.home.dex.trade.my_orders.TradeMyOrdersAdapter
import com.wavesplatform.wallet.v2.ui.home.history.TestObject
import io.github.kbiakov.codeview.html
import kotlinx.android.synthetic.main.fragment_trade_last_trades.*
import android.text.Spannable
import android.text.SpannableStringBuilder




class TradeLastTradesFragment : BaseFragment(), TradeLastTradesView {

    @Inject
    @InjectPresenter
    lateinit var presenter: TradeLastTradesPresenter

    @Inject
    lateinit var adapter: TradeLastTradesAdapter


    @ProvidePresenter
    fun providePresenter(): TradeLastTradesPresenter = presenter

    override fun configLayoutRes() = R.layout.fragment_trade_last_trades


    override fun onViewReady(savedInstanceState: Bundle?) {
        recycle_last_trades.layoutManager = LinearLayoutManager(baseActivity)
        recycle_last_trades.adapter = adapter
        recycle_last_trades.isNestedScrollingEnabled = false

        presenter.loadLastTrades()
    }

    override fun afterSuccessLoadLastTrades(data: ArrayList<TestObject>) {
        adapter.setNewData(data)
    }

}
