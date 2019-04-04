/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.dex.trade.orderbook

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.sdk.net.model.WatchMarket
import com.wavesplatform.sdk.net.model.response.OrderBook
import com.wavesplatform.sdk.utils.MoneyUtil
import com.wavesplatform.sdk.utils.notNull
import com.wavesplatform.sdk.utils.stripZeros
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.analytics.AnalyticEvents
import com.wavesplatform.wallet.v2.data.analytics.analytics
import com.wavesplatform.wallet.v2.data.model.local.BuySellData
import com.wavesplatform.wallet.v2.data.model.local.LastPriceItem
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.TradeActivity
import com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.TradeBuyAndSellBottomSheetFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.smart_info.SmartPairInfoBottomSheetFragment
import com.wavesplatform.wallet.v2.util.safeLet
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
                        tryOpenOrderDialog(true, getAskPrice())
                    } else {
                        tryOpenOrderDialog(false, getBidPrice())
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
                        tryOpenOrderDialog(true, item.price, item.amount, item.sum)
                    }
                    TradeOrderBookAdapter.BID_TYPE -> {
                        item as OrderBook.Bid
                        tryOpenOrderDialog(false, item.price, item.amount, item.sum)
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
            tryOpenOrderDialog(true, getAskPrice())
        }

        linear_sell.click {
            tryOpenOrderDialog(false, getBidPrice())
        }

        presenter.loadOrderBook()
    }

    private fun getLastPrice(): Long? {
        val itemEntity = adapter.data.firstOrNull { it.itemType == TradeOrderBookAdapter.LAST_PRICE_TYPE }
        return if (itemEntity != null) {
            (itemEntity as LastPriceItem).lastTrade?.price?.toBigDecimal()?.setScale(8.plus(presenter.watchMarket?.market?.priceAssetDecimals
                    ?: 0)
                    .minus(presenter.watchMarket?.market?.amountAssetDecimals
                            ?: 0), RoundingMode.HALF_UP)?.unscaledValue()?.toLong()
        } else {
            null
        }
    }

    private fun tryOpenOrderDialog(buy: Boolean, initPriceValue: Long?, initAmountValue: Long? = null, initSumValue: Long? = null) {
        logEvent(buy)
        val amountAssetInfo = (activity as TradeActivity).presenter.amountAssetInfo
        val priceAssetInfo = (activity as TradeActivity).presenter.priceAssetInfo
        if ((amountAssetInfo?.hasScript == true || priceAssetInfo?.hasScript == true)) {
            safeLet(amountAssetInfo, priceAssetInfo) { amountAssetInfo, priceAssetInfo ->
                if (presenter.prefsUtil.isNotShownSmartAlertForPair(amountAssetInfo.id, priceAssetInfo.id)) {
                    openOrderDialog(initAmountValue, initPriceValue, initSumValue, buy)
                } else {
                    val smartPairInfoDialog = SmartPairInfoBottomSheetFragment()
                    val listener = object : SmartPairInfoBottomSheetFragment.SmartPairDialogListener {
                        override fun onContinueClicked(notShowAgain: Boolean) {
                            presenter.prefsUtil.setNotShownSmartAlertForPair(amountAssetInfo.id, priceAssetInfo.id, notShowAgain)
                            openOrderDialog(initAmountValue, initPriceValue, initSumValue, buy)
                        }

                        override fun onCancelClicked(notShowAgain: Boolean) {
                            presenter.prefsUtil.setNotShownSmartAlertForPair(amountAssetInfo.id, priceAssetInfo.id, notShowAgain)
                            // do nothing
                        }

                    }
                    smartPairInfoDialog.configureDialog(amountAssetInfo, priceAssetInfo, listener)
                    smartPairInfoDialog.show(fragmentManager, SmartPairInfoBottomSheetFragment::class.java.simpleName)
                }
            }
        } else {
            openOrderDialog(initAmountValue, initPriceValue, initSumValue, buy)
        }
    }

    private fun logEvent(buy: Boolean) {
        safeLet(presenter.watchMarket?.market?.amountAssetLongName, presenter.watchMarket?.market?.priceAssetLongName)
        { amountAssetName, priceAssetName ->
            if (buy) {
                analytics.trackEvent(AnalyticEvents.DEXBuyTapEvent(amountAssetName, priceAssetName))
            } else {
                analytics.trackEvent(AnalyticEvents.DEXSellTapEvent(amountAssetName, priceAssetName))
            }
        }
    }


    private fun openOrderDialog(initAmountValue: Long?, initPriceValue: Long?, initSumValue: Long?, buy: Boolean) {
        val data = BuySellData(watchMarket = presenter.watchMarket, initAmount = initAmountValue, initPrice = initPriceValue,
                initSum = initSumValue, bidPrice = getBidPrice(), askPrice = getAskPrice(), lastPrice = getLastPrice())
        data.orderType =
                if (buy) TradeBuyAndSellBottomSheetFragment.BUY_TYPE
                else TradeBuyAndSellBottomSheetFragment.SELL_TYPE

        val dialog = TradeBuyAndSellBottomSheetFragment.newInstance(data)
        dialog.show(childFragmentManager, dialog::class.java.simpleName)
    }

    private fun getBidPrice(): Long? {
        val itemEntity = adapter.data.firstOrNull { it.itemType == TradeOrderBookAdapter.BID_TYPE }
        return if (itemEntity != null) {
            (itemEntity as OrderBook.Bid).price
        } else {
            null
        }
    }

    private fun getAskPrice(): Long? {
        val itemEntity = adapter.data.lastOrNull { it.itemType == TradeOrderBookAdapter.ASK_TYPE }
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
