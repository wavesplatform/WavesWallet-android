package com.wavesplatform.wallet.v2.ui.home.dex

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.util.notNull
import pers.victor.ext.findDrawable
import java.math.BigDecimal
import javax.inject.Inject

class DexAdapter @Inject constructor() : BaseQuickAdapter<WatchMarket, BaseViewHolder>(R.layout.dex_layout_item, null) {

    override fun convert(helper: BaseViewHolder, item: WatchMarket) {
        if (item.pairResponse?.data != null) {
            item.pairResponse?.data.notNull { data ->
                val deltaPercent = (data.lastPrice.minus(data.firstPrice)).times(BigDecimal(100))

                val percent = if (deltaPercent != BigDecimal.ZERO) {
                    deltaPercent / data.lastPrice
                } else {
                    BigDecimal.ZERO
                }

                val tradeIcon = when {
                    percent > BigDecimal.ZERO -> {
                        findDrawable(R.drawable.ic_chartarrow_success_400)
                    }
                    percent < BigDecimal.ZERO -> {
                        findDrawable(R.drawable.ic_chartarrow_error_500)
                    }
                    percent == BigDecimal.ZERO -> {
                        findDrawable(R.drawable.ic_chartarrow_accent_100)
                    }
                    else -> findDrawable(R.drawable.ic_chartarrow_accent_100)
                }

                helper.setImageDrawable(R.id.image_dex_trade, tradeIcon)
                        .setText(R.id.text_price, data.lastPrice.stripTrailingZeros().toPlainString())
                        .setText(R.id.text_percent, "${"%.2f".format(percent)}%")
            }
        } else {
            helper.setImageDrawable(R.id.image_dex_trade, findDrawable(R.drawable.ic_chartarrow_accent_100))
                    .setText(R.id.text_price, "0.0")
                    .setText(R.id.text_percent, "0.0%")
        }

        helper.setText(R.id.text_asset_name, "${item.market.amountAssetShortName} / ${item.market.priceAssetShortName}")
                .setText(R.id.text_price_asset, mContext.getString(R.string.dex_last_price_value, item.market.priceAssetLongName))
    }
}
