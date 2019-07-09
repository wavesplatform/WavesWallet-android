/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.your_assets

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.sdk.model.response.node.AssetBalanceResponse
import com.wavesplatform.sdk.utils.notNull
import kotlinx.android.synthetic.main.item_your_assets.view.*
import javax.inject.Inject

class YourAssetsAdapter @Inject constructor() : BaseQuickAdapter<AssetBalanceResponse, BaseViewHolder>(R.layout.item_your_assets, null) {

    var allData: MutableList<AssetBalanceResponse> = arrayListOf()
    var currentAssetId: String? = null

    override fun convert(helper: BaseViewHolder, item: AssetBalanceResponse) {
        helper.setText(R.id.text_asset_name, item.getName())
                .setText(R.id.text_asset_value, item.getDisplayAvailableBalance())
                .setVisible(R.id.image_favourite, item.isFavorite)
        helper.itemView.text_asset_value.visibility =
                if (item.getAvailableBalance() == 0L) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
        helper.itemView.image_asset_icon.setAsset(item)
        helper.itemView.checkbox_choose.isChecked = item.assetId == currentAssetId
    }

    fun filter(text: String) {
        data.clear()
        if (text.trim().isEmpty()) {
            setNewData(allData)
        } else {
            for (item in allData) {
                item.getName().notNull {
                    if (it.toLowerCase().contains(text.toLowerCase())) {
                        data.add(item)
                    }
                }
            }
        }
        notifyDataSetChanged()
    }

    fun showOnlyWithBalance(onlyWithBalance: Boolean) {
        val newAssets = if (onlyWithBalance) {
            allData.filter { it.getAvailableBalance() > 0 }
        } else {
            allData
        }
        setNewData(newAssets)
    }
}