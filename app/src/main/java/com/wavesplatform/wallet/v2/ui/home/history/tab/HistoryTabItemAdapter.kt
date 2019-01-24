package com.wavesplatform.wallet.v2.ui.home.history.tab

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.View
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.oushangfeng.pinnedsectionitemdecoration.utils.FullSpanUtil
import com.vicpin.krealmextensions.queryFirst
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.local.HistoryItem
import com.wavesplatform.wallet.v2.data.model.remote.response.SpamAsset
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.data.model.remote.response.TransactionType
import com.wavesplatform.wallet.v2.util.*
import kotlinx.android.synthetic.main.recycle_item_history.view.*
import pers.victor.ext.dp2px
import pers.victor.ext.gone
import pers.victor.ext.visiable
import java.util.*
import javax.inject.Inject


class HistoryTabItemAdapter @Inject constructor() :
        BaseMultiItemQuickAdapter<HistoryItem, BaseViewHolder>(null) {

    @Inject
    lateinit var prefsUtil: PrefsUtil

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

    @SuppressLint("SetTextI18n")
    override fun convert(helper: BaseViewHolder, item: HistoryItem) {
        when (item.itemType) {
            HistoryItem.TYPE_HEADER -> {
                helper.setText(R.id.text_header_text, item.header)
            }
            HistoryItem.TYPE_DATA -> {
                try {
                    if (data[helper.adapterPosition + 1].header.isNotEmpty()) {
                        helper.itemView.card_history.setMargins(bottom = dp2px(18))
                    } else {
                        helper.itemView.card_history.setMargins(bottom = dp2px(8))
                    }
                } catch (e: Throwable) {
                    helper.itemView.card_history.setMargins(bottom = dp2px(8))
                    e.printStackTrace()
                }

                helper.itemView.notNull { view ->
                    val spam = null != queryFirst<SpamAsset> {
                        equalTo("assetId", item.data.assetId)
                    }

                    val decimals = item.data.asset?.precision ?: 8

                    view.image_transaction.setImageDrawable(item.data.transactionType().icon())

                    val showTag = Constants.defaultAssets.any {
                        it.assetId == item.data.assetId || item.data.assetId.isNullOrEmpty()
                    }

                    view.text_tag_spam.gone()

                    item.data.transactionType().notNull {
                        try {
                            val name = if (spam) {
                                ""
                            } else {
                                item.data.asset?.name
                            }
                            view.text_transaction_name.text = String.format(
                                    mContext.getString(it.title), name).trim()
                        } catch (e: MissingFormatArgumentException) {
                            view.text_transaction_name.text = mContext.getString(it.title)
                        }

                        when (it) {
                            TransactionType.SENT_TYPE -> {
                                item.data.amount.notNull {
                                    view.text_transaction_value.text =
                                            "-${getScaledAmount(it, decimals)}"
                                }
                            }
                            TransactionType.RECEIVED_TYPE -> {
                                item.data.amount.notNull {
                                    view.text_transaction_value.text =
                                            "+${getScaledAmount(it, decimals)}"
                                }
                            }
                            TransactionType.MASS_SPAM_RECEIVE_TYPE,
                            TransactionType.MASS_SEND_TYPE,
                            TransactionType.MASS_RECEIVE_TYPE -> {
                                if (item.data.transfers.isNotEmpty()) {
                                    val sumString = getScaledAmount(
                                            item.data.transfers.sumByLong { it.amount }, decimals)
                                    if (sumString.isEmpty()) {
                                        view.text_transaction_value.text = ""
                                    } else {
                                        view.text_transaction_value.text = sumString
                                    }
                                } else {
                                    view.text_transaction_value.text =
                                            getScaledAmount(item.data.amount, decimals)
                                }
                            }
                            TransactionType.CREATE_ALIAS_TYPE -> {
                                view.text_transaction_value.text = item.data.alias
                            }
                            TransactionType.EXCHANGE_TYPE -> {
                                setExchangeItem(item.data, view)
                            }
                            TransactionType.DATA_TYPE -> {
                                view.text_transaction_value.text = mContext
                                        .getString(R.string.history_data_type_title)
                            }
                            TransactionType.CANCELED_LEASING_TYPE -> {
                                item.data.lease?.amount.notNull {
                                    view.text_transaction_value.text =
                                            getScaledAmount(it, decimals)
                                }
                            }
                            TransactionType.TOKEN_GENERATION_TYPE -> {
                                val quantity = getScaledAmount(
                                        item.data.quantity, decimals)
                                view.text_transaction_value.text = quantity
                            }
                            TransactionType.TOKEN_BURN_TYPE -> {
                                item.data.amount.notNull {
                                    view.text_transaction_value.text =
                                            "-${getScaledAmount(it, decimals)}"
                                }
                            }
                            TransactionType.TOKEN_REISSUE_TYPE -> {
                                val quantity = getScaledAmount(
                                        item.data.quantity, decimals)
                                view.text_transaction_value.text = "+$quantity"
                            }
                            TransactionType.SET_SCRIPT_TYPE,
                            TransactionType.CANCEL_SCRIPT_TYPE,
                            TransactionType.SET_SPONSORSHIP_TYPE,
                            TransactionType.CANCEL_SPONSORSHIP_TYPE,
                            TransactionType.ASSET_SCRIPT_TYPE -> {
                                view.text_transaction_name.text =
                                        mContext.getString(R.string.history_data_type_title)
                                view.text_transaction_value.text = mContext.getString(
                                        item.data.transactionType().title)
                                view.text_tag.gone()
                            }
                            else -> {
                                item.data.amount.notNull {
                                    view.text_transaction_value.text =
                                            getScaledAmount(it, decimals)
                                }
                            }
                        }
                    }

                    if (spam) {
                        if (item.data.transfers.isNotEmpty()) {
                            val sumString = getScaledAmount(
                                    item.data.transfers.sumByLong { it.amount }, decimals)
                            if (sumString.isEmpty()) {
                                view.text_transaction_value.text = ""
                            } else {
                                view.text_transaction_value.text = sumString
                            }
                        } else {
                            view.text_transaction_value.text =
                                    getScaledAmount(item.data.amount, decimals)
                        }
                        if (prefsUtil.getValue(PrefsUtil.KEY_ENABLE_SPAM_FILTER, false)) {
                            view.text_tag_spam.visiable()
                        } else {
                            view.text_tag_spam.gone()
                        }
                        view.text_tag.gone()
                    } else {
                        if (item.data.transactionType() != TransactionType.CREATE_ALIAS_TYPE
                                && item.data.transactionType() != TransactionType.DATA_TYPE
                                && item.data.transactionType() != TransactionType.SPAM_RECEIVE_TYPE
                                && item.data.transactionType() != TransactionType.MASS_SPAM_RECEIVE_TYPE
                                && item.data.transactionType() != TransactionType.EXCHANGE_TYPE
                                && item.data.transactionType() != TransactionType.SET_SCRIPT_TYPE
                                && item.data.transactionType() != TransactionType.CANCEL_SCRIPT_TYPE
                                && item.data.transactionType() != TransactionType.SET_SPONSORSHIP_TYPE
                                && item.data.transactionType() != TransactionType.CANCEL_SPONSORSHIP_TYPE
                                && item.data.transactionType() != TransactionType.ASSET_SCRIPT_TYPE) {
                            if (showTag) {
                                val ticker = item.data.asset?.getTicker()
                                if (!ticker.isNullOrBlank()) {
                                    view.text_tag.text = ticker
                                    view.text_tag.visiable()
                                }
                            } else {
                                view.text_tag.gone()
                                view.text_transaction_value.text =
                                        "${view.text_transaction_value.text} ${item.data.asset?.name}"
                            }
                            view.text_transaction_value.makeTextHalfBold()
                        }
                    }
                }
            }
        }
    }

    private fun setExchangeItem(transaction: Transaction, view: View) {
        val myOrder = findMyOrder(
                transaction.order1!!,
                transaction.order2!!,
                App.getAccessManager().getWallet()?.address)
        val secondOrder = if (myOrder.id == transaction.order1!!.id) {
            transaction.order2!!
        } else {
            transaction.order1!!
        }

        val directionStringResId: Int
        val directionSign: String
        val amountAsset = myOrder.assetPair?.amountAssetObject!!
        val amountValue = MoneyUtil.getScaledText(transaction.amount, amountAsset).stripZeros()

        if (myOrder.orderType == Constants.SELL_ORDER_TYPE) {
            directionStringResId = R.string.history_my_dex_intent_sell
            directionSign = "-"
        } else {
            directionStringResId = R.string.history_my_dex_intent_buy
            directionSign = "+"
        }

        view.text_transaction_name.text = mContext.getString(
                directionStringResId,
                amountAsset.name,
                secondOrder.assetPair?.priceAssetObject?.name)

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

        view.text_transaction_value.text = directionSign + amountValue + assetName
    }
}
