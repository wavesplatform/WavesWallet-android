package com.wavesplatform.wallet.v2.ui.widget.adapters

import com.chad.library.adapter.base.BaseItemDraggableAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.sdk.model.response.data.AssetInfoResponse
import com.wavesplatform.sdk.model.response.data.SearchPairResponse
import com.wavesplatform.wallet.R
import javax.inject.Inject

class TokenAdapter
@Inject constructor() : BaseItemDraggableAdapter<SearchPairResponse.Pair,
        BaseViewHolder>(R.layout.widget_drag_asset_item, null) {

    var allData: List<AssetInfoResponse> = arrayListOf()

    override fun convert(helper: BaseViewHolder, item: SearchPairResponse.Pair) {
        val assetInfo = allData.firstOrNull { it.id == item.amountAsset}
        helper.setText(R.id.text_market_name, assetInfo?.name)
                .addOnClickListener(R.id.image_delete)
    }
}