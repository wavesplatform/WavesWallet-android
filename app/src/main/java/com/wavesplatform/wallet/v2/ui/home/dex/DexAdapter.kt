package com.wavesplatform.wallet.v2.ui.home.dex

import android.support.v4.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.data.model.remote.response.MarketResponse
import java.util.*
import javax.inject.Inject

class DexAdapter @Inject constructor() : BaseQuickAdapter<WatchMarket, BaseViewHolder>(R.layout.dex_layout_item, null) {

    override fun convert(helper: BaseViewHolder, item: WatchMarket) {
        val tradeIcon = when (Random().nextInt(3)) {
            0 -> {
                ContextCompat.getDrawable(mContext, R.drawable.ic_chartarrow_success_400)
            }
            1 -> {
                ContextCompat.getDrawable(mContext, R.drawable.ic_chartarrow_error_500)
            }
            2 -> {
                ContextCompat.getDrawable(mContext, R.drawable.ic_chartarrow_accent_100)
            }
            else -> ContextCompat.getDrawable(mContext, R.drawable.ic_chartarrow_success_400)
        }
        helper.setImageDrawable(R.id.image_dex_trade, tradeIcon)
                .setText(R.id.text_asset_name, "${item.market.amountAssetShortName} / ${item.market.priceAssetShortName}")
                .setText(R.id.text_price_asset, item.market.priceAssetLongName)
    }
}
