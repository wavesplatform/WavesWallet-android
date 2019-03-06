package com.wavesplatform.wallet.v2.ui.home.history.filter.adapter

import android.widget.ImageView
import android.widget.LinearLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.home.history.filter.TransferModel
import com.wavesplatform.wallet.v2.util.setMargins
import pers.victor.ext.dp2px
import javax.inject.Inject

class TransferAdapter @Inject constructor() : BaseQuickAdapter<TransferModel, BaseViewHolder>(R.layout.history_transfer_item, null) {

    override fun convert(helper: BaseViewHolder, item: TransferModel) {

        var address = item?.address
        var addressStr = StringBuilder()
        for (i in 0 until address.length) {
            when (i) {
                in 0..3 -> addressStr.append(address[i])
                in 4..6 -> addressStr.append(".")
                in address.length - 4 until address.length -> addressStr.append(address[i])
            }
        }

        helper.setText(R.id.text_transfer_name, item.name)
                .setText(R.id.text_transfer_address, addressStr)

        when {
            data.indexOf(item) == 0 -> helper.getView<LinearLayout>(R.id.root).setMargins(dp2px(16), right = dp2px(4))
            data.indexOf(item) == data.size - 1 -> helper.getView<LinearLayout>(R.id.root).setMargins(right = dp2px(16), left = dp2px(4))
            else -> helper.getView<LinearLayout>(R.id.root).setMargins(right = dp2px(4), left = dp2px(4))
        }

        if (item?.isChecked) {
            helper.getView<ImageView>(R.id.image_checked_state).setImageResource(R.drawable.ic_on)
            helper.getView<LinearLayout>(R.id.main_container).setBackgroundResource(R.drawable.period_checked)
        } else {
            helper.getView<ImageView>(R.id.image_checked_state).setImageResource(R.drawable.ic_off)
            helper.getView<LinearLayout>(R.id.main_container).setBackgroundResource(R.drawable.period_normal)
        }
    }
}
