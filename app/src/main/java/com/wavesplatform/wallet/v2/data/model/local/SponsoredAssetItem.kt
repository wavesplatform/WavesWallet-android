/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.local

import com.wavesplatform.sdk.model.response.AssetBalanceResponse

data class SponsoredAssetItem(
        var assetBalance: AssetBalanceResponse,
        var fee: String,
        var isActive: Boolean
)