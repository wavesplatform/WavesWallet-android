package com.wavesplatform.wallet.v2.data.model.local

import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance

data class SponsoredAssetItem(
    var assetBalance: AssetBalance,
    var fee: String,
    var isActive: Boolean
)