package com.wavesplatform.wallet.v2.ui.home.history

import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.helpers.PublicKeyAccountHelper
import com.wavesplatform.wallet.v2.data.model.remote.response.TransactionType
import com.wavesplatform.wallet.v2.util.*
import kotlinx.android.synthetic.main.recycle_item_history.view.*
import pers.victor.ext.gone
import pers.victor.ext.visiable
import java.util.*
import javax.inject.Inject

class HistoryItemAdapter @Inject constructor(var publicKeyAccountHelper: PublicKeyAccountHelper) :
        BaseSectionQuickAdapter<HistoryItem, BaseViewHolder>(R.layout.recycle_item_history, R.layout.asset_header, null) {

    override fun convertHead(helper: BaseViewHolder?, item: HistoryItem?) {
        helper?.setText(R.id.text_header_text, item?.header)
    }

    override fun convert(helper: BaseViewHolder?, item: HistoryItem?) {
        helper?.itemView.notNull { view ->
            view.image_transaction.setImageDrawable(item?.t?.transactionType()?.icon())

            var showTag = Constants.defaultAssets.any {
                it.assetId == item?.t?.assetId || item?.t?.assetId.isNullOrEmpty()
            }

            item?.t?.transactionType().notNull {
                try {
                    view.text_transaction_name.text = String.format(it.title(), item?.t?.asset?.issueTransaction?.name)
                } catch (e: MissingFormatArgumentException) {
                    view.text_transaction_name.text = it.title()
                }

                when (it) {
                    TransactionType.SENT_TYPE -> {
                        item?.t?.amount.notNull {
                            view.text_transaction_value.text = "-${MoneyUtil.getScaledText(it, item?.t?.asset)}"
                        }
                    }
                    TransactionType.RECEIVED_TYPE -> {
                        item?.t?.amount.notNull {
                            view.text_transaction_value.text = "+${MoneyUtil.getScaledText(it, item?.t?.asset)}"
                        }
                    }
                    TransactionType.MASS_SPAM_RECEIVE_TYPE, TransactionType.MASS_SEND_TYPE, TransactionType.MASS_RECEIVE_TYPE -> {
                        if (item?.t?.transfers != null && item.t?.transfers!!.isNotEmpty()) {
                            val sum = item.t.transfers.sumByLong { it.amount }
                            if (it == TransactionType.MASS_SPAM_RECEIVE_TYPE || it == TransactionType.MASS_RECEIVE_TYPE) {
                                view.text_transaction_value.text = "+${MoneyUtil.getScaledText(sum.toLong(), item.t?.asset)}"
                            } else {
                                view.text_transaction_value.text = "-${MoneyUtil.getScaledText(sum.toLong(), item.t?.asset)}"
                            }
                        }

                    }
                    TransactionType.CREATE_ALIAS_TYPE -> {
                        view.text_transaction_value.text = item?.t?.alias
                    }
                    TransactionType.EXCHANGE_TYPE -> {
                        val myOrder =
                                if (item?.t?.order1?.sender == publicKeyAccountHelper.publicKeyAccount?.address) item?.t?.order1
                                else item?.t?.order2

                        val pairOrder =
                                if (item?.t?.order1?.sender != publicKeyAccountHelper.publicKeyAccount?.address) item?.t?.order1
                                else item?.t?.order2


                        if (myOrder?.orderType == Constants.SELL_ORDER_TYPE) {
                            view.text_transaction_name.text = "-${MoneyUtil.getScaledText(item?.t?.amount, myOrder.assetPair?.amountAssetObject)} ${myOrder.assetPair?.amountAssetObject?.issueTransaction?.name}"
                            view.text_transaction_value.text = "+${MoneyUtil.getScaledText(item?.t?.amount?.times(item?.t?.price!!)?.div(100000000), pairOrder?.assetPair?.priceAssetObject)}"
                        } else {
                            view.text_transaction_name.text = "+${MoneyUtil.getScaledText(item?.t?.amount, myOrder?.assetPair?.amountAssetObject)} ${myOrder?.assetPair?.amountAssetObject?.issueTransaction?.name}"
                            view.text_transaction_value.text = "-${MoneyUtil.getScaledText(item?.t?.amount?.times(item?.t?.price!!)?.div(100000000), pairOrder?.assetPair?.priceAssetObject)}"
                        }

                        showTag = Constants.defaultAssets.any {
                            it.assetId == pairOrder?.assetPair?.priceAssetObject?.assetId || pairOrder?.assetPair?.priceAssetObject?.assetId.isNullOrEmpty()
                        }

                        if (showTag) {
                            view.text_tag.visiable()
                            view.text_tag.text = pairOrder?.assetPair?.priceAssetObject?.issueTransaction?.name
                        } else {
                            view.text_tag.gone()
                            view.text_transaction_value.text = "${view.text_transaction_value.text} ${pairOrder?.assetPair?.priceAssetObject?.issueTransaction?.name}"
                        }
                    }
                    TransactionType.DATA_TYPE -> {
                        view.text_transaction_value.text = mContext.getString(R.string.history_data_type_title)
                    }
                    TransactionType.CANCELED_LEASING_TYPE -> {
                        item?.t?.lease?.amount.notNull {
                            view.text_transaction_value.text = MoneyUtil.getScaledText(it.toLong(), item?.t?.asset)
                        }
                    }
                    TransactionType.TOKEN_GENERATION_TYPE -> {
                        val quantity = MoneyUtil.getScaledText(item?.t?.quantity
                                ?: 0, item?.t?.asset).substringBefore(".")
                        view.text_transaction_value.text = quantity
                    }
                    TransactionType.TOKEN_BURN_TYPE -> {
                        item?.t?.amount.notNull {
                            val afterDot = MoneyUtil.getScaledText(it, item?.t?.asset).substringAfter(".").toInt()
                            var amount = ""

                            if (afterDot == 0) amount = MoneyUtil.getScaledText(it, item?.t?.asset).substringBefore(".")
                            else amount = MoneyUtil.getScaledText(it, item?.t?.asset)

                            view.text_transaction_value.text = "-$amount"
                        }
                    }
                    TransactionType.TOKEN_REISSUE_TYPE -> {
                        view.text_transaction_value.text = "+${item?.t?.amount}"
                    }
                    else -> {
                        item?.t?.amount.notNull {
                            view.text_transaction_value.text = MoneyUtil.getScaledText(it, item?.t?.asset)
                        }
                    }
                }
            }

            if (item?.t?.transactionType() != TransactionType.CREATE_ALIAS_TYPE && item?.t?.transactionType() != TransactionType.DATA_TYPE
                    && item?.t?.transactionType() != TransactionType.SPAM_RECEIVE_TYPE && item?.t?.transactionType() != TransactionType.MASS_SPAM_RECEIVE_TYPE
                    && item?.t?.transactionType() != TransactionType.EXCHANGE_TYPE) {
                if (showTag) {
                    view.text_tag.visiable()
                    view.text_tag.text = item?.t?.asset?.issueTransaction?.name
                } else {
                    view.text_tag.gone()
                    view.text_transaction_value.text = "${view.text_transaction_value.text} ${item?.t?.asset?.issueTransaction?.name}"
                }
            } else if (item.t?.transactionType() == TransactionType.SPAM_RECEIVE_TYPE || item.t?.transactionType() == TransactionType.MASS_SPAM_RECEIVE_TYPE) {
                view.text_tag.gone()
                view.text_tag_spam.visiable()
                view.text_transaction_value.text = "${view.text_transaction_value.text} ${item.t?.asset?.issueTransaction?.name}"
            }

            view.text_transaction_value.makeTextHalfBold()
        }
    }
}
