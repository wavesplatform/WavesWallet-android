package com.wavesplatform.wallet.v2.ui.home.dex.trade.orderbook

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.data.model.remote.response.OrderBook
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.TradeActivity
import com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.TradeBuyAndSellBottomSheetFragment
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.fragment_trade_orderbook.*
import kotlinx.android.synthetic.main.layout_empty_data.view.*
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.inflate
import pers.victor.ext.visiable
import javax.inject.Inject


class TradeOrderBookFragment : BaseFragment(), TradeOrderBookView {

    @Inject
    @InjectPresenter
    lateinit var presenter: TradeOrderBookPresenter

    @Inject
    lateinit var adapter: TradeOrderBookAdapter

    lateinit var orderBookLinearLayoutManager: LinearLayoutManager

    @ProvidePresenter
    fun providePresenter(): TradeOrderBookPresenter = presenter

    companion object {
        fun newInstance(watchMarket: WatchMarket?): TradeOrderBookFragment {
            val args = Bundle()
            args.classLoader = WatchMarket::class.java.classLoader
            args.putParcelable(TradeActivity.BUNDLE_MARKET, watchMarket)
            val fragment = TradeOrderBookFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun configLayoutRes() = R.layout.fragment_trade_orderbook

    override fun onViewReady(savedInstanceState: Bundle?) {
        presenter.watchMarket = arguments?.getParcelable<WatchMarket>(TradeActivity.BUNDLE_MARKET)

        presenter.watchMarket?.market.notNull {
            adapter.market = it

            text_amount_title.text = getString(R.string.orderbook_amount_title, it.amountAssetShortName)
            text_price_title.text = getString(R.string.orderbook_price_title, it.priceAssetShortName)
            text_sum_title.text = getString(R.string.orderbook_sum_title, it.priceAssetShortName)
        }


        orderBookLinearLayoutManager = LinearLayoutManager(baseActivity)
        recycle_orderbook.layoutManager = orderBookLinearLayoutManager

        adapter.bindToRecyclerView(recycle_orderbook)

        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val item = adapter.getItem(position) as MultiItemEntity
            when (item.itemType) {
                TradeOrderBookAdapter.ASK_TYPE -> {
                    item as OrderBook.Ask
                    val dialog = TradeBuyAndSellBottomSheetFragment.newInstance(presenter.watchMarket,
                            TradeBuyAndSellBottomSheetFragment.BUY_TYPE,
                            item.price, item.amount)
                    dialog.show(fragmentManager, dialog::class.java.simpleName)
                }
                TradeOrderBookAdapter.BID_TYPE -> {
                    item as OrderBook.Bid
                    val dialog = TradeBuyAndSellBottomSheetFragment.newInstance(presenter.watchMarket,
                            TradeBuyAndSellBottomSheetFragment.BUY_TYPE,
                            item.price, item.amount)
                    dialog.show(fragmentManager, dialog::class.java.simpleName)
                }
            }
        }

        linear_buy.click {
            val dialog = TradeBuyAndSellBottomSheetFragment.newInstance(presenter.watchMarket, TradeBuyAndSellBottomSheetFragment.BUY_TYPE)
            dialog.show(fragmentManager, dialog::class.java.simpleName)
        }

        linear_sell.click {
            val dialog = TradeBuyAndSellBottomSheetFragment.newInstance(presenter.watchMarket, TradeBuyAndSellBottomSheetFragment.SELL_TYPE)
            dialog.show(fragmentManager, dialog::class.java.simpleName)
        }

        presenter.loadOrderBook()
    }

    override fun afterSuccessOrderbook(data: MutableList<MultiItemEntity>, lastPricePosition: Int) {
        progress_bar.hide()
        adapter.setNewData(data)
        adapter.emptyView = getEmptyView()
        if (data.isNotEmpty()) {
            linear_fields_name.visiable()
            if (presenter.needFirstScrollToLastPrice) {
                presenter.needFirstScrollToLastPrice = false
                recycle_orderbook.post {
                    val lastPosition = orderBookLinearLayoutManager.findLastVisibleItemPosition()
                    recycle_orderbook.scrollToPosition(lastPricePosition + lastPosition / 2)
                }
            }
        } else {
            linear_fields_name.gone()
        }
    }

    override fun afterFailedOrderbook() {
        if (adapter.data.isEmpty()) {
            adapter.emptyView = getEmptyView()
            linear_fields_name.gone()
        }
    }

    private fun getEmptyView(): View {
        val view = inflate(R.layout.layout_empty_data)
        view.text_empty.text = getString(R.string.orderbook_empty)
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        progress_bar.hide()
    }
}
