/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.dex.sorting

import com.chad.library.adapter.base.BaseItemDraggableAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.sdk.model.response.MarketResponse
import javax.inject.Inject

class ActiveMarketsSortingAdapter @Inject constructor() : BaseItemDraggableAdapter<MarketResponse, BaseViewHolder>(R.layout.item_dex_active_markets_sorting, null) {

    override fun convert(helper: BaseViewHolder, item: MarketResponse) {
        helper.setText(R.id.text_market_name, "${item.amountAssetShortName} / ${item.priceAssetShortName}")
                .addOnClickListener(R.id.image_delete)
    }
}
