/*
 * Created by Eduard Zaydel on 26/6/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.local.gateway

import com.wavesplatform.sdk.model.response.node.AssetBalanceResponse

data class GatewayMetadataArgs(var asset: AssetBalanceResponse?,
                               var address: String?)