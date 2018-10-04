package com.wavesplatform.wallet.v2.ui.home.history

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.TransactionType
import com.wavesplatform.wallet.v2.util.*
import kotlinx.android.synthetic.main.recycle_item_history.view.*
import pers.victor.ext.gone
import pers.victor.ext.visiable
import java.util.*
import javax.inject.Inject
import com.oushangfeng.pinnedsectionitemdecoration.utils.FullSpanUtil
import android.support.v7.widget.RecyclerView
import pers.victor.ext.dp2px


class HistoryItemAdapter @Inject constructor() :
        BaseMultiItemQuickAdapter<HistoryItem, BaseViewHolder>(null) {

    init {
        addItemType(HistoryItem.TYPE_HEADER, R.layout.asset_header)
        addItemType(HistoryItem.TYPE_DATA, R.layout.recycle_item_history)
        addItemType(HistoryItem.TYPE_EMPTY, R.layout.layout_history_tab_header_space)
    }


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        FullSpanUtil.onAttachedToRecyclerView(recyclerView, this, HistoryItem.TYPE_HEADER)
    }

    override fun onViewAttachedToWindow(holder: BaseViewHolder) {
        super.onViewAttachedToWindow(holder)
        FullSpanUtil.onViewAttachedToWindow(holder, this, HistoryItem.TYPE_HEADER)
    }

    override fun convert(helper: BaseViewHolder, item: HistoryItem) {
        val currentPosition = data.indexOf(item)
        when (item.itemType) {
            HistoryItem.TYPE_HEADER -> {
                helper.setText(R.id.text_header_text, item?.header)
            }
            HistoryItem.TYPE_DATA -> {
                try {
                    val param = helper.itemView.card_history.layoutParams as RecyclerView.LayoutParams

                    if (data[currentPosition + 1].header.isNotEmpty()) {
                        helper.itemView.card_history.layoutParams = param
                        param.bottomMargin = dp2px(18)
                    } else {
                        param.bottomMargin = dp2px(8)
                    }
                    helper.itemView.card_history.layoutParams = param
                } catch (e: Throwable) {
                    e.printStackTrace()
                }

                helper?.itemView.notNull { view ->
                    view.image_transaction.setImageDrawable(item.data.transactionType()?.icon())

                    var showTag = Constants.defaultAssets.any {
                        it.assetId == item.data.assetId || item.data.assetId.isNullOrEmpty()
                    }

                    view.text_tag_spam.gone()

                    item.data.transactionType().notNull {
                        try {
                            view.text_transaction_name.text = String.format(it.title(), item.data.asset?.name)
                        } catch (e: MissingFormatArgumentException) {
                            view.text_transaction_name.text = it.title()
                        }

                        when (it) {
                            TransactionType.SENT_TYPE -> {
                                item.data.amount.notNull {
                                    view.text_transaction_value.text = "-${MoneyUtil.getScaledText(it, item.data.asset)}"
                                }
                            }
                            TransactionType.RECEIVED_TYPE -> {
                                item.data.amount.notNull {
                                    view.text_transaction_value.text = "+${MoneyUtil.getScaledText(it, item.data.asset)}"
                                }
                            }
                            TransactionType.MASS_SPAM_RECEIVE_TYPE, TransactionType.MASS_SEND_TYPE, TransactionType.MASS_RECEIVE_TYPE -> {
                                if (item.data.transfers != null && item.data.transfers!!.isNotEmpty()) {
                                    val sum = item.data.transfers.sumByLong { it.amount }
                                    if (it == TransactionType.MASS_SPAM_RECEIVE_TYPE || it == TransactionType.MASS_RECEIVE_TYPE) {
                                        view.text_transaction_value.text = "+${MoneyUtil.getScaledText(sum.toLong(), item.data.asset)}"
                                    } else {
                                        view.text_transaction_value.text = "-${MoneyUtil.getScaledText(sum.toLong(), item.data.asset)}"
                                    }
                                }

                            }
                            TransactionType.CREATE_ALIAS_TYPE -> {
                                view.text_transaction_value.text = item.data.alias
                            }
                            TransactionType.EXCHANGE_TYPE -> {
                                val myOrder =
                                        if (item.data.order1?.sender == App.getAccessManager().getWallet()?.address) item.data.order1
                                        else item.data.order2

                                val pairOrder =
                                        if (item.data.order1?.sender != App.getAccessManager().getWallet()?.address) item.data.order1
                                        else item.data.order2


                                if (myOrder?.orderType == Constants.SELL_ORDER_TYPE) {
                                    view.text_transaction_name.text = "-${MoneyUtil.getScaledText(item.data.amount, myOrder.assetPair?.amountAssetObject)} ${myOrder.assetPair?.amountAssetObject?.name}"
                                    view.text_transaction_value.text = "+${MoneyUtil.getScaledText(item.data.amount?.times(item.data.price!!)?.div(100000000), pairOrder?.assetPair?.priceAssetObject)}"
                                } else {
                                    view.text_transaction_name.text = "+${MoneyUtil.getScaledText(item.data.amount, myOrder?.assetPair?.amountAssetObject)} ${myOrder?.assetPair?.amountAssetObject?.name}"
                                    view.text_transaction_value.text = "-${MoneyUtil.getScaledText(item.data.amount?.times(item.data.price!!)?.div(100000000), pairOrder?.assetPair?.priceAssetObject)}"
                                }

                                showTag = Constants.defaultAssets.any {
                                    it.assetId == pairOrder?.assetPair?.priceAssetObject?.id || pairOrder?.assetPair?.priceAssetObject?.id.isNullOrEmpty()
                                }

                                if (showTag) {
                                    view.text_tag.visiable()
                                    view.text_tag.text = pairOrder?.assetPair?.priceAssetObject?.name
                                } else {
                                    view.text_tag.gone()
                                    view.text_transaction_value.text = "${view.text_transaction_value.text} ${pairOrder?.assetPair?.priceAssetObject?.name}"
                                }
                            }
                            TransactionType.DATA_TYPE -> {
                                view.text_transaction_value.text = mContext.getString(R.string.history_data_type_title)
                            }
                            TransactionType.CANCELED_LEASING_TYPE -> {
                                item.data.lease?.amount.notNull {
                                    view.text_transaction_value.text = MoneyUtil.getScaledText(it.toLong(), item.data.asset)
                                }
                            }
                            TransactionType.TOKEN_GENERATION_TYPE -> {
                                val quantity = MoneyUtil.getScaledText(item.data.quantity
                                        ?: 0, item.data.asset).substringBefore(".")
                                view.text_transaction_value.text = quantity
                            }
                            TransactionType.TOKEN_BURN_TYPE -> {
                                item.data.amount.notNull {
                                    val afterDot = MoneyUtil.getScaledText(it, item.data.asset).substringAfter(".").toInt()
                                    var amount = ""

                                    if (afterDot == 0) amount = MoneyUtil.getScaledText(it, item.data.asset).substringBefore(".")
                                    else amount = MoneyUtil.getScaledText(it, item.data.asset)

                                    view.text_transaction_value.text = "-$amount"
                                }
                            }
                            TransactionType.TOKEN_REISSUE_TYPE -> {
                                view.text_transaction_value.text = "+${item.data.amount}"
                            }
                            else -> {
                                item.data.amount.notNull {
                                    view.text_transaction_value.text = MoneyUtil.getScaledText(it, item.data.asset)
                                }
                            }
                        }
                    }

                    if (item.data.transactionType() != TransactionType.CREATE_ALIAS_TYPE && item.data.transactionType() != TransactionType.DATA_TYPE
                            && item.data.transactionType() != TransactionType.SPAM_RECEIVE_TYPE && item.data.transactionType() != TransactionType.MASS_SPAM_RECEIVE_TYPE
                            && item.data.transactionType() != TransactionType.EXCHANGE_TYPE) {
                        if (showTag) {
                            view.text_tag.visiable()
                            view.text_tag.text = item.data.asset?.name
                        } else {
                            view.text_tag.gone()
                            view.text_transaction_value.text = "${view.text_transaction_value.text} ${item.data.asset?.name}"
                        }
                    } else if (item.data.transactionType() == TransactionType.SPAM_RECEIVE_TYPE || item.data.transactionType() == TransactionType.MASS_SPAM_RECEIVE_TYPE) {
                        view.text_tag.gone()
                        view.text_tag_spam.visiable()
                        view.text_transaction_value.text = "${view.text_transaction_value.text} ${item.data.asset?.name}"
                    }

                    view.text_transaction_value.makeTextHalfBold()
                }
            }
        }

    }
}
