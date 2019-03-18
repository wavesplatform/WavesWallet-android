package com.wavesplatform.wallet.v2.data.model.local

import com.chad.library.adapter.base.entity.AbstractExpandableItem
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.sdk.net.model.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.AssetsAdapter

class WalletSectionItem(var header: String) : AbstractExpandableItem<AssetBalance>(), MultiItemEntity {
    override fun getItemType(): Int {
        return AssetsAdapter.TYPE_HEADER
    }

    override fun getLevel(): Int = 0
}