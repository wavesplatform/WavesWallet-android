package com.wavesplatform.wallet.v2.ui.home.history.adapter

import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.home.history.item.HistoryDateItemFragment.Companion.all
import com.wavesplatform.wallet.v2.ui.home.history.item.HistoryDateItemFragment.Companion.received
import com.wavesplatform.wallet.v2.ui.home.history.item.HistoryDateItemFragment.Companion.send
import com.wavesplatform.wallet.v2.util.makeTextHalfBold
import javax.inject.Inject

class HistoryItemAdapter @Inject constructor() : BaseSectionQuickAdapter<HistoryItem, BaseViewHolder>(R.layout.wallet_leasing_item, R.layout.asset_header, null) {

    var dataType = all

    override fun convertHead(helper: BaseViewHolder?, item: HistoryItem?) {
        helper?.setText(R.id.text_header_text, item?.header)
    }

    override fun convert(helper: BaseViewHolder?, item: HistoryItem?) {

        when (dataType) {
            all -> {
                helper?.getView<ImageView>(R.id.image_startlease)?.setImageResource(R.drawable.ic_startlease)
            }
            send -> {
                helper?.getView<ImageView>(R.id.image_startlease)?.setImageResource(R.drawable.ic_send)
            }
            received -> {
                helper?.getView<ImageView>(R.id.image_startlease)?.setImageResource(R.drawable.ic_receive)
            }
        }

        val textLeasingValue = helper?.getView<TextView>(R.id.text_leasing_value)
        textLeasingValue?.text = "${item?.t?.assetValue}"
        textLeasingValue?.makeTextHalfBold()
    }

    fun setType(type: String) {
        dataType = type
    }
}
