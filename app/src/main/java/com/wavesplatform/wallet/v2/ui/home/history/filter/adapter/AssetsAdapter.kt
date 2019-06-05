/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.history.filter.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.sdk.model.response.AssetBalanceResponse
import kotlinx.android.synthetic.main.item_history_asset.view.*
import pers.victor.ext.gone
import pers.victor.ext.visiable
import javax.inject.Inject

class AssetsAdapter @Inject constructor() : BaseQuickAdapter<AssetBalanceResponse, BaseViewHolder>(R.layout.item_history_asset, null) {

    override fun convert(helper: BaseViewHolder, item: AssetBalanceResponse) {
        helper.setText(R.id.text_asset_name, item.getName())

        helper.itemView.image_asset_icon.setAsset(item)

        if (item.isChecked) {
            helper.itemView.container_checked.visiable()
        } else {
            helper.itemView.container_checked.gone()
        }
    }
}
