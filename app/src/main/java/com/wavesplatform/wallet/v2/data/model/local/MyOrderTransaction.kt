/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.local

import com.wavesplatform.sdk.model.response.data.AssetInfoResponse
import com.wavesplatform.sdk.model.response.matcher.AssetPairOrderResponse

data class MyOrderTransaction(var orderResponse: AssetPairOrderResponse,
                              var amountAssetInfo: AssetInfoResponse?,
                              var priceAssetInfo: AssetInfoResponse?,
                              var fee: Long)