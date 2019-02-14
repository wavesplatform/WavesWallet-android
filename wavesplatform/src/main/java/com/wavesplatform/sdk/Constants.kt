package com.wavesplatform.sdk

import com.wavesplatform.sdk.model.response.AssetBalance
import com.wavesplatform.sdk.model.response.AssetInfo
import com.wavesplatform.sdk.model.response.GlobalConfiguration
import com.wavesplatform.sdk.model.response.IssueTransaction


class Constants {
    companion object {
        const val VERSION: Int = 2
        const val NET_CODE = 'W'.toByte()

        const val URL_NODE = "https://nodes.wavesnodes.com"
        const val URL_DATA = "https://api.wavesplatform.com"
        const val URL_MATCHER = "https://matcher.wavesplatform.com"
        const val URL_SPAM_FILE = "https://github-proxy.wvservices.com/wavesplatform/waves-community/master/Scam%20tokens%20according%20to%20the%20opinion%20of%20Waves%20Community.csv/"
        const val URL_COINOMAT = "https://coinomat.com/api/"
        const val URL_COMMISSION = "https://github-proxy.wvservices.com/" + "wavesplatform/waves-client-config/master/fee.json"
        const val URL_CONFIG = "https://github-proxy.wvservices.com/" + "wavesplatform/waves-client-config/master/environment_mainnet.json"

        val WAVES_ASSET_ID = ""
        val MONERO_ASSET_ID = "5WvPKSJXzVE2orvbkJ8wsQmmQKqTv9sGBPksV4adViw3"
        val BITCOIN_ASSET_ID = "8LQW8f7P5d5PZM7GtZEBgaqRPGSzS3DfPuiXrURJ4AJS"
        val ETHEREUM_ASSET_ID = "474jTeYx2r2Va35794tCScAXWJG9hU2HcgxzMowaZUnu"
        val BITCOINCASH_ASSET_ID = "zMFqXuoyrn5w17PFurTqxB7GsS71fp9dfk6XFwxbPCy"
        val LIGHTCOIN_ASSET_ID = "HZk1mbfuJpmxU1Fs4AX5MWLVYtctsNcg6e2C6VKqK8zk"
        val ZEC_ASSET_ID = "BrjUWjndUanm5VsJkbUip8VRYy6LWJePtxya3FNv4TQa"
        val DASH_ASSET_ID = "B3uGHFRpSUuGEDWjqB9LWWxafQj8VTvpMucEyoxzws5H"
        val WUSD_ASSET_ID = "Ft8X1v1LTa1ABafufpaCWyVj8KkaxUWE6xBhW6sNFJck"
        val WEUR_ASSET_ID = "Gtb1WRznfchDnTh37ezoDTJ4wcoKaRsKqKjJjy7nm2zU"
        val WTRY_ASSET_ID = "2mX5DzVKWrAJw8iwdJnV2qtoeVG9h5nTDpTqC1wb1WEN"

        val wavesAssetInfo = AssetInfo(id = WAVES_ASSET_ID, precision = 8, name = "WAVES", quantity = 10000000000000000L)

        var MRTGeneralAsset = GlobalConfiguration.GeneralAssetId(assetId = "4uK8i4ThRGbehENwa6MxyLtxAjAo1Rj9fduborGExarC",
                gatewayId = "MRT", displayName = "MinersReward")

        var WCTGeneralAsset = GlobalConfiguration.GeneralAssetId(assetId = "DHgwrRvVyqJsepd32YbBqUeDH4GJ1N984X8QoekjgH8J",
                gatewayId = "WCT", displayName = "WavesCommunity")

        val defaultAssets = listOf(
                AssetBalance(WAVES_ASSET_ID, quantity = 10000000000000000L, isFavorite = true, issueTransaction = IssueTransaction(name = "WAVES", decimals = 8, quantity = 10000000000000000L, timestamp = 1460419200000L), isGateway = true),
                AssetBalance(BITCOIN_ASSET_ID, quantity = 2100000000000000, issueTransaction = IssueTransaction(assetId = BITCOIN_ASSET_ID, id = BITCOIN_ASSET_ID, name = "Bitcoin", decimals = 8, quantity = 2100000000000000, timestamp = 1480698060000L), isGateway = true),
                AssetBalance(ETHEREUM_ASSET_ID, quantity = 10000000000000000, issueTransaction = IssueTransaction(assetId = ETHEREUM_ASSET_ID, id = ETHEREUM_ASSET_ID, name = "Ethereum", decimals = 8, quantity = 10000000000000000, timestamp = 1500385140000L), isGateway = true),
                AssetBalance(WUSD_ASSET_ID, quantity = 100000000000, issueTransaction = IssueTransaction(assetId = WUSD_ASSET_ID, id = WUSD_ASSET_ID, name = "US Dollar", decimals = 2, quantity = 100000000000, timestamp = 1480431300000L), isFiatMoney = true, isGateway = true),
                AssetBalance(WEUR_ASSET_ID, quantity = 100000000000, issueTransaction = IssueTransaction(assetId = WEUR_ASSET_ID, id = WEUR_ASSET_ID, name = "Euro", decimals = 2, quantity = 10000000000, timestamp = 1480432200000L), isFiatMoney = true, isGateway = true),
                AssetBalance(LIGHTCOIN_ASSET_ID, quantity = 8400000000000000, issueTransaction = IssueTransaction(assetId = LIGHTCOIN_ASSET_ID, id = LIGHTCOIN_ASSET_ID, name = "Litecoin", decimals = 8, quantity = 8400000000000000, timestamp = 1505472180000L), isGateway = true),
                AssetBalance(ZEC_ASSET_ID, quantity = 2100000000000000, issueTransaction = IssueTransaction(assetId = ZEC_ASSET_ID, id = ZEC_ASSET_ID, name = "Zcash", decimals = 8, quantity = 2100000000000000, timestamp = 1507039380000L), isGateway = true),
                AssetBalance(BITCOINCASH_ASSET_ID, quantity = 2100000000000000, issueTransaction = IssueTransaction(assetId = BITCOINCASH_ASSET_ID, id = BITCOINCASH_ASSET_ID, name = "Bitcoin Cash", decimals = 8, quantity = 2100000000000000, timestamp = 1501678320000L), isGateway = true),
                AssetBalance(WTRY_ASSET_ID, quantity = 100000000, issueTransaction = IssueTransaction(assetId = WTRY_ASSET_ID, id = WTRY_ASSET_ID, name = "TRY", decimals = 2, quantity = 100000000, timestamp = 1512411060000L), isFiatMoney = true, isGateway = true),
                AssetBalance(DASH_ASSET_ID, quantity = 1890000000000000, issueTransaction = IssueTransaction(assetId = DASH_ASSET_ID, id = DASH_ASSET_ID, name = "DASH", decimals = 8, quantity = 1890000000000000, timestamp = 1524430860000L), isGateway = true),
                AssetBalance(MONERO_ASSET_ID, quantity = 1603984700000000, issueTransaction = IssueTransaction(assetId = MONERO_ASSET_ID, id = MONERO_ASSET_ID, name = "Monero", decimals = 8, quantity = 1603984700000000, timestamp = 1526572200000L), isGateway = true))

        val defaultCrypto = arrayOf(
                BITCOIN_ASSET_ID,
                ETHEREUM_ASSET_ID,
                LIGHTCOIN_ASSET_ID,
                MONERO_ASSET_ID,
                BITCOINCASH_ASSET_ID,
                ZEC_ASSET_ID,
                DASH_ASSET_ID)

        val defaultFiat = arrayOf(
                WEUR_ASSET_ID,
                WUSD_ASSET_ID,
                WTRY_ASSET_ID)

        const val SELL_ORDER_TYPE = "sell"
        const val BUY_ORDER_TYPE = "buy"

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
    }
}