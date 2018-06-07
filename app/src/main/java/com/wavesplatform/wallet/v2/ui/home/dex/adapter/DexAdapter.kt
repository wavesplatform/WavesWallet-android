package com.wavesplatform.wallet.v2.ui.home.dex.adapter

import android.support.v4.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.adapter.TestObject
import java.util.*
import javax.inject.Inject

class DexAdapter @Inject constructor() : BaseQuickAdapter<TestObject, BaseViewHolder>(R.layout.dex_layout_item, null) {

    override fun convert(helper: BaseViewHolder, item: TestObject) {
        val trade = Random().nextInt(3)
        var tradeIcon = ContextCompat.getDrawable(mContext, R.drawable.ic_chartarrow_success_400)

        var verifyIcon =
                if (item.isFavourite) {
                    ContextCompat.getDrawable(mContext, R.drawable.ic_verified_multy)
                } else {
                    ContextCompat.getDrawable(mContext, R.drawable.ic_unverified_multy)
                }

        when (trade) {
            1 -> {
                tradeIcon = ContextCompat.getDrawable(mContext, R.drawable.ic_chartarrow_error_500)
            }
            2 -> {
                tradeIcon = ContextCompat.getDrawable(mContext, R.drawable.ic_chartarrow_accent_100)
            }
        }
        helper.setImageDrawable(R.id.image_dex_trade, tradeIcon)
        helper.setImageDrawable(R.id.image_verified, verifyIcon)
    }
}
