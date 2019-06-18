package com.wavesplatform.wallet.v2.data.model.local

import android.os.Parcelable
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.sdk.model.response.node.AssetBalanceResponse
import com.wavesplatform.sdk.utils.notNull
import com.wavesplatform.wallet.v2.data.model.db.AssetBalanceDb
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.AssetsAdapter
import kotlinx.android.parcel.Parcelize

@Parcelize
class AssetBalanceMultiItemEntity() : AssetBalanceResponse(), MultiItemEntity, Parcelable {

    constructor(assetBalance: AssetBalanceDb?) : this() {
        assetBalance.notNull {
            this.assetId = it.assetId
            this.balance = it.balance
            this.leasedBalance = it.leasedBalance
            this.inOrderBalance = it.inOrderBalance
            this.reissuable = it.reissuable
            this.minSponsoredAssetFee = it.minSponsoredAssetFee
            this.sponsorBalance = it.sponsorBalance
            this.quantity = it.quantity
            this.issueTransaction = it.issueTransaction?.convertFromDb()
            this.isHidden = it.isHidden
            this.position = it.position
            this.configureVisibleState = it.configureVisibleState
            this.isChecked = it.isChecked
            this.isFiatMoney = it.isFiatMoney
            this.isFavorite = it.isFavorite
            this.isGateway = it.isGateway
            this.isSpam = it.isSpam
        }
    }

    override fun getItemType(): Int {
        return when {
            isSpam -> AssetsAdapter.TYPE_SPAM_ASSET
            isHidden -> AssetsAdapter.TYPE_HIDDEN_ASSET
            else -> AssetsAdapter.TYPE_ASSET
        }
    }
}