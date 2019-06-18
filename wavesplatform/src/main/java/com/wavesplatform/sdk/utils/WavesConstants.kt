package com.wavesplatform.sdk.utils

import com.wavesplatform.sdk.model.response.data.AssetInfoResponse

class WavesConstants {

    companion object {

        const val URL_NODE = "https://nodes.wavesnodes.com/"
        const val URL_DATA = "https://api.wavesplatform.com/"
        const val URL_MATCHER = "https://matcher.wavesplatform.com/"

        const val URL_NODE_TEST = "https://pool.testnet.wavesnodes.com/"
        const val URL_DATA_TEST = "api.testnet.wavesplatform.com/"
        const val URL_MATCHER_TEST = "https://matcher.testnet.wavesnodes.com/"

        const val URL_WAVES_EXPLORER = "http://wavesexplorer.com/tx/%s"
        const val URL_WAVES_STAGE_EXPLORER = "http://stage.wavesexplorer.com/tx/%s"

        const val VERSION: Int = 2
        const val WAVES_ASSET_ID_EMPTY = ""
        const val WAVES_ASSET_ID_FILLED = "WAVES"
        const val SELL_ORDER_TYPE = "sell"
        const val BUY_ORDER_TYPE = "buy"
        const val CUSTOM_FEE_ASSET_NAME = "Waves"
        const val WAVES_MIN_FEE = 100000L
        const val MIN_WAVES_SPONSORED_BALANCE = 1.005

        val WAVES_ASSET_INFO = AssetInfoResponse(
                id = WAVES_ASSET_ID_EMPTY,
                precision = 8,
                name = "WAVES",
                quantity = 10000000000000000L)
    }
}