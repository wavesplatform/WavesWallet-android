package com.wavesplatform.wallet.v2.ui.widget.adapters

import com.chad.library.adapter.base.BaseItemDraggableAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.sdk.model.response.data.AssetInfoResponse
import com.wavesplatform.sdk.model.response.data.SearchPairResponse
import com.wavesplatform.wallet.R
import javax.inject.Inject

class TokenAdapter @Inject constructor() : BaseItemDraggableAdapter<TokenAdapter.TokenPair,
        BaseViewHolder>(R.layout.widget_drag_asset_item, null) {

    override fun convert(helper: BaseViewHolder, item: TokenPair) {
        if (data.size == 1) {
            helper.setText(R.id.text_market_name, item.assetInfo.name)
                    .setImageResource(R.id.image_delete, R.drawable.ic_draglock_22_disabled_400)
        } else {
            helper.setText(R.id.text_market_name, item.assetInfo.name)
                    .setImageResource(R.id.image_delete, R.drawable.ic_delete_22_error_500)
                    .addOnClickListener(R.id.image_delete)
        }
    }

    data class TokenPair(var assetInfo: AssetInfoResponse, var pair: SearchPairResponse.Pair)
}