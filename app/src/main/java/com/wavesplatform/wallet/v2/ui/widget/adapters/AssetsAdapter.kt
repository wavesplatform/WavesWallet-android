package com.wavesplatform.wallet.v2.ui.widget.adapters

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.sdk.model.response.data.AssetInfoResponse
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.custom.AssetAvatarView
import javax.inject.Inject

class AssetsAdapter @Inject constructor() : BaseQuickAdapter<AssetInfoResponse,
        BaseViewHolder>(R.layout.bottom_sheet_dialog_search_asset_item, null) {

    override fun convert(helper: BaseViewHolder, item: AssetInfoResponse) {
        helper.setText(R.id.asset_title, item.name)
                .addOnClickListener(R.id.asset_root)

        val assetIcon = helper.getView<AssetAvatarView>(R.id.asset_icon)
        assetIcon.setAsset(item)
    }
}