package com.wavesplatform.wallet.v2.data.model.local

import com.wavesplatform.wallet.v2.data.model.remote.response.AssetInfo
import com.wavesplatform.wallet.v2.data.model.remote.response.OrderResponse

data class MyOrderTransaction(var orderResponse: OrderResponse,
                              var amountAssetInfo: AssetInfo?,
                              var priceAssetInfo: AssetInfo?,
                              var fee: Long)