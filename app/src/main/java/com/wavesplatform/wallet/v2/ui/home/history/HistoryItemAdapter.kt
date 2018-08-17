package com.wavesplatform.wallet.v2.ui.home.history

import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.TransactionType
import com.wavesplatform.wallet.v2.ui.home.history.item.HistoryDateItemFragment
import com.wavesplatform.wallet.v2.util.*
import kotlinx.android.synthetic.main.recycle_item_history.view.*
import java.util.*
import javax.inject.Inject

class HistoryItemAdapter @Inject constructor() : BaseSectionQuickAdapter<HistoryItem, BaseViewHolder>(R.layout.recycle_item_history, R.layout.asset_header, null) {

    var dataType: String? = HistoryDateItemFragment.all

    override fun convertHead(helper: BaseViewHolder?, item: HistoryItem?) {
        helper?.setText(R.id.text_header_text, item?.header)
    }

    override fun convert(helper: BaseViewHolder?, item: HistoryItem?) {
        helper?.itemView.notNull { view ->
            view.image_transaction.setImageDrawable(item?.t?.transactionType()?.icon())
            item?.t?.transactionType().notNull {
                try {
                    view.text_transaction_name.text = String.format(it.title(), item?.t?.asset?.issueTransaction?.name)
                } catch (e: MissingFormatArgumentException) {
                    view.text_transaction_name.text = it.title()
                }

                when (it) {
                    TransactionType.MASS_SPAM_RECEIVE_TYPE,  TransactionType.MASS_SEND_TYPE, TransactionType.MASS_RECEIVE_TYPE-> {
                        if (item?.t?.transfers != null && item.t?.transfers!!.isNotEmpty()) {
                            val sum = item.t.transfers.sumBy { it.amount.toInt() }
                            view.text_transaction_amount.text = MoneyUtil.getScaledText(sum.toLong(), item.t?.asset)
                        }
                    }
                    else -> {
                        item?.t?.amount.notNull {
                            view.text_transaction_amount.text = MoneyUtil.getScaledText(it, item?.t?.asset)
                        }
                    }
                }
            }

            view.text_transaction_amount.makeTextHalfBold()
        }

    }

    fun setType(type: String?) {
        dataType = type
    }
}
