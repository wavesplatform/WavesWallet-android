package com.wavesplatform.wallet.v2.ui.home.history.details

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.vicpin.krealmextensions.queryFirst
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.SpamAsset
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.data.model.remote.response.TransactionType
import com.wavesplatform.wallet.v2.util.*
import kotlinx.android.synthetic.main.history_details_layout.view.*
import pers.victor.ext.app
import pers.victor.ext.gone
import pers.victor.ext.inflate
import pers.victor.ext.visiable
import javax.inject.Inject

class HistoryDetailsAdapter @Inject constructor() : PagerAdapter() {

    var mData: List<Transaction> = arrayListOf()

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val transaction = mData[position]
        val layout = inflate(R.layout.history_details_layout, collection, false) as ViewGroup

        var showTag = Constants.defaultAssets.any {
            it.assetId == transaction.assetId || transaction.assetId.isNullOrEmpty()
        }

        layout.image_history_type.setImageDrawable(transaction.transactionType().icon())
        layout.text_amount_value_in_dollar.gone()

        when (transaction.transactionType()) {
            TransactionType.SENT_TYPE -> {
                transaction.amount.notNull {
                    layout.text_amount_or_title.text = "-${MoneyUtil.getScaledText(it, transaction.asset).stripZeros()}"
                }
            }
            TransactionType.RECEIVED_TYPE -> {
                transaction.amount.notNull {
                    layout.text_amount_or_title.text = "+${MoneyUtil.getScaledText(it, transaction.asset).stripZeros()}"
                }
            }
            TransactionType.MASS_SPAM_RECEIVE_TYPE, TransactionType.MASS_SEND_TYPE, TransactionType.MASS_RECEIVE_TYPE -> {
                if (transaction.transfers.isNotEmpty()) {
                    val sum = transaction.transfers.sumByLong { it.amount }
                    if (transaction.transactionType() == TransactionType.MASS_SPAM_RECEIVE_TYPE ||
                            transaction.transactionType() == TransactionType.MASS_RECEIVE_TYPE) {
                        layout.text_amount_or_title.text =
                                "+${MoneyUtil.getScaledText(sum, transaction.asset).stripZeros()}"
                    } else {
                        layout.text_amount_or_title.text =
                                "-${MoneyUtil.getScaledText(sum, transaction.asset).stripZeros()}"
                    }
                }

            }
            TransactionType.CREATE_ALIAS_TYPE -> {
                layout.text_amount_or_title.text = transaction.alias
            }
            TransactionType.EXCHANGE_TYPE -> {
                val myOrder =
                        if (transaction.order1?.sender == App.getAccessManager().getWallet()?.address) transaction.order1
                        else transaction.order2

                val pairOrder =
                        if (transaction.order1?.sender != App.getAccessManager().getWallet()?.address) transaction.order1
                        else transaction.order2

                layout.text_amount_value_in_dollar.visiable()
                if (myOrder?.orderType == Constants.SELL_ORDER_TYPE) {
                    layout.text_amount_value_in_dollar.text = "-${MoneyUtil.getScaledText(transaction.amount, myOrder.assetPair?.amountAssetObject).stripZeros()} ${myOrder.assetPair?.amountAssetObject?.name}"
                    layout.text_amount_or_title.text = "+${MoneyUtil.getScaledText(transaction.amount?.times(transaction.price!!)?.div(100000000), pairOrder?.assetPair?.priceAssetObject).stripZeros()}"
                } else {
                    layout.text_amount_value_in_dollar.text = "+${MoneyUtil.getScaledText(transaction.amount, myOrder?.assetPair?.amountAssetObject).stripZeros()} ${myOrder?.assetPair?.amountAssetObject?.name}"
                    layout.text_amount_or_title.text = "-${MoneyUtil.getScaledText(transaction.amount?.times(transaction.price!!)?.div(100000000), pairOrder?.assetPair?.priceAssetObject).stripZeros()}"
                }

                showTag = Constants.defaultAssets.any {
                    it.assetId == pairOrder?.assetPair?.priceAssetObject?.id || pairOrder?.assetPair?.priceAssetObject?.id.isNullOrEmpty()
                }

                if (showTag) {
                    layout.text_tag.visiable()
                    layout.text_tag.text = pairOrder?.assetPair?.priceAssetObject?.name
                } else {
                    layout.text_tag.gone()
                    layout.text_amount_value_in_dollar.text = "${layout.text_amount_value_in_dollar.text} ${pairOrder?.assetPair?.priceAssetObject?.name}"
                }
            }
            TransactionType.DATA_TYPE -> {
                layout.text_amount_or_title.text = app.getString(R.string.history_data_type_title)
            }
            TransactionType.CANCELED_LEASING_TYPE -> {
                transaction.lease?.amount.notNull {
                    layout.text_amount_or_title.text = MoneyUtil.getScaledText(it.toLong(), transaction.asset).stripZeros()
                }
            }
            TransactionType.TOKEN_GENERATION_TYPE -> {
                val quantity = MoneyUtil.getScaledText(transaction.quantity, transaction.asset)
                        .substringBefore(".")
                layout.text_amount_or_title.text = quantity
            }
            TransactionType.TOKEN_BURN_TYPE -> {
                transaction.amount.notNull {
                    val afterDot = MoneyUtil.getScaledText(it, transaction.asset)
                            .substringAfter(".").toInt()
                    val amount = if (afterDot == 0) {
                        MoneyUtil.getScaledText(it, transaction.asset).substringBefore(".")
                    } else {
                        MoneyUtil.getScaledText(it, transaction.asset)
                    }

                    layout.text_amount_or_title.text = "-$amount"
                }
            }
            TransactionType.TOKEN_REISSUE_TYPE -> {
                val quantity = MoneyUtil.getScaledText(transaction.quantity, transaction.asset)
                        .substringBefore(".")
                layout.text_amount_or_title.text = "+${quantity}"
            }
            else -> {
                transaction.amount.notNull {
                    layout.text_amount_or_title.text = MoneyUtil.getScaledText(it, transaction.asset).stripZeros()
                }
            }
        }

        if (transaction.transactionType() != TransactionType.CREATE_ALIAS_TYPE
                && transaction.transactionType() != TransactionType.DATA_TYPE
                && transaction.transactionType() != TransactionType.SPAM_RECEIVE_TYPE
                && transaction.transactionType() != TransactionType.MASS_SPAM_RECEIVE_TYPE
                && transaction.transactionType() != TransactionType.EXCHANGE_TYPE) {
            if (showTag) {
                layout.text_tag.visiable()
                layout.text_tag.text = transaction.asset?.name
            } else {
                layout.text_tag.gone()
                layout.text_amount_or_title.text = "${layout.text_amount_or_title.text} ${transaction.asset?.name}"
            }
        }

        if (queryFirst<SpamAsset> { equalTo("assetId", transaction.assetId) } != null) {
            layout.text_tag.gone()
            layout.text_tag_spam.visiable()
            layout.text_amount_or_title.text = "${layout.text_amount_or_title.text}"
        }


        layout.text_amount_or_title.makeTextHalfBold()

        collection.addView(layout)
        return layout
    }

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