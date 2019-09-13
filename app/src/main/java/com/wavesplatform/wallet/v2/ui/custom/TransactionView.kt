package com.wavesplatform.wallet.v2.ui.custom

import android.content.Context
import android.graphics.Typeface
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import com.wavesplatform.sdk.keeper.interfaces.KeeperTransaction
import com.wavesplatform.sdk.model.request.node.DataTransaction
import com.wavesplatform.sdk.model.request.node.InvokeScriptTransaction
import com.wavesplatform.sdk.model.request.node.TransferTransaction
import com.wavesplatform.sdk.model.response.node.AssetsDetailsResponse
import com.wavesplatform.sdk.utils.WavesConstants
import com.wavesplatform.sdk.utils.getScaledAmount
import com.wavesplatform.sdk.utils.notNull
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.TransactionType
import com.wavesplatform.wallet.v2.util.icon
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

    fun setTransaction(transaction: KeeperTransaction,
                       asset: AssetsDetailsResponse? = null) {
        when (transaction) {
            is TransferTransaction -> {
                image_transaction.setImageDrawable(TransactionType.SENT_TYPE.icon())
                text_transaction_name.text = context.getString(TransactionType.SENT_TYPE.title)
                val decimals = asset?.decimals ?: 8
                transaction.amount.notNull {
                    text_transaction_title.text =
                            "-${getScaledAmount(it, decimals)} " +
                                    "${asset?.name ?: WavesConstants.WAVES_ASSET_INFO.name}"
                }
            }
            is DataTransaction -> {
                image_transaction.setImageDrawable(TransactionType.DATA_TYPE.icon())
                text_transaction_name.text = context.getString(R.string.history_data_type_title)
                text_transaction_title.text = context.getString(TransactionType.DATA_TYPE.title)
                text_transaction_title.setTypeface(null, Typeface.BOLD)
            }
            is InvokeScriptTransaction -> {
                image_transaction.setImageDrawable(TransactionType.SCRIPT_INVOCATION_TYPE.icon())
                text_transaction_name.text = context.getString(R.string.history_data_type_title)
                text_transaction_title.text = context.getString(TransactionType.SCRIPT_INVOCATION_TYPE.title)
                text_transaction_title.setTypeface(null, Typeface.BOLD)
            }
        }
    }
}