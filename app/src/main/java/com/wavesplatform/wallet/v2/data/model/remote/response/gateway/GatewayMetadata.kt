/*
 * Created by Eduard Zaydel on 25/6/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.remote.response.gateway

import java.math.BigDecimal

data class GatewayMetadata(
        var minLimit: BigDecimal = BigDecimal.ZERO,
        var maxLimit: BigDecimal = BigDecimal.ZERO,
        var fee: BigDecimal = BigDecimal.ZERO,
        var gatewayProcessId: String? = null,
        var gatewayRecipientAddress: String? = null
)