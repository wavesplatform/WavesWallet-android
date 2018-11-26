package com.wavesplatform.wallet.v2.ui.home.wallet.assets.details.content

import android.content.Context
import android.support.v4.app.FragmentManager
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vicpin.krealmextensions.queryFirst
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.local.HistoryItem
import com.wavesplatform.wallet.v2.data.model.remote.response.SpamAsset
import com.wavesplatform.wallet.v2.data.model.remote.response.TransactionType
import com.wavesplatform.wallet.v2.injection.qualifier.ApplicationContext
import com.wavesplatform.wallet.v2.ui.home.history.details.HistoryDetailsBottomSheetFragment
import com.wavesplatform.wallet.v2.util.*
import kotlinx.android.synthetic.main.assets_detailed_history_item.view.*
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.visiable
import java.util.*
import javax.inject.Inject

class HistoryTransactionPagerAdapter @Inject constructor(
        @ApplicationContext var mContext: Context,
        var fragmentManager: FragmentManager?) : PagerAdapter() {
    var items: MutableList<HistoryItem> = arrayListOf()

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val layout = LayoutInflater.from(mContext).inflate(R.layout.assets_detailed_history_item, collection, false)
        val item = items[position]

        layout.card_transaction.click {
            val bottomSheetFragment = HistoryDetailsBottomSheetFragment()

            val allItems = items.asSequence()
                    .filter {
                        it.header.isEmpty()
                    }
                    .map { it.data }
                    .toList()

            bottomSheetFragment.configureData(item.data, position, allItems)
            bottomSheetFragment.show(fragmentManager, bottomSheetFragment.tag)
        }

        layout.image_transaction.setImageDrawable(item.data.transactionType()?.icon())

        var showTag = Constants.defaultAssets.any {
            it.assetId == item.data.assetId || item.data.assetId.isNullOrEmpty()
        }

        item.data.transactionType().notNull {
            try {
                layout.text_transaction_name.text =
                        mContext.getString(it.title)
            } catch (e: MissingFormatArgumentException) {
                layout.text_transaction_name.text = mContext.getString(it.title)
            }

            when (it) {
                TransactionType.SENT_TYPE -> {
                    item.data.amount.notNull {
                        layout.text_transaction_value.text = "-${MoneyUtil.getScaledText(it, item.data.asset)}"
                    }
                }
                TransactionType.RECEIVED_TYPE -> {
                    item.data.amount.notNull {
                        layout.text_transaction_value.text = "+${MoneyUtil.getScaledText(it, item.data.asset)}"
                    }
                }
                TransactionType.MASS_SPAM_RECEIVE_TYPE, TransactionType.MASS_SEND_TYPE, TransactionType.MASS_RECEIVE_TYPE -> {
                    if (item.data.transfers.isNotEmpty()) {
                        val sum = item.data.transfers.sumByLong { it.amount }
                        if (it == TransactionType.MASS_SPAM_RECEIVE_TYPE || it == TransactionType.MASS_RECEIVE_TYPE) {
                            layout.text_transaction_value.text = "+${MoneyUtil.getScaledText(sum.toLong(), item.data?.asset)}"
                        } else {
                            layout.text_transaction_value.text = "-${MoneyUtil.getScaledText(sum.toLong(), item.data?.asset)}"
                        }
                    }

                }
                TransactionType.CREATE_ALIAS_TYPE -> {
                    layout.text_transaction_value.text = item.data.alias
                }
                TransactionType.EXCHANGE_TYPE -> {
                    val myOrder =
                            if (item.data.order1?.sender == App.getAccessManager().getWallet()?.address) item.data.order1
                            else item.data.order2

                    val pairOrder =
                            if (item.data.order1?.sender != App.getAccessManager().getWallet()?.address) item.data.order1
                            else item.data.order2


                    if (myOrder?.orderType == Constants.SELL_ORDER_TYPE) {
                        layout.text_transaction_name.text = "-${MoneyUtil.getScaledText(item.data.amount, myOrder.assetPair?.amountAssetObject)} ${myOrder.assetPair?.amountAssetObject?.name}"
                        layout.text_transaction_value.text = "+${MoneyUtil.getScaledText(item.data.amount?.times(item.data.price!!)?.div(100000000), pairOrder?.assetPair?.priceAssetObject)}"
                    } else {
                        layout.text_transaction_name.text = "+${MoneyUtil.getScaledText(item.data.amount, myOrder?.assetPair?.amountAssetObject)} ${myOrder?.assetPair?.amountAssetObject?.name}"
                        layout.text_transaction_value.text = "-${MoneyUtil.getScaledText(item.data.amount?.times(item.data.price!!)?.div(100000000), pairOrder?.assetPair?.priceAssetObject)}"
                    }

                    showTag = Constants.defaultAssets.any {
                        it.assetId == pairOrder?.assetPair?.priceAssetObject?.id || pairOrder?.assetPair?.priceAssetObject?.id.isNullOrEmpty()
                    }

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
                    item.data.lease?.amount.notNull {
                        layout.text_transaction_value.text = MoneyUtil.getScaledText(it.toLong(), item.data.asset)
                    }
                }
                TransactionType.TOKEN_GENERATION_TYPE -> {
                    val quantity = MoneyUtil.getScaledText(
                            item.data.quantity, item.data.asset).substringBefore(".")
                    layout.text_transaction_value.text = quantity
                }
                TransactionType.TOKEN_BURN_TYPE -> {
                    item.data.amount.notNull {
                        val afterDot = MoneyUtil.getScaledText(it, item.data.asset)
                                .substringAfter(".").toInt()
                        val amount = if (afterDot == 0) MoneyUtil.getScaledText(it, item.data.asset)
                                .substringBefore(".")
                        else MoneyUtil.getScaledText(it, item.data.asset)

                        layout.text_transaction_value.text = "-$amount"
                    }
                }
                TransactionType.TOKEN_REISSUE_TYPE -> {
                    layout.text_transaction_value.text = "+${item.data.amount}"
                }
                else -> {
                    item.data.amount.notNull {
                        layout.text_transaction_value.text = MoneyUtil.getScaledText(it, item.data.asset)
                    }
                }
            }
        }

        if (item.data.transactionType() != TransactionType.CREATE_ALIAS_TYPE
                && item.data.transactionType() != TransactionType.DATA_TYPE
                && item.data.transactionType() != TransactionType.SPAM_RECEIVE_TYPE
                && item.data.transactionType() != TransactionType.MASS_SPAM_RECEIVE_TYPE
                && item.data.transactionType() != TransactionType.EXCHANGE_TYPE) {
            if (showTag) {
                layout.text_tag.visiable()
                layout.text_tag.text = item.data.asset?.name
            } else {
                layout.text_tag.gone()
                layout.text_transaction_value.text = "${layout.text_transaction_value.text} ${item.data.asset?.name}"
            }
        }

        if (queryFirst<SpamAsset> { equalTo("assetId", item.data.assetId) } != null) {
            layout.text_tag.gone()
            layout.text_tag_spam.visiable()
            layout.text_transaction_value.text = "${item.data.asset?.name}"
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