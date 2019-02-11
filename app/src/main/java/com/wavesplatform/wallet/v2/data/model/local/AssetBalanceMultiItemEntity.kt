package com.wavesplatform.wallet.v2.data.model.local

import android.os.Parcelable
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.wallet.v2.data.model.db.AssetBalanceDb
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.AssetsAdapter
import kotlinx.android.parcel.Parcelize

@Parcelize
class AssetBalanceMultiItemEntity : AssetBalanceDb(), MultiItemEntity, Parcelable {

    override fun getItemType(): Int {
        return when {
            isSpam -> AssetsAdapter.TYPE_SPAM_ASSET
            isHidden -> AssetsAdapter.TYPE_HIDDEN_ASSET
            else -> AssetsAdapter.TYPE_ASSET
        }
    }
}