package com.wavesplatform.wallet.v2.ui.home.wallet.leasing.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.R.id.image_transaction
import com.wavesplatform.wallet.R.string.asset
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.TestObject
import com.wavesplatform.wallet.v2.util.icon
import com.wavesplatform.wallet.v2.util.makeTextHalfBold
import com.wavesplatform.wallet.v2.util.title
import com.wavesplatform.wallet.v2.util.transactionType
import kotlinx.android.synthetic.main.recycle_item_history.view.*
import javax.inject.Inject

class AdapterActiveLeasing @Inject constructor() : BaseQuickAdapter<Transaction, BaseViewHolder>(R.layout.recycle_item_history, null) {

    override fun convert(helper: BaseViewHolder, item: Transaction) {
        helper.itemView.image_transaction.setImageDrawable(item.transactionType().icon())
        helper.itemView.text_transaction_name.text = item.transactionType().title()
        helper.itemView.text_transaction_value.text = MoneyUtil.getScaledText(item.amount, item.asset)
        helper.itemView.text_transaction_value.makeTextHalfBold()
    }
}

