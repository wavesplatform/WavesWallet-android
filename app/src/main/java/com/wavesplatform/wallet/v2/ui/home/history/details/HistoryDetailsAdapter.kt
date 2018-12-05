package com.wavesplatform.wallet.v2.ui.home.history.details

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.vicpin.krealmextensions.queryFirst
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.SpamAsset
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.data.model.remote.response.TransactionType
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

        val showTag = Constants.defaultAssets.any {
            it.assetId == transaction.assetId || transaction.assetId.isNullOrEmpty()
        }

        layout.image_history_type.setImageDrawable(transaction.transactionType().icon())
        layout.text_amount_value_in_dollar.gone()

        when (transaction.transactionType()) {
            TransactionType.SENT_TYPE -> {
                transaction.amount.notNull {
                    layout.text_amount_or_title.text =
                            "-${MoneyUtil.getScaledText(it, transaction.asset).stripZeros()}"
                }
            }
            TransactionType.RECEIVED_TYPE -> {
                transaction.amount.notNull {
                    layout.text_amount_or_title.text =
                            "+${MoneyUtil.getScaledText(it, transaction.asset).stripZeros()}"
                }
            }
            TransactionType.MASS_SPAM_RECEIVE_TYPE, TransactionType.MASS_SEND_TYPE,
            TransactionType.MASS_RECEIVE_TYPE -> {
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
                setExchangeItem(transaction, layout)
            }
            TransactionType.DATA_TYPE -> {
                layout.text_amount_or_title.text = layout.context.getString(
                        R.string.history_data_type_title)
            }
            TransactionType.CANCELED_LEASING_TYPE -> {
                transaction.lease?.amount.notNull {
                    layout.text_amount_or_title.text = MoneyUtil.getScaledText(
                            it, transaction.asset).stripZeros()
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
                            .substringAfter(".").clearBalance().toLong()
                    val amount = if (afterDot == 0L) {
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
                    layout.text_amount_or_title.text =
                            MoneyUtil.getScaledText(it, transaction.asset).stripZeros()
                }
            }
        }

        if (transaction.transactionType() != TransactionType.CREATE_ALIAS_TYPE
                && transaction.transactionType() != TransactionType.DATA_TYPE
                && transaction.transactionType() != TransactionType.SPAM_RECEIVE_TYPE
                && transaction.transactionType() != TransactionType.MASS_SPAM_RECEIVE_TYPE
                && transaction.transactionType() != TransactionType.EXCHANGE_TYPE) {
            if (showTag) {
                val ticker = transaction.asset?.getTicker()
                if (!ticker.isNullOrBlank()) {
                    layout.text_tag.text = ticker
                    layout.text_tag.visiable()
                }
            } else {
                layout.text_tag.gone()
                layout.text_amount_or_title.text = "${layout.text_amount_or_title.text}" +
                        " ${transaction.asset?.name}"
            }
        }

        if (queryFirst<SpamAsset> { equalTo("assetId", transaction.assetId) } != null) {
            if (prefsUtil.getValue(PrefsUtil.KEY_DISABLE_SPAM_FILTER, false)) {
                layout.text_tag_spam.gone()
            } else {
                layout.text_tag_spam.visiable()
            }
            layout.text_tag.gone()
            val sumString = MoneyUtil.getScaledText(
                    transaction.transfers.sumByLong { it.amount }, transaction.asset)
                    .trim()
                    .stripZeros()
            layout.text_amount_or_title.text = "$sumString ${transaction.asset?.name}"
        }


        layout.text_amount_or_title.makeTextHalfBold()

        collection.addView(layout)
        return layout
    }

    private fun setExchangeItem(transaction: Transaction, view: View) {
        val myOrder = findMyOrder(
                transaction.order1!!,
                transaction.order2!!,
                App.getAccessManager().getWallet()?.address!!)

        val directionSignPrice: String
        val directionSignAmount: String
        val amountAsset = myOrder.assetPair?.amountAssetObject!!
        val priceAsset = myOrder.assetPair?.priceAssetObject!!
        val amountValue = MoneyUtil.getScaledText(transaction.amount, amountAsset).stripZeros()
        val priceValue = MoneyUtil.getScaledText(
                transaction.amount.times(transaction.price).div(100000000),
                priceAsset).stripZeros()

        if (myOrder.orderType == Constants.SELL_ORDER_TYPE) {
            directionSignPrice = "+"
            directionSignAmount = "-"
        } else {
            directionSignPrice = "-"
            directionSignAmount = "+"
        }

        view.text_amount_value_in_dollar.visiable()
        view.text_amount_value_in_dollar.text =
                directionSignPrice + priceValue + " " + priceAsset.name

        val amountAssetTicker = if (amountAsset.name == "WAVES") {
            "WAVES"
        } else {
            amountAsset.ticker
        }

        val assetName = if (amountAssetTicker.isNullOrEmpty()) {
            view.text_tag.gone()
            " ${amountAsset.name}"
        } else {
            view.text_tag.visiable()
            view.text_tag.text = amountAssetTicker
            ""
        }

        view.text_amount_or_title.text = directionSignAmount + amountValue + assetName
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