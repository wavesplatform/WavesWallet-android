package com.wavesplatform.wallet.v2.ui.home.history.details

import android.graphics.Typeface
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.sdk.utils.MoneyUtil
import com.wavesplatform.wallet.v2.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.sdk.model.response.Transaction
import com.wavesplatform.sdk.model.response.TransactionType
import com.wavesplatform.sdk.utils.TransactionUtil
import com.wavesplatform.wallet.v2.util.*
import kotlinx.android.synthetic.main.history_details_layout.view.*
import pers.victor.ext.gone
import pers.victor.ext.inflate
import pers.victor.ext.visiable
import javax.inject.Inject

class HistoryDetailsAdapter @Inject constructor() : PagerAdapter() {

    @Inject
    lateinit var prefsUtil: PrefsUtil

    var mData: List<Transaction> = arrayListOf()

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val transaction = mData[position]
        val layout = inflate(R.layout.history_details_layout, collection,
                false) as ViewGroup
//
//        layout.text_tag.gone()
//        layout.text_tag_spam.gone()
//
//        transaction.transactionType().notNull {
//            layout.image_history_type.setImageDrawable(it.icon())
//            layout.text_subtitle.text = layout.text_subtitle.context
//                    .getString(it.title)
//            when (it) {
//                TransactionType.SENT_TYPE -> {
//                    transaction.amount.notNull {
//                        layout.text_title.text =
//                                "-${MoneyUtil.getScaledText(it, transaction.asset)}"
//                    }
//                }
//                TransactionType.RECEIVED_TYPE -> {
//                    transaction.amount.notNull {
//                        layout.text_title.text =
//                                "+${MoneyUtil.getScaledText(it, transaction.asset)}"
//                    }
//                }
//                TransactionType.MASS_SPAM_RECEIVE_TYPE,
//                TransactionType.MASS_RECEIVE_TYPE,
//                TransactionType.MASS_SEND_TYPE -> {
//                    layout.text_title.text = getTransactionAmount(
//                            transaction = transaction, round = false)
//                }
//                TransactionType.CREATE_ALIAS_TYPE -> {
//                    layout.text_title.text = transaction.alias
//                    layout.text_title.setTypeface(null, Typeface.BOLD)
//                }
//                TransactionType.EXCHANGE_TYPE -> {
//                    setExchangeItem(transaction, layout)
//                }
//                TransactionType.CANCELED_LEASING_TYPE -> {
//                    transaction.lease?.amount.notNull {
//                        layout.text_title.text = MoneyUtil.getScaledText(
//                                it, transaction.asset)
//                    }
//                }
//                TransactionType.TOKEN_BURN_TYPE -> {
//                    transaction.amount.notNull {
//                        layout.text_title.text = "-" + TransactionUtil.getScaledText(it, transaction.asset)
//                    }
//                }
//                TransactionType.TOKEN_GENERATION_TYPE,
//                TransactionType.TOKEN_REISSUE_TYPE -> {
//                    val quantity = MoneyUtil.getScaledText(transaction.quantity, transaction.asset)
//                            .substringBefore(".")
//                    layout.text_title.text = quantity
//                }
//                TransactionType.DATA_TYPE,
//                TransactionType.SET_ADDRESS_SCRIPT_TYPE,
//                TransactionType.CANCEL_ADDRESS_SCRIPT_TYPE,
//                TransactionType.SET_SPONSORSHIP_TYPE,
//                TransactionType.CANCEL_SPONSORSHIP_TYPE,
//                TransactionType.UPDATE_ASSET_SCRIPT_TYPE -> {
//                    layout.text_title.setTypeface(null, Typeface.BOLD)
//                    layout.text_title.text = layout.text_subtitle.context
//                            .getString(it.title)
//                    layout.text_subtitle.text = layout.text_subtitle.context
//                            .getString(R.string.history_data_type_title)
//                }
//                else -> {
//                    transaction.amount.notNull {
//                        layout.text_title.text = MoneyUtil.getScaledText(it, transaction.asset)
//                    }
//                }
//            }
//        }
//
//        if (!TransactionType.isZeroTransferOrExchange(transaction.transactionType())) {
//            if (isSpamConsidered(transaction.assetId, prefsUtil)) {
//                layout.text_tag_spam.visiable()
//            } else {
//                if (isShowTicker(transaction.assetId)) {
//                    val ticker = transaction.asset?.getTicker()
//                    if (!ticker.isNullOrBlank()) {
//                        layout.text_tag.text = ticker
//                        layout.text_tag.visiable()
//                    }
//                } else {
//                    layout.text_title.text = "${layout.text_title.text} ${transaction.asset?.name}"
//                }
//            }
//        }
//        layout.text_title.makeTextHalfBold()

        collection.addView(layout)
        return layout
    }

//    private fun setExchangeItem(transaction: Transaction, view: View) {
//        val myOrder = findMyOrder(
//                transaction.order1!!,
//                transaction.order2!!,
//                App.getAccessManager().getWallet()?.address!!)
//
//        val directionSignPrice: String
//        val directionSignAmount: String
//        val amountAsset = myOrder.assetPair?.amountAssetObject!!
//        val priceAsset = myOrder.assetPair?.priceAssetObject!!
//        val amountValue = MoneyUtil.getScaledText(transaction.amount, amountAsset).stripZeros()
//        val priceValue = MoneyUtil.getScaledText(
//                transaction.amount.times(transaction.price).div(100000000),
//                priceAsset).stripZeros()
//
//        if (myOrder.orderType == Constants.SELL_ORDER_TYPE) {
//            directionSignPrice = "+"
//            directionSignAmount = "-"
//        } else {
//            directionSignPrice = "-"
//            directionSignAmount = "+"
//        }
//
//        view.text_subtitle.visiable()
//        view.text_subtitle.text = directionSignPrice + priceValue + " " + priceAsset.name
//
//        val amountAssetTicker = if (amountAsset.name == "WAVES") {
//            "WAVES"
//        } else {
//            amountAsset.ticker
//        }
//
//        val assetName = if (amountAssetTicker.isNullOrEmpty()) {
//            " ${amountAsset.name}"
//        } else {
//            view.text_tag.visiable()
//            view.text_tag.text = amountAssetTicker
//            ""
//        }
//
//        view.text_title.text = directionSignAmount + amountValue + assetName
//    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int {
        return mData.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }
}