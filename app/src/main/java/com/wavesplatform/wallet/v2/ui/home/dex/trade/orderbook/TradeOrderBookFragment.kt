package com.wavesplatform.wallet.v2.ui.home.dex.trade.orderbook

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.model.local.BuySellData
import com.wavesplatform.wallet.v2.data.model.local.LastPriceItem
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.data.model.remote.response.OrderBook
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.TradeActivity
import com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.TradeBuyAndSellBottomSheetFragment
import com.wavesplatform.wallet.v2.util.notNull
import com.wavesplatform.wallet.v2.util.stripZeros
import kotlinx.android.synthetic.main.fragment_trade_orderbook.*
import kotlinx.android.synthetic.main.global_server_error_layout.*
import kotlinx.android.synthetic.main.layout_empty_data.view.*
import pers.victor.ext.*
import java.math.RoundingMode
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

    override fun configLayoutRes() = R.layout.fragment_trade_orderbook

    override fun onViewReady(savedInstanceState: Bundle?) {
        presenter.watchMarket = arguments?.getParcelable<WatchMarket>(TradeActivity.BUNDLE_MARKET)

        eventSubscriptions.add(rxEventBus.filteredObservable(Events.DexOrderButtonClickEvent::class.java)
                .subscribe {
                    if (it.buy) {
                        openOrderDialog(true, getAskPrice())
                    } else {
                        openOrderDialog(false, getBidPrice())
                    }
                })

        eventSubscriptions.add(rxEventBus.filteredObservable(Events.OrderBookTabClickEvent::class.java)
                .subscribe {
                    presenter.clearSubscriptions()
                    presenter.loadOrderBook()
                })

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
            if (isNetworkConnected()) {
                val item = adapter.getItem(position) as MultiItemEntity
                when (item.itemType) {
                    TradeOrderBookAdapter.ASK_TYPE -> {
                        item as OrderBook.Ask
                        openOrderDialog(true, item.price, item.amount)
                    }
                    TradeOrderBookAdapter.BID_TYPE -> {
                        item as OrderBook.Bid
                        openOrderDialog(false, item.price, item.amount)
                    }
                }
            }
        }

        button_retry.click {
            error_layout.gone()
            progress_bar.show()

            presenter.clearSubscriptions()
            presenter.loadOrderBook()
        }

        linear_buy.click {
            openOrderDialog(true, getAskPrice())
        }

        linear_sell.click {
            openOrderDialog(false, getBidPrice())
        }

        presenter.loadOrderBook()
    }

    private fun getLastPrice(): Long? {
        val itemEntity = adapter.data.firstOrNull { it.itemType == TradeOrderBookAdapter.LAST_PRICE_TYPE }
        return if (itemEntity != null) {
            (itemEntity as LastPriceItem).lastTrade?.price?.toBigDecimal()?.setScale(8.plus(presenter.watchMarket?.market?.priceAssetDecimals
                    ?: 0)
                    .minus(presenter.watchMarket?.market?.amountAssetDecimals ?: 0), RoundingMode.HALF_UP)?.unscaledValue()?.toLong()
        } else {
            null
        }
    }

    private fun openOrderDialog(buy: Boolean, initPriceValue: Long?, initAmountValue: Long? = null) {
        val data = BuySellData(watchMarket = presenter.watchMarket, initAmount = initAmountValue, initPrice = initPriceValue,
                bidPrice = getBidPrice(), askPrice = getAskPrice(), lastPrice = getLastPrice())
        data.orderType =
                if (buy) TradeBuyAndSellBottomSheetFragment.BUY_TYPE
                else TradeBuyAndSellBottomSheetFragment.SELL_TYPE

        val dialog = TradeBuyAndSellBottomSheetFragment.newInstance(data)
        dialog.show(fragmentManager, dialog::class.java.simpleName)
    }

    private fun getBidPrice(): Long? {
        val itemEntity = adapter.data.filter { it.itemType == TradeOrderBookAdapter.BID_TYPE }.firstOrNull()
        return if (itemEntity != null) {
            (itemEntity as OrderBook.Bid).price
        } else {
            null
        }
    }

    private fun getAskPrice(): Long? {
        val itemEntity = adapter.data.filter { it.itemType == TradeOrderBookAdapter.ASK_TYPE }.lastOrNull()
        return if (itemEntity != null) {
            (itemEntity as OrderBook.Ask).price
        } else {
            null
        }
    }

    override fun afterSuccessOrderbook(data: MutableList<MultiItemEntity>, lastPricePosition: Int) {
        progress_bar.hide()
        adapter.setNewData(data)
        adapter.emptyView = getEmptyView()
        linear_buttons.visiable()
        if (data.isNotEmpty()) {
            linear_fields_name.visiable()
            if (presenter.needFirstScrollToLastPrice) {
                presenter.needFirstScrollToLastPrice = false
                recycle_orderbook.post {
                    val lastPosition = orderBookLinearLayoutManager.findLastVisibleItemPosition()
                    var scrollPosition = lastPricePosition + lastPosition / 2
                    if (scrollPosition >= adapter.data.size) scrollPosition = adapter.data.size - 1
                    recycle_orderbook?.scrollToPosition(scrollPosition)
                }
            }
        } else {
            linear_fields_name.gone()
        }

        fillButtonsPrice()
    }

    private fun fillButtonsPrice() {
        rxEventBus.post(Events.UpdateButtonsPrice(getAskPrice(), getBidPrice()))
        recycle_orderbook?.post {
            getBidPrice().notNull {
                text_sell_value?.text = MoneyUtil.getScaledPrice(it, presenter.watchMarket?.market?.amountAssetDecimals
                        ?: 0, presenter.watchMarket?.market?.priceAssetDecimals
                        ?: 0).stripZeros()
            }

            getAskPrice().notNull {
                text_buy_value?.text = MoneyUtil.getScaledPrice(it, presenter.watchMarket?.market?.amountAssetDecimals
                        ?: 0, presenter.watchMarket?.market?.priceAssetDecimals
                        ?: 0).stripZeros()
            }
        }
    }

    override fun afterFailedOrderbook() {
        progress_bar.hide()
        if (adapter.data.isEmpty()) {
            linear_buttons.gone()
            linear_fields_name.gone()

            error_layout.visiable()
        }
    }

    private fun getEmptyView(): View {
        val view = inflate(R.layout.layout_empty_data)
        view.text_empty.text = getString(R.string.orderbook_empty)
        return view
    }

    override fun onDestroyView() {
        presenter.clearSubscriptions()
        progress_bar.hide()
        super.onDestroyView()
    }

    override fun onNetworkConnectionChanged(networkConnected: Boolean) {
        super.onNetworkConnectionChanged(networkConnected)
        linear_buy.isClickable = networkConnected
        linear_sell.isClickable = networkConnected
        if (networkConnected) {
            linear_buy.setBackgroundResource(R.drawable.shape_btn_waves_blue_default)
            linear_sell.setBackgroundResource(R.drawable.shape_btn_red_default)
        } else {
            linear_buy.setBackgroundResource(R.drawable.shape_btn_waves_blue_disabled)
            linear_sell.setBackgroundResource(R.drawable.shape_btn_red_disabled)
        }
    }

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
}
