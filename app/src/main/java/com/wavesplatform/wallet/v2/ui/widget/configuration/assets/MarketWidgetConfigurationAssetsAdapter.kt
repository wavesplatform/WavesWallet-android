/*
 * Created by Eduard Zaydel on 8/8/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget.configuration.assets

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.sdk.model.response.data.AssetInfoResponse
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.custom.AssetAvatarView
import javax.inject.Inject

class MarketWidgetConfigurationAssetsAdapter @Inject constructor() : BaseQuickAdapter<AssetInfoResponse,
        BaseViewHolder>(R.layout.bottom_sheet_dialog_search_asset_item, null) {

    override fun convert(helper: BaseViewHolder, item: AssetInfoResponse) {
        helper.setText(R.id.asset_title, item.name)
                .addOnClickListener(R.id.asset_root)

        val assetIcon = helper.getView<AssetAvatarView>(R.id.asset_icon)
        assetIcon.setAsset(item)
    }
}