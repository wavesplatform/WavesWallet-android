package com.wavesplatform.wallet.v2.data

import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetInfo
import com.wavesplatform.wallet.v2.data.model.remote.response.GlobalConfiguration
import com.wavesplatform.wallet.v2.data.model.remote.response.IssueTransaction

object Constants {

    // Production
    const val URL_COINOMAT = "https://coinomat.com/api/"
    const val URL_SPAM = "https://raw.githubusercontent.com/wavesplatform/waves-community/master/"
    const val URL_SPAM_FILE = "https://raw.githubusercontent.com/wavesplatform/waves-community/master/Scam%20tokens%20according%20to%20the%20opinion%20of%20Waves%20Community.csv"
    const val URL_WAVES_FORUM = "https://forum.wavesplatform.com/"
    const val URL_TERMS = "https://wavesplatform.com/files/docs/Waves_terms_and_conditions.pdf"
    const val URL_WHITEPAPER = "https://wavesplatform.com/files/whitepaper_v0.pdf"
    const val URL_TELEGRAM = "https://telegram.me/wavesnews"
    const val URL_GITHUB = "https://github.com/wavesplatform/"
    const val URL_TWITTER = "https://twitter.com/wavesplatform"
    const val URL_FACEBOOK = "https://www.facebook.com/wavesplatform"
    const val URL_DISCORD = "https://discordapp.com/invite/cnFmDyA"
    const val URL_REDDIT = "https://www.reddit.com/r/Wavesplatform/"

    const val ACC_TWITTER = "wavesplatform"
    const val ACC_TELEGRAM = "wavesnews"

    const val SUPPORT_EMAIL = "support@wavesplatform.com"
    const val SUPPORT_SITE = "https://support.wavesplatform.com/"
    const val PRODUCATION_PACKAGE_NAME = "com.wavesplatform.wallet"

    const val WAVES_FEE = 100000L
    const val WAVES_DEX_FEE = 300000L
    const val CUSTOM_FEE_ASSET_NAME: String = "Waves"

    const val SELL_ORDER_TYPE = "sell"
    const val BUY_ORDER_TYPE = "buy"

    // Transaction types
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
    const val ID_SET_SCRIPT_TYPE = 17
    const val ID_CANCEL_SCRIPT_TYPE = 18
    const val ID_SET_SPONSORSHIP_TYPE = 19
    const val ID_CANCEL_SPONSORSHIP_TYPE = 20

    // Custom Result code
    const val RESULT_CANCELED = 404
    const val RESULT_OK = 200
    const val RESULT_OK_NO_RESULT = 204
    const val RESULT_SMART_ERROR = 307

    const val VERSION = 2
    val WAVES_ASSET_ID = ""
    val MONERO_ASSET_ID = EnvironmentManager.findAssetId("XMR")
    val BITCOIN_ASSET_ID = EnvironmentManager.findAssetId("BTC")
    val ETHEREUM_ASSET_ID = EnvironmentManager.findAssetId("ETH")
    val BITCOINCASH_ASSET_ID = EnvironmentManager.findAssetId("BCH")
    val LIGHTCOIN_ASSET_ID = EnvironmentManager.findAssetId("LTC")
    val ZEC_ASSET_ID = EnvironmentManager.findAssetId("ZEC")
    val DASH_ASSET_ID = EnvironmentManager.findAssetId("DASH")
    val WUSD_ASSET_ID = EnvironmentManager.findAssetId("USD")
    val WEUR_ASSET_ID = EnvironmentManager.findAssetId("EUR")
    val WTRY_ASSET_ID = EnvironmentManager.findAssetId("TRY")

    val alphabetColor = hashMapOf(
            Pair("a", R.color.a),
            Pair("b", R.color.b),
            Pair("c", R.color.c),
            Pair("d", R.color.d),
            Pair("e", R.color.e),
            Pair("f", R.color.f),
            Pair("g", R.color.g),
            Pair("h", R.color.h),
            Pair("i", R.color.i),
            Pair("j", R.color.j),
            Pair("k", R.color.k),
            Pair("l", R.color.l),
            Pair("m", R.color.m),
            Pair("n", R.color.n),
            Pair("o", R.color.o),
            Pair("p", R.color.p),
            Pair("q", R.color.q),
            Pair("r", R.color.r),
            Pair("s", R.color.s),
            Pair("t", R.color.t),
            Pair("u", R.color.u),
            Pair("v", R.color.v),
            Pair("w", R.color.w),
            Pair("x", R.color.x),
            Pair("y", R.color.y),
            Pair("z", R.color.z),
            Pair("persist", R.color.persist))

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

    val defaultAssetsAvatar = hashMapOf(
            Pair(WAVES_ASSET_ID, R.drawable.logo_waves_48),
            Pair(BITCOIN_ASSET_ID, R.drawable.logo_bitcoin_48),
            Pair(ETHEREUM_ASSET_ID, R.drawable.logo_ethereum_48),
            Pair(WUSD_ASSET_ID, R.drawable.logo_usd_48),
            Pair(WEUR_ASSET_ID, R.drawable.logo_euro_48),
            Pair(WTRY_ASSET_ID, R.drawable.logo_lira_48),
            Pair(LIGHTCOIN_ASSET_ID, R.drawable.logo_ltc_48),
            Pair(MONERO_ASSET_ID, R.drawable.logo_monero_48),
            Pair(BITCOINCASH_ASSET_ID, R.drawable.logo_bitcoincash_48),
            Pair(ZEC_ASSET_ID, R.drawable.logo_zec_48),
            Pair(DASH_ASSET_ID, R.drawable.logo_dash_48))

    val coinomatCryptoCurrencies = hashMapOf(
            Pair(BITCOIN_ASSET_ID, "BTC"),
            Pair(ETHEREUM_ASSET_ID, "ETH"),
            Pair(LIGHTCOIN_ASSET_ID, "LTC"),
            Pair(MONERO_ASSET_ID, "XMR"),
            Pair(BITCOINCASH_ASSET_ID, "BCH"),
            Pair(ZEC_ASSET_ID, "ZEC"),
            Pair(DASH_ASSET_ID, "DASH"))

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

    val ENABLE_VIEW = 1f
    val DISABLE_VIEW = 0.3f
}
