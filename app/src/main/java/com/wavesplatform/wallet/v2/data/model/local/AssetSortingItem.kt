/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.local

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.sdk.model.response.node.AssetBalanceResponse

class AssetSortingItem : MultiItemEntity {
    var type: Int = 0
    var asset: AssetBalanceResponse = AssetBalanceResponse()

    constructor(itemType: Int, asset: AssetBalanceResponse) {
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
        const val TYPE_FAVORITE_ITEM = 1
        const val TYPE_DEFAULT_ITEM = 2
        const val TYPE_HIDDEN_ITEM = 3
        const val TYPE_HIDDEN_HEADER = 4
        const val TYPE_FAVORITE_SEPARATOR = 5
        const val TYPE_EMPTY_FAVORITE = 6
        const val TYPE_EMPTY_DEFAULT  = 7
        const val TYPE_EMPTY_HIDDEN  = 8
    }
}