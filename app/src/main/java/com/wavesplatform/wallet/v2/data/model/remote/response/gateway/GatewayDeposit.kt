/*
 * Created by Eduard Zaydel on 2/7/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.remote.response.gateway

import java.math.BigDecimal

data class GatewayDeposit(
        var minLimit: BigDecimal = BigDecimal.ZERO,
        var depositAddress: String = "",
        var currencyFrom: String? = ""
)