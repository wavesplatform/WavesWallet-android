package com.wavesplatform.wallet.v2.data.model.local

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance

class MultipleSortingAssetItem(val itemTypeValue: Int, val assetBalance: AssetBalance) : MultiItemEntity {

    override fun getItemType(): Int {
        return itemTypeValue
    }

    companion object {
        val POSITION_FAVORITE_ASSET = 1
        val POSITION_ASSET = 2
        val VISIBILITY_FAVORITE_ASSET = 3
        val VISIBILITY_ASSET = 4
    }
}