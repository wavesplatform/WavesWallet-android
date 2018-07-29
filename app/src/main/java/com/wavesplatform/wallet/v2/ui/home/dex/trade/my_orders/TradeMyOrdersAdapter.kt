package com.wavesplatform.wallet.v2.ui.home.dex.trade.my_orders

import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.MyOrderItem
import com.wavesplatform.wallet.v2.ui.home.history.adapter.HistoryItem
import kotlinx.android.synthetic.main.recycle_item_my_orders.view.*
import pers.victor.ext.click
import javax.inject.Inject

class TradeMyOrdersAdapter @Inject constructor() : BaseSectionQuickAdapter<MyOrderItem, BaseViewHolder>(R.layout.recycle_item_my_orders, R.layout.header_my_orders, null) {

    override fun convertHead(helper: BaseViewHolder?, item: MyOrderItem?) {
        helper?.setText(R.id.text_header_text, item?.header)
    }

    override fun convert(helper: BaseViewHolder?, item: MyOrderItem?) {
        helper?.addOnClickListener(R.id.image_delete)
    }
}
