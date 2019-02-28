package com.wavesplatform.sdk

import com.wavesplatform.sdk.model.response.AssetInfo
import com.wavesplatform.sdk.model.response.GlobalConfiguration
import com.wavesplatform.sdk.utils.EnvironmentManager


class Constants {
    companion object {
        const val VERSION: Int = 2
        const val NET_CODE = 'W'.toByte()

        const val URL_NODE = "https://nodes.wavesnodes.com"
        const val URL_DATA = "https://api.wavesplatform.com"
        const val URL_MATCHER = "https://matcher.wavesplatform.com"
        const val URL_SPAM_FILE = "https://github-proxy.wvservices.com/wavesplatform/waves-community/master/Scam%20tokens%20according%20to%20the%20opinion%20of%20Waves%20Community.csv/"
        const val URL_COINOMAT = "https://coinomat.com/api/"
        const val WAVES_EXPLORER = "http://wavesexplorer.com/tx/%s"
        const val URL_COMMISSION = "https://github-proxy.wvservices.com/" + "wavesplatform/waves-client-config/master/fee.json"
        const val URL_CONFIG = "https://github-proxy.wvservices.com/" + "wavesplatform/waves-client-config/master/environment_mainnet.json"

        const val WAVES_ASSET_ID_EMPTY = ""
        const val WAVES_ASSET_ID_FILLED = "WAVES"

        val wavesAssetInfo = AssetInfo(id = WAVES_ASSET_ID_EMPTY, precision = 8, name = "WAVES", quantity = 10000000000000000L)
        var MRTGeneralAsset = GlobalConfiguration.GeneralAssetId(assetId = "4uK8i4ThRGbehENwa6MxyLtxAjAo1Rj9fduborGExarC",
                gatewayId = "MRT", displayName = "MinersReward")

        var WCTGeneralAsset = GlobalConfiguration.GeneralAssetId(assetId = "DHgwrRvVyqJsepd32YbBqUeDH4GJ1N984X8QoekjgH8J",
                gatewayId = "WCT", displayName = "WavesCommunity")

        const val SELL_ORDER_TYPE = "sell"
        const val BUY_ORDER_TYPE = "buy"

        const val CUSTOM_FEE_ASSET_NAME: String = "Waves"
        const val WAVES_MIN_FEE: Long = 100000L
        const val MIN_WAVES_SPONSORED_BALANCE: Double = 1.005

        // Transaction view types non Transaction block-chain type
        const val ID_RECEIVED_TYPE = 0
        const val ID_SENT_TYPE = 1
        const val ID_STARTED_LEASING_TYPE = 2
        const val ID_SELF_TRANSFER_TYPE = 3
        const val ID_CANCELED_LEASING_TYPE = 4
        const val ID_TOKEN_GENERATION_TYPE = 5
        const val ID_TOKEN_BURN_TYPE = 6
        const val ID_TOKEN_REISSUE_TYPE = 7
        const val ID_EXCHANGE_TYPE = 8
        const val ID_CREATE_ALIAS_TYPE = 9
        const val ID_INCOMING_LEASING_TYPE = 10
        const val ID_UNRECOGNISED_TYPE = 11
        const val ID_MASS_SEND_TYPE = 12
        const val ID_MASS_RECEIVE_TYPE = 13
        const val ID_SPAM_RECEIVE_TYPE = 14
        const val ID_MASS_SPAM_RECEIVE_TYPE = 15
        const val ID_DATA_TYPE = 16
        const val ID_SPAM_SELF_TRANSFER = 17
        const val ID_SET_ADDRESS_SCRIPT_TYPE = 18
        const val ID_CANCEL_ADDRESS_SCRIPT_TYPE = 19
        const val ID_RECEIVE_SPONSORSHIP_TYPE = 20
        const val ID_SET_SPONSORSHIP_TYPE = 21
        const val ID_CANCEL_SPONSORSHIP_TYPE = 22
        const val ID_UPDATE_ASSET_SCRIPT_TYPE = 23

        fun defaultAssetsAvatar(): HashMap<String, String> {
            val map = hashMapOf<String, String>()
            for (asset in EnvironmentManager.globalConfiguration.generalAssetIds) {
                map[asset.assetId] = asset.iconUrls.default
            }
            return map
        }

        fun coinomatCryptoCurrencies(): HashMap<String, String> {
            val map = hashMapOf<String, String>()
            for (asset in EnvironmentManager.globalConfiguration.generalAssetIds) {
                if (asset.isGateway) {
                    map[asset.assetId] = asset.gatewayId
                }
            }
            return map
        }

        fun defaultCrypto(): Array<String> {
            val list = mutableListOf<String>()
            for (asset in EnvironmentManager.defaultAssets) {
                if (!asset.isFiatMoney) {
                    list.add(asset.assetId)
                }
            }
            return list.toTypedArray()
        }

        fun defaultFiat(): Array<String> {
            val list = mutableListOf<String>()
            for (asset in EnvironmentManager.defaultAssets) {
                if (asset.isFiatMoney) {
                    list.add(asset.assetId)
                }
            }
            return list.toTypedArray()
        }
    }
 }