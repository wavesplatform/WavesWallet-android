/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.dex

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.WatchMarketResponse
import com.wavesplatform.sdk.utils.notNull
import pers.victor.ext.findDrawable
import java.math.BigDecimal
import javax.inject.Inject

class DexAdapter @Inject constructor() : BaseQuickAdapter<WatchMarketResponse, BaseViewHolder>(R.layout.item_dex_layout, null) {

    override fun convert(helper: BaseViewHolder, item: WatchMarketResponse) {
        if (item.pairResponse != null) {
            item.pairResponse.notNull { data ->
                val deltaPercent = if (data.firstPrice > data.lastPrice) {
                    (data.firstPrice.minus(data.lastPrice)).times(BigDecimal(100))
                } else {
                    (data.lastPrice.minus(data.firstPrice)).times(BigDecimal(100))
                }

                val percent = if (deltaPercent != BigDecimal.ZERO) {
                    deltaPercent / data.lastPrice
                } else {
                    BigDecimal.ZERO
                }

                val tradeIcon = when {
                    data.lastPrice > data.firstPrice -> {
                        findDrawable(R.drawable.ic_chartarrow_success_400)
                    }
                    data.firstPrice > data.lastPrice -> {
                        findDrawable(R.drawable.ic_chartarrow_error_500)
                    }
                    data.lastPrice == data.firstPrice -> {
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
