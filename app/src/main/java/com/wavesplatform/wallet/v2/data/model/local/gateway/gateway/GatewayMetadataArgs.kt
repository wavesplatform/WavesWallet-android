/*
 * Created by Eduard Zaydel on 25/6/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.local.gateway.gateway

import com.wavesplatform.wallet.v1.payload.AssetBalance
import com.wavesplatform.wallet.v2.data.model.local.gateway.base.GatewayMetadataModel

data class GatewayMetadataArgs(var asset: AssetBalance,
                               var address: String) : GatewayMetadataModel