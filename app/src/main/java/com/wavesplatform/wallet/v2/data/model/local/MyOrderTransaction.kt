/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.local

import com.wavesplatform.wallet.v2.data.model.remote.response.AssetInfo
import com.wavesplatform.wallet.v2.data.model.remote.response.OrderResponse

data class MyOrderTransaction(var orderResponse: OrderResponse,
                              var amountAssetInfo: AssetInfo?,
                              var priceAssetInfo: AssetInfo?,
                              var fee: Long)