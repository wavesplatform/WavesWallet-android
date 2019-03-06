package com.wavesplatform.wallet.v2.ui.home.dex.sorting

import com.chad.library.adapter.base.BaseItemDraggableAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.MarketResponse
import javax.inject.Inject

class ActiveMarketsSortingAdapter @Inject constructor() : BaseItemDraggableAdapter<MarketResponse, BaseViewHolder>(R.layout.dex_active_markets_sorting_item, null) {

    override fun convert(helper: BaseViewHolder, item: MarketResponse) {
        helper.setText(R.id.text_market_name, "${item.amountAssetShortName} / ${item.priceAssetShortName}")
                .addOnClickListener(R.id.image_delete)
    }
}
