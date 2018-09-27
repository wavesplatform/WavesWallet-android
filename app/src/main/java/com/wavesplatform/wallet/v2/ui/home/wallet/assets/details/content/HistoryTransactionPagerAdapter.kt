package com.wavesplatform.wallet.v2.ui.home.wallet.assets.details.content

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.TransactionType
import com.wavesplatform.wallet.v2.injection.qualifier.ApplicationContext
import com.wavesplatform.wallet.v2.ui.home.history.HistoryItem
import com.wavesplatform.wallet.v2.util.*
import kotlinx.android.synthetic.main.assets_detailed_history_item.view.*
import pers.victor.ext.gone
import pers.victor.ext.visiable
import java.util.*
import javax.inject.Inject

class HistoryTransactionPagerAdapter @Inject constructor(@ApplicationContext var mContext: Context) : PagerAdapter() {
    var items: MutableList<HistoryItem> = arrayListOf()

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val layout = LayoutInflater.from(mContext).inflate(R.layout.assets_detailed_history_item, collection, false)
        val item = items[position]

        layout.image_transaction.setImageDrawable(item?.t?.transactionType()?.icon())

        var showTag = Constants.defaultAssets.any({
            it.assetId == item?.t?.assetId || item?.t?.assetId.isNullOrEmpty()
        })

        item?.t?.transactionType().notNull {
            try {
                layout.text_transaction_name.text = String.format(it.title(), item?.t?.asset?.name)
            } catch (e: MissingFormatArgumentException) {
                layout.text_transaction_name.text = it.title()
            }

            when (it) {
                TransactionType.SENT_TYPE -> {
                    item?.t?.amount.notNull {
                        layout.text_transaction_value.text = "-${MoneyUtil.getScaledText(it, item?.t?.asset)}"
                    }
                }
                TransactionType.RECEIVED_TYPE -> {
                    item?.t?.amount.notNull {
                        layout.text_transaction_value.text = "+${MoneyUtil.getScaledText(it, item?.t?.asset)}"
                    }
                }
                TransactionType.MASS_SPAM_RECEIVE_TYPE, TransactionType.MASS_SEND_TYPE, TransactionType.MASS_RECEIVE_TYPE -> {
                    if (item?.t?.transfers != null && item.t?.transfers!!.isNotEmpty()) {
                        val sum = item.t.transfers.sumByLong { it.amount }
                        if (it == TransactionType.MASS_SPAM_RECEIVE_TYPE || it == TransactionType.MASS_RECEIVE_TYPE) {
                            layout.text_transaction_value.text = "+${MoneyUtil.getScaledText(sum.toLong(), item.t?.asset)}"
                        } else {
                            layout.text_transaction_value.text = "-${MoneyUtil.getScaledText(sum.toLong(), item.t?.asset)}"
                        }
                    }

                }
                TransactionType.CREATE_ALIAS_TYPE -> {
                    layout.text_transaction_value.text = item?.t?.alias
                }
                TransactionType.EXCHANGE_TYPE -> {
                    var myOrder =
                            if (item?.t?.order1?.sender == App.getAccessManager().getWallet()?.address) item?.t?.order1
                            else item?.t?.order2

                    var pairOrder =
                            if (item?.t?.order1?.sender != App.getAccessManager().getWallet()?.address) item?.t?.order1
                            else item?.t?.order2


                    if (myOrder?.orderType == Constants.SELL_ORDER_TYPE) {
                        layout.text_transaction_name.text = "-${MoneyUtil.getScaledText(item?.t?.amount, myOrder.assetPair?.amountAssetObject)} ${myOrder.assetPair?.amountAssetObject?.name}"
                        layout.text_transaction_value.text = "+${MoneyUtil.getScaledText(item?.t?.amount?.times(item?.t?.price!!)?.div(100000000), pairOrder?.assetPair?.priceAssetObject)}"
                    } else {
                        layout.text_transaction_name.text = "+${MoneyUtil.getScaledText(item?.t?.amount, myOrder?.assetPair?.amountAssetObject)} ${myOrder?.assetPair?.amountAssetObject?.name}"
                        layout.text_transaction_value.text = "-${MoneyUtil.getScaledText(item?.t?.amount?.times(item?.t?.price!!)?.div(100000000), pairOrder?.assetPair?.priceAssetObject)}"
                    }

                    showTag = Constants.defaultAssets.any({
                        it.assetId == pairOrder?.assetPair?.priceAssetObject?.id || pairOrder?.assetPair?.priceAssetObject?.id.isNullOrEmpty()
                    })

                    if (showTag) {
                        layout.text_tag.visiable()
                        layout.text_tag.text = pairOrder?.assetPair?.priceAssetObject?.name
                    } else {
                        layout.text_tag.gone()
                        layout.text_transaction_value.text = "${layout.text_transaction_value.text} ${pairOrder?.assetPair?.priceAssetObject?.name}"
                    }
                }
                TransactionType.DATA_TYPE -> {
                    layout.text_transaction_value.text = mContext.getString(R.string.history_data_type_title)
                }
                TransactionType.CANCELED_LEASING_TYPE -> {
                    item?.t?.lease?.amount.notNull {
                        layout.text_transaction_value.text = MoneyUtil.getScaledText(it.toLong(), item?.t?.asset)
                    }
                }
                TransactionType.TOKEN_GENERATION_TYPE -> {
                    val quantity = MoneyUtil.getScaledText(item?.t?.quantity
                            ?: 0, item?.t?.asset).substringBefore(".")
                    layout.text_transaction_value.text = quantity
                }
                TransactionType.TOKEN_BURN_TYPE -> {
                    item?.t?.amount.notNull {
                        val afterDot = MoneyUtil.getScaledText(it, item?.t?.asset).substringAfter(".").toInt()
                        var amount = ""

                        if (afterDot == 0) amount = MoneyUtil.getScaledText(it, item?.t?.asset).substringBefore(".")
                        else amount = MoneyUtil.getScaledText(it, item?.t?.asset)

                        layout.text_transaction_value.text = "-$amount"
                    }
                }
                TransactionType.TOKEN_REISSUE_TYPE -> {
                    layout.text_transaction_value.text = "+${item?.t?.amount}"
                }
                else -> {
                    item?.t?.amount.notNull {
                        layout.text_transaction_value.text = MoneyUtil.getScaledText(it, item?.t?.asset)
                    }
                }
            }
        }

        if (item?.t?.transactionType() != TransactionType.CREATE_ALIAS_TYPE && item?.t?.transactionType() != TransactionType.DATA_TYPE
                && item?.t?.transactionType() != TransactionType.SPAM_RECEIVE_TYPE && item?.t?.transactionType() != TransactionType.MASS_SPAM_RECEIVE_TYPE
                && item?.t?.transactionType() != TransactionType.EXCHANGE_TYPE) {
            if (showTag) {
                layout.text_tag.visiable()
                layout.text_tag.text = item?.t?.asset?.name
            } else {
                layout.text_tag.gone()
                layout.text_transaction_value.text = "${layout.text_transaction_value.text} ${item?.t?.asset?.name}"
            }
        } else if (item?.t?.transactionType() == TransactionType.SPAM_RECEIVE_TYPE || item?.t?.transactionType() == TransactionType.MASS_SPAM_RECEIVE_TYPE) {
            layout.text_tag.gone()
            layout.text_tag_spam.visiable()
            layout.text_transaction_value.text = "${layout.text_transaction_value.text} ${item?.t?.asset?.name}"
        }


        layout.text_transaction_value.makeTextHalfBold()


        collection.addView(layout)
        return layout
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getPageWidth(position: Int): Float {
        return 1f
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }
}