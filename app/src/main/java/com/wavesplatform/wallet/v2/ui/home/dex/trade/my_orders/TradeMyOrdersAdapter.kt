package com.wavesplatform.wallet.v2.ui.home.dex.trade.my_orders

import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.MyOrderItem
import com.wavesplatform.wallet.v2.ui.home.history.adapter.HistoryItem
import com.wavesplatform.wallet.v2.util.random
import kotlinx.android.synthetic.main.recycle_item_my_orders.view.*
import pers.victor.ext.click
import pers.victor.ext.findColor
import javax.inject.Inject

class TradeMyOrdersAdapter @Inject constructor() : BaseSectionQuickAdapter<MyOrderItem, BaseViewHolder>(R.layout.recycle_item_my_orders, R.layout.header_my_orders, null) {

    override fun convertHead(helper: BaseViewHolder?, item: MyOrderItem?) {
        helper?.setText(R.id.text_header_text, item?.header)
    }

    override fun convert(helper: BaseViewHolder?, item: MyOrderItem?) {
        helper?.addOnClickListener(R.id.image_delete)
        if ((0..1).random() == 0) {
            helper?.setTextColor(R.id.text_status_value, findColor(R.color.error400))
        }else{
            helper?.setTextColor(R.id.text_status_value, findColor(R.color.submit400))
        }
    }
}
