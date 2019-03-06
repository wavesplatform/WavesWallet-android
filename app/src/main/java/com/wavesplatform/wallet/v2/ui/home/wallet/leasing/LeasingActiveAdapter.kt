package com.wavesplatform.wallet.v2.ui.home.wallet.leasing

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.util.icon
import com.wavesplatform.wallet.v2.util.makeTextHalfBold
import com.wavesplatform.wallet.v2.util.transactionType
import kotlinx.android.synthetic.main.recycle_item_history.view.*
import javax.inject.Inject

class LeasingActiveAdapter @Inject constructor() : BaseQuickAdapter<Transaction, BaseViewHolder>(R.layout.recycle_item_history, null) {

    override fun convert(helper: BaseViewHolder, item: Transaction) {
        helper.setGone(R.id.text_tag, true)
                .setText(R.id.text_tag, Constants.wavesAssetInfo.name)

        helper.itemView.image_transaction.setImageDrawable(item.transactionType().icon())
        helper.itemView.text_transaction_name.text = mContext.getString(item.transactionType().title)
        helper.itemView.text_transaction_value.text = MoneyUtil.getScaledText(item.amount, item.asset)
        helper.itemView.text_transaction_value.makeTextHalfBold()
    }
}
