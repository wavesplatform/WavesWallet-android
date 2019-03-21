package com.wavesplatform.wallet.v2.data.model.local

import com.wavesplatform.sdk.net.model.response.AssetInfo
import com.wavesplatform.sdk.net.model.response.OrderResponse

data class MyOrderTransaction(var orderResponse: OrderResponse,
                              var amountAssetInfo: AssetInfo?,
                              var priceAssetInfo: AssetInfo?,
                              var fee: Long)