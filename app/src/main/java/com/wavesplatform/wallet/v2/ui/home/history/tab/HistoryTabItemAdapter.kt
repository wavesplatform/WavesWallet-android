package com.wavesplatform.wallet.v2.ui.home.history.tab

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.support.v4.widget.TextViewCompat
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.View
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.oushangfeng.pinnedsectionitemdecoration.utils.FullSpanUtil
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.local.HistoryItem
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.data.model.remote.response.TransactionType
import com.wavesplatform.wallet.v2.util.*
import com.wavesplatform.wallet.v2.util.TransactionUtil.Companion.getTransactionAmount
import kotlinx.android.synthetic.main.recycle_item_history.view.*
import pers.victor.ext.dp2px
import pers.victor.ext.gone
import pers.victor.ext.visiable
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
                }

                helper.itemView.notNull { view ->
                    TextViewCompat.setAutoSizeTextTypeWithDefaults(view.text_transaction_value,
                            TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE)
                    view.text_transaction_value.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    view.text_tag.gone()
                    view.text_tag_spam.gone()
                    view.text_transaction_value.setTypeface(null, Typeface.NORMAL)
                    val decimals = item.data.asset?.precision ?: 8

                    item.data.transactionType().notNull {
                        view.image_transaction.setImageDrawable(it.icon())
                        view.text_transaction_name.text = mContext.getString(it.title)
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
                            TransactionType.RECEIVE_SPONSORSHIP_TYPE -> {
                                item.data.fee.notNull {
                                    view.text_transaction_value.text =
                                            "+${getScaledAmount(it, item.data.feeAssetObject?.precision
                                                    ?: 8)} ${item.data.feeAssetObject?.name}"
                                }
                            }
                            TransactionType.MASS_SPAM_RECEIVE_TYPE,
                            TransactionType.MASS_RECEIVE_TYPE,
                            TransactionType.MASS_SEND_TYPE -> {
                                view.text_transaction_value.text = getTransactionAmount(
                                        transaction = item.data, decimals = decimals)
                            }
                            TransactionType.CREATE_ALIAS_TYPE -> {
                                view.text_transaction_value.text = item.data.alias
                                view.text_transaction_value.setTypeface(null, Typeface.BOLD)
                            }
                            TransactionType.EXCHANGE_TYPE -> {
                                setExchangeItem(item.data, view)
                            }
                            TransactionType.CANCELED_LEASING_TYPE -> {
                                item.data.lease?.amount.notNull {
                                    view.text_transaction_value.text = getScaledAmount(it, decimals)
                                }
                            }
                            TransactionType.TOKEN_BURN_TYPE -> {
                                item.data.amount.notNull {
                                    view.text_transaction_value.text =
                                            "-${getScaledAmount(it, decimals)}"
                                }
                            }
                            TransactionType.TOKEN_GENERATION_TYPE,
                            TransactionType.TOKEN_REISSUE_TYPE -> {
                                val quantity = getScaledAmount(item.data.quantity, decimals)
                                view.text_transaction_value.text = "+$quantity"
                            }
                            TransactionType.DATA_TYPE,
                            TransactionType.SET_ADDRESS_SCRIPT_TYPE,
                            TransactionType.CANCEL_ADDRESS_SCRIPT_TYPE,
                            TransactionType.UPDATE_ASSET_SCRIPT_TYPE -> {
                                view.text_transaction_name.text =
                                        mContext.getString(R.string.history_data_type_title)
                                view.text_transaction_value.text = mContext.getString(
                                        item.data.transactionType().title)
                                view.text_transaction_value.setTypeface(null, Typeface.BOLD)
                            }
                            TransactionType.SET_SPONSORSHIP_TYPE,
                            TransactionType.CANCEL_SPONSORSHIP_TYPE -> {
                                view.text_transaction_value.text = item.data.asset?.name
                            }
                            else -> {
                                item.data.amount.notNull {
                                    view.text_transaction_value.text = getScaledAmount(it, decimals)
                                }
                            }
                        }
                    }

                    if (!TransactionType.isZeroTransferOrExchange(item.data.transactionType())) {
                        if (isSpamConsidered(item.data.assetId, prefsUtil)) {
                            view.text_tag_spam.visiable()
                        } else {
                            if (isShowTicker(item.data.assetId)) {
                                val ticker = item.data.asset?.getTicker()
                                if (!ticker.isNullOrBlank()) {
                                    view.text_tag.text = ticker
                                    view.text_tag.visiable()
                                }
                            } else {
                                view.text_transaction_value.text =
                                        "${view.text_transaction_value.text} ${item.data.asset?.name}"
                            }
                        }
                    }

                    view.text_transaction_value.makeTextHalfBold()
                    view.text_transaction_value.post {
                        TextViewCompat
                                .setAutoSizeTextTypeUniformWithConfiguration(view.text_transaction_value,
                                        10, 16, 1, TypedValue.COMPLEX_UNIT_SP)
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
        val amountValue = getScaledAmount(transaction.amount,
                amountAsset.precision)

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

        val amountAssetTicker = if (amountAsset.name == Constants.WAVES_ASSET_ID_FILLED) {
            Constants.WAVES_ASSET_ID_FILLED
        } else {
            amountAsset.ticker
        }

        val assetName = if (amountAssetTicker.isNullOrEmpty()) {
            " ${amountAsset.name}"
        } else {
            view.text_tag.visiable()
            view.text_tag.text = amountAssetTicker
            ""
        }

        view.text_transaction_value.text = directionSign + amountValue + assetName
    }
}
