package com.wavesplatform.wallet.v2.ui.home.dex.trade.my_orders.details

import android.support.v7.widget.AppCompatTextView
import android.view.View
import android.widget.LinearLayout
import com.jakewharton.rxbinding2.view.RxView
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.manager.MatcherDataManager
import com.wavesplatform.wallet.v2.data.model.local.MyOrderTransaction
import com.wavesplatform.wallet.v2.data.model.local.OrderStatus
import com.wavesplatform.wallet.v2.data.model.local.OrderType
import com.wavesplatform.wallet.v2.data.model.remote.response.TransactionType
import com.wavesplatform.wallet.v2.ui.base.view.BaseTransactionBottomSheetFragment
import com.wavesplatform.wallet.v2.util.*
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_history_bottom_sheet_base_info_layout.view.*
import kotlinx.android.synthetic.main.history_details_layout.view.*
import kotlinx.android.synthetic.main.layout_bottom_sheet_my_orders_body.view.*
import kotlinx.android.synthetic.main.layout_my_orders_bottom_sheet_bottom_btns.view.*
import pers.victor.ext.date
import pers.victor.ext.gone
import pers.victor.ext.visiable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MyOrderDetailsBottomSheetFragment : BaseTransactionBottomSheetFragment<MyOrderTransaction>() {

    @Inject
    lateinit var matcherDataManager: MatcherDataManager

    override fun configLayoutRes(): Int {
        return R.layout.history_details_bottom_sheet_dialog
    }

    override fun setupHeader(data: MyOrderTransaction): View? {
        val view = inflater?.inflate(R.layout.history_details_layout, null, false)

        view?.let {
            view.image_transaction_type.setImageDrawable(TransactionType.EXCHANGE_TYPE.icon())
            view.text_transaction_name.text = getString(TransactionType.EXCHANGE_TYPE.title)

            setExchangeItem(data, view)

            view.text_transaction_value.makeTextHalfBold()
        }

        return view
    }

    override fun setupBody(data: MyOrderTransaction): View? {
        val view = inflater?.inflate(
                R.layout.layout_bottom_sheet_my_orders_body,
                null, false)

        view?.let {
            if (data.orderResponse.getType() == OrderType.BUY) {
                view.text_type.text = getString(R.string.history_exchange_sell)
            } else {
                view.text_type.text = getString(R.string.history_exchange_buy)
            }

//            // show value for price
//            if (isShowTicker(myOrder.assetPair?.priceAssetObject?.id)) {
//                textPriceValue?.text = MoneyUtil.getScaledPrice(transaction.price,
//                        myOrder?.assetPair?.amountAssetObject?.precision ?: 0,
//                        myOrder?.assetPair?.priceAssetObject?.precision ?: 0)
//
//                val ticker = myOrder.assetPair?.priceAssetObject?.getTicker()
//                if (!ticker.isNullOrBlank()) {
//                    textPriceTag?.text = ticker
//                    textPriceTag?.visiable()
//                }
//            } else {
//                textPriceValue?.text = "${MoneyUtil.getScaledPrice(transaction.price,
//                        myOrder.assetPair?.amountAssetObject?.precision ?: 0,
//                        myOrder.assetPair?.priceAssetObject?.precision ?: 0)} " +
//                        "${myOrder.assetPair?.priceAssetObject?.name}"
//            }
//
//            // show value for amount
//            if (isShowTicker(myOrder.assetPair?.priceAssetObject?.id)) {
//                textExchangeValue?.text = MoneyUtil.getScaledPrice(transaction.getOrderSum(),
//                        myOrder.assetPair?.amountAssetObject?.precision ?: 0,
//                        myOrder.assetPair?.priceAssetObject?.precision ?: 0)
//
//                val ticker = myOrder.assetPair?.priceAssetObject?.getTicker()
//                if (!ticker.isNullOrBlank()) {
//                    textExchangeTag?.text = ticker
//                    textExchangeTag?.visiable()
//                }
//            } else {
//                textExchangeValue?.text = "${MoneyUtil.getScaledPrice(transaction.getOrderSum(),
//                        myOrder.assetPair?.amountAssetObject?.precision ?: 0,
//                        myOrder.assetPair?.priceAssetObject?.precision ?: 0)} " +
//                        "${myOrder.assetPair?.priceAssetObject?.name}"
//            }

        }

        return view
    }

    override fun setupInfo(data: MyOrderTransaction): View? {
        val view = inflater?.inflate(R.layout.fragment_history_bottom_sheet_base_info_layout, null, false)

        view?.let {
            // hide unused fields
            view.relative_block.gone()
            view.relative_confirmations.gone()

            // fill fee field
            view.text_fee?.text = MoneyUtil.getScaledText(data.fee, Constants.wavesAssetInfo.precision).stripZeros()
            view.text_base_info_tag.visiable()

            // fill time field
            view.text_timestamp?.text = data.orderResponse.timestamp.date("dd.MM.yyyy HH:mm")

            // fill status field
            val percent = (data.orderResponse.filled.toFloat() / data.orderResponse.amount.toFloat()) * 100
            view.text_status?.setBackgroundResource(0)
            view.text_status?.setTextColor(data.orderResponse.getType().color)
            when (data.orderResponse.getStatus()) {
                OrderStatus.Filled -> {
                    // force string, bcz percent with commission
                    view.text_status?.text = "100%"
                }
                OrderStatus.Cancelled -> {
                    // with template "Canceled ({percent}%)"
                    view.text_status?.text = getString(R.string.my_orders_details_canceled_status, "%.2f".format(percent).plus("%"))
                }
                else -> {
                    // with template "{percent}%"
                    view.text_status?.text = "%.2f".format(percent).plus("%")
                }
            }


        }

        return view
    }

    override fun setupFooter(data: MyOrderTransaction): View? {
        val view = inflater?.inflate(R.layout.layout_my_orders_bottom_sheet_bottom_btns, null, false)


        view?.let {
            eventSubscriptions.add(RxView.clicks(view.image_close)
                    .throttleFirst(1500, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        dismiss()
                    })

            if (!arrayOf(OrderStatus.Cancelled, OrderStatus.Filled).contains(data.orderResponse.getStatus())) {
                view.text_cancel_order.visiable()

                eventSubscriptions.add(RxView.clicks(view.text_cancel_order)
                        .throttleFirst(1500, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            cancelOrder(data)
                        })
            }
        }

        return view
    }

    private fun cancelOrder(data: MyOrderTransaction) {
        showProgressBar(true)
        eventSubscriptions.add(matcherDataManager.cancelOrder(data.orderResponse.id, data.amountAssetInfo?.id, data.priceAssetInfo?.id)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    showProgressBar(false)
                    configureView()
                }, {
                    showProgressBar(false)
                    it.printStackTrace()
                }))
    }

    private fun setExchangeItem(data: MyOrderTransaction, view: View) {
        val directionStringResId: Int
        val directionSign: String
        val amountValue = getScaledAmount(data.orderResponse.amount,
                data.amountAssetInfo?.precision ?: 8)

        if (data.orderResponse.type == Constants.SELL_ORDER_TYPE) {
            directionStringResId = R.string.my_orders_details_type_sell
            directionSign = "-"
        } else {
            directionStringResId = R.string.my_orders_details_type_buy
            directionSign = "+"
        }

        view.text_transaction_name.text = getString(
                directionStringResId,
                data.amountAssetInfo?.name,
                data.priceAssetInfo?.name)

        val amountAssetTicker = if (data.amountAssetInfo?.name == Constants.WAVES_ASSET_ID_FILLED) {
            Constants.WAVES_ASSET_ID_FILLED
        } else {
            data.amountAssetInfo?.ticker
        }

        val assetName = if (amountAssetTicker.isNullOrEmpty()) {
            " ${data.amountAssetInfo?.name}"
        } else {
            view.text_tag.visiable()
            view.text_tag.text = amountAssetTicker
            ""
        }

        view.text_transaction_value.text = directionSign + amountValue + assetName
    }
}