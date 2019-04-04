/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.local

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.sdk.net.model.response.AssetBalance

class AssetSortingItem : MultiItemEntity {
    var type: Int = 0
    var asset: AssetBalance = AssetBalance()

    constructor(itemType: Int, asset: AssetBalance) {
        this.type = itemType
        this.asset = asset
    }

    constructor(itemType: Int) {
        this.type = itemType
    }

    override fun getItemType(): Int {
        return type
    }

    companion object {
        val TYPE_FAVORITE = 1
        val TYPE_NOT_FAVORITE = 2
        val TYPE_LINE = 3
    }
}