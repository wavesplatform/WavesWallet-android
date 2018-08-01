package com.wavesplatform.wallet.v2.data.model.local

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.wallet.v2.ui.home.history.TestObject

class OrderbookItem : MultiItemEntity {
    private var itemType: Int = 0
    private var item: TestObject? = null


    constructor(itemType: Int, item: TestObject) {
        this.itemType = itemType
        this.item = item
    }

    constructor(itemType: Int) {
        this.itemType = itemType
    }

    override fun getItemType(): Int {
        return itemType
    }

    companion object {
        val LAST_PRICE_TYPE = 1
        val PRICE_TYPE = 2
    }
}