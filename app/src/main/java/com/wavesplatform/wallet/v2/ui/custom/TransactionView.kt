package com.wavesplatform.wallet.v2.ui.custom

import android.content.Context
import android.graphics.Typeface
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import com.wavesplatform.sdk.model.request.node.*
import com.wavesplatform.sdk.model.response.node.AssetsDetailsResponse
import com.wavesplatform.sdk.model.response.node.HistoryTransactionResponse
import com.wavesplatform.sdk.model.response.node.TransferResponse
import com.wavesplatform.sdk.model.response.node.transaction.*
import com.wavesplatform.sdk.utils.getScaledAmount
import com.wavesplatform.sdk.utils.notNull
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.TransactionType
import com.wavesplatform.wallet.v2.util.WavesWallet
import com.wavesplatform.wallet.v2.util.getTransactionAmount
import com.wavesplatform.wallet.v2.util.getTransactionType
import kotlinx.android.synthetic.main.view_keeper_transaction.view.*

open class TransactionView : ConstraintLayout {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    protected fun init(attrs: AttributeSet? = null) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.view_keeper_transaction, this, true)
    }

    fun setTransaction(transaction: BaseTransactionResponse,
                       transactionType: Int,
                       asset: AssetsDetailsResponse) {


        val txType = TransactionType.getTypeById(transactionType)
        image_transaction.setImageResource(txType.image)
        text_transaction_name.text = context.getText(txType.title)

        val decimals = asset.decimals

        when (txType) {
            TransactionType.SENT_TYPE -> {
                transaction as TransferTransactionResponse
                transaction.amount.notNull {
                    text_transaction_title.text =
                            "-${getScaledAmount(it, decimals)}"
                }
            }
            TransactionType.RECEIVED_TYPE -> {
                transaction as TransferTransactionResponse
                transaction.amount.notNull {
                    text_transaction_title.text =
                            "+${getScaledAmount(it, decimals)}"
                }
            }
            TransactionType.RECEIVE_SPONSORSHIP_TYPE -> {
                transaction as SponsorshipTransactionResponse
                transaction.fee.notNull {
                    asset.notNull { assetNonNull ->
                        text_transaction_title.text =
                                "+${getScaledAmount(it, assetNonNull.decimals)} ${assetNonNull.name}"
                    }
                }
            }
            TransactionType.MASS_SPAM_RECEIVE_TYPE,
            TransactionType.MASS_RECEIVE_TYPE,
            TransactionType.MASS_SEND_TYPE -> {
                transaction as MassTransferTransactionResponse

                val list = mutableListOf<TransferResponse>()
                transaction.transfers.forEach {
                    list.add(TransferResponse(
                            recipient = it.recipient,
                            amount = it.amount))
                }

                val massTransaction = HistoryTransactionResponse(
                        type = transaction.type,
                        amount = transaction.totalAmount ?: 0,
                        transfers = list)

                text_transaction_title.text = getTransactionAmount(
                        transaction = massTransaction, decimals = decimals)
            }
            TransactionType.CREATE_ALIAS_TYPE -> {
                transaction as AliasTransactionResponse
                text_transaction_title.text = transaction.alias
                text_transaction_title.setTypeface(null, Typeface.BOLD)
            }
            TransactionType.EXCHANGE_TYPE -> {
                //setExchangeItem(item.data, view)
            }
            TransactionType.CANCELED_LEASING_TYPE -> {
                transaction as LeaseCancelTransactionResponse
                asset.notNull {
                    // text_transaction_title.text = getScaledAmount(transaction., it.decimals)
                }
            }
            TransactionType.TOKEN_BURN_TYPE -> {
                transaction as BurnTransactionResponse
                transaction.amount.notNull {
                    text_transaction_title.text =
                            "-${getScaledAmount(it, decimals)}"
                }
            }
            TransactionType.TOKEN_GENERATION_TYPE,
            TransactionType.TOKEN_REISSUE_TYPE -> {
                transaction as ReissueTransactionResponse
                val quantity = getScaledAmount(transaction.quantity, decimals)
                text_transaction_title.text = "+$quantity"
            }
            TransactionType.DATA_TYPE,
            TransactionType.SET_ADDRESS_SCRIPT_TYPE,
            TransactionType.CANCEL_ADDRESS_SCRIPT_TYPE,
            TransactionType.SCRIPT_INVOCATION_TYPE,
            TransactionType.UPDATE_ASSET_SCRIPT_TYPE -> {
                text_transaction_name.text = context.getString(R.string.history_data_type_title)
                text_transaction_title.text = context.getString(txType.title)
                text_transaction_title.setTypeface(null, Typeface.BOLD)
            }
            TransactionType.SET_SPONSORSHIP_TYPE,
            TransactionType.CANCEL_SPONSORSHIP_TYPE -> {
                transaction as SponsorshipTransactionResponse
                asset.notNull {
                    text_transaction_title.text = it.name
                }
            }
            else -> {
                text_transaction_name.text = context.getString(R.string.history_data_type_title)
                text_transaction_title.text = context.getString(txType.title)
                text_transaction_title.setTypeface(null, Typeface.BOLD)
            }
        }

    }

    fun setTransaction(transaction: BaseTransaction,
                       asset: AssetsDetailsResponse,
                       spam: HashSet<String>? = null) {

        val transactionType = getTransactionType(transaction, WavesWallet.getAddress(), spam)
        val txType = TransactionType.getTypeById(transactionType)
        image_transaction.setImageResource(txType.image)
        text_transaction_name.text = context.getText(txType.title)

        val decimals = asset.decimals

        when (txType) {
            TransactionType.SENT_TYPE -> {
                transaction as TransferTransaction
                transaction.amount.notNull {
                    text_transaction_title.text =
                            "-${getScaledAmount(it, decimals)}"
                }
            }
            TransactionType.RECEIVED_TYPE -> {
                transaction as TransferTransaction
                transaction.amount.notNull {
                    text_transaction_title.text =
                            "+${getScaledAmount(it, decimals)}"
                }
            }
            TransactionType.RECEIVE_SPONSORSHIP_TYPE -> {
                transaction as SponsorshipTransaction
                transaction.fee.notNull {
                    asset.notNull { assetNonNull ->
                        text_transaction_title.text =
                                "+${getScaledAmount(it, assetNonNull.decimals)} ${assetNonNull.name}"
                    }
                }
            }
            TransactionType.MASS_SPAM_RECEIVE_TYPE,
            TransactionType.MASS_RECEIVE_TYPE,
            TransactionType.MASS_SEND_TYPE -> {
                transaction as MassTransferTransaction

                val list = mutableListOf<TransferResponse>()
                transaction.transfers.forEach {
                    list.add(TransferResponse(
                            recipient = it.recipient,
                            amount = it.amount))
                }

                val massTransaction = HistoryTransactionResponse(
                        type = transaction.type,
                        amount = transaction.totalAmount ?: 0,
                        transfers = list)

                text_transaction_title.text = getTransactionAmount(
                        transaction = massTransaction, decimals = decimals)
            }
            TransactionType.CREATE_ALIAS_TYPE -> {
                transaction as AliasTransaction
                text_transaction_title.text = transaction.alias
                text_transaction_title.setTypeface(null, Typeface.BOLD)
            }
            TransactionType.EXCHANGE_TYPE -> {
                //setExchangeItem(item.data, view)
            }
            TransactionType.CANCELED_LEASING_TYPE -> {
                transaction as LeaseCancelTransaction
                asset.notNull {
                    // text_transaction_title.text = getScaledAmount(transaction., it.decimals)
                }
            }
            TransactionType.TOKEN_BURN_TYPE -> {
                transaction as BurnTransaction
                transaction.quantity.notNull {
                    text_transaction_title.text =
                            "-${getScaledAmount(transaction.quantity, decimals)}"
                }
            }
            TransactionType.TOKEN_GENERATION_TYPE,
            TransactionType.TOKEN_REISSUE_TYPE -> {
                transaction as ReissueTransaction
                val quantity = getScaledAmount(transaction.quantity, decimals)
                text_transaction_title.text = "+$quantity"
            }
            TransactionType.DATA_TYPE,
            TransactionType.SET_ADDRESS_SCRIPT_TYPE,
            TransactionType.CANCEL_ADDRESS_SCRIPT_TYPE,
            TransactionType.SCRIPT_INVOCATION_TYPE,
            TransactionType.UPDATE_ASSET_SCRIPT_TYPE -> {
                text_transaction_name.text = context.getString(R.string.history_data_type_title)
                text_transaction_title.text = context.getString(txType.title)
                text_transaction_title.setTypeface(null, Typeface.BOLD)
            }
            TransactionType.SET_SPONSORSHIP_TYPE,
            TransactionType.CANCEL_SPONSORSHIP_TYPE -> {
                transaction as SponsorshipTransaction
                asset.notNull {
                    text_transaction_title.text = it.name
                }
            }
            else -> {
                text_transaction_name.text = context.getString(R.string.history_data_type_title)
                text_transaction_title.text = context.getString(txType.title)
                text_transaction_title.setTypeface(null, Typeface.BOLD)
            }
        }
    }
}