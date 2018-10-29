package com.wavesplatform.wallet.v2.data

import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetInfo
import com.wavesplatform.wallet.v2.data.model.remote.response.IssueTransaction

object Constants {

    // Production
    const val URL_SPAM = "https://raw.githubusercontent.com/wavesplatform/waves-community/master/Scam%20tokens%20according%20to%20the%20opinion%20of%20Waves%20Community.csv"
    const val URL_WAVES_COMMUNITY = "https://wavescommunity.com"
    const val URL_TERMS = "https://wavesplatform.com/files/docs/Waves_terms_and_conditions.pdf"
    const val URL_WHITEPAPER = "https://wavesplatform.com/files/images/whitepaper_v0.pdf"
    const val URL_TELEGRAM = "https://telegram.me/wavesnews"
    const val URL_GITHUB = "https://github.com/wavesplatform/"
    const val URL_TWITTER = "https://twitter.com/wavesplatform"
    const val URL_FACEBOOK = "https://www.facebook.com/wavesplatform"
    const val URL_DISCORD = "https://discordapp.com/invite/cnFmDyA"

    const val URL_GLOBAL_CONFIGURATION = "https://raw.githubusercontent.com/wavesplatform/waves-client-config/master/environment_mainnet.json"

    const val ACC_TWITTER = "wavesplatform"
    const val ACC_TELEGRAM = "wavesnews"

    const val SUPPORT_EMAIL = "support@wavesplatform.com"
    const val SUPPORT_SITE = "https://support.wavesplatform.com/"
    const val PRODUCATION_PACKAGE_NAME = "com.wavesplatform.wallet"

    const val WAVES_FEE = 100000L
    const val CUSTOM_FEE_ASSET_NAME: String = "Waves"
    const val VERSION = 2
    const val ADDRESS_SCHEME = 'W'
    const val ALIAS_VERSION = 2

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

    // Custom Result code
    const val RESULT_CANCELED = 404
    const val RESULT_OK = 200
    const val RESULT_OK_NO_RESULT = 204

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

    val wavesAssetInfo = AssetInfo(id = "", precision = 8, name = "WAVES", quantity = 10000000000000000L)

    val defaultAssets = listOf<AssetBalance>(
            AssetBalance("", quantity = 10000000000000000L, isFavorite = true, issueTransaction = IssueTransaction(name = "WAVES", decimals = 8, quantity = 10000000000000000L), isGateway = true),
            AssetBalance("8LQW8f7P5d5PZM7GtZEBgaqRPGSzS3DfPuiXrURJ4AJS", quantity = 2100000000000000, issueTransaction = IssueTransaction(assetId = "8LQW8f7P5d5PZM7GtZEBgaqRPGSzS3DfPuiXrURJ4AJS", id = "8LQW8f7P5d5PZM7GtZEBgaqRPGSzS3DfPuiXrURJ4AJS", name = "Bitcoin", decimals = 8, quantity = 2100000000000000), isGateway = true),
            AssetBalance("474jTeYx2r2Va35794tCScAXWJG9hU2HcgxzMowaZUnu", quantity = 10000000000000000, issueTransaction = IssueTransaction(assetId = "474jTeYx2r2Va35794tCScAXWJG9hU2HcgxzMowaZUnu", id = "474jTeYx2r2Va35794tCScAXWJG9hU2HcgxzMowaZUnu", name = "Ethereum", decimals = 8, quantity = 10000000000000000), isGateway = true),
            AssetBalance("Ft8X1v1LTa1ABafufpaCWyVj8KkaxUWE6xBhW6sNFJck", quantity = 100000000000, issueTransaction = IssueTransaction(assetId = "Ft8X1v1LTa1ABafufpaCWyVj8KkaxUWE6xBhW6sNFJck", id = "Ft8X1v1LTa1ABafufpaCWyVj8KkaxUWE6xBhW6sNFJck", name = "US Dollar", decimals = 2, quantity = 100000000000), isFiatMoney = true, isGateway = true),
            AssetBalance("Gtb1WRznfchDnTh37ezoDTJ4wcoKaRsKqKjJjy7nm2zU", quantity = 100000000000, issueTransaction = IssueTransaction(assetId = "Gtb1WRznfchDnTh37ezoDTJ4wcoKaRsKqKjJjy7nm2zU", id = "Gtb1WRznfchDnTh37ezoDTJ4wcoKaRsKqKjJjy7nm2zU", name = "Euro", decimals = 2, quantity = 10000000000), isFiatMoney = true, isGateway = true),
            AssetBalance("HZk1mbfuJpmxU1Fs4AX5MWLVYtctsNcg6e2C6VKqK8zk", quantity = 8400000000000000, issueTransaction = IssueTransaction(assetId = "HZk1mbfuJpmxU1Fs4AX5MWLVYtctsNcg6e2C6VKqK8zk", id = "HZk1mbfuJpmxU1Fs4AX5MWLVYtctsNcg6e2C6VKqK8zk", name = "Litecoin", decimals = 8, quantity = 8400000000000000), isGateway = true),
            AssetBalance("BrjUWjndUanm5VsJkbUip8VRYy6LWJePtxya3FNv4TQa", quantity = 2100000000000000, issueTransaction = IssueTransaction(assetId = "BrjUWjndUanm5VsJkbUip8VRYy6LWJePtxya3FNv4TQa", id = "BrjUWjndUanm5VsJkbUip8VRYy6LWJePtxya3FNv4TQa", name = "Zcash", decimals = 8, quantity = 2100000000000000), isGateway = true),
            AssetBalance("zMFqXuoyrn5w17PFurTqxB7GsS71fp9dfk6XFwxbPCy", quantity = 2100000000000000, issueTransaction = IssueTransaction(assetId = "zMFqXuoyrn5w17PFurTqxB7GsS71fp9dfk6XFwxbPCy", id = "zMFqXuoyrn5w17PFurTqxB7GsS71fp9dfk6XFwxbPCy", name = "Bitcoin Cash", decimals = 8, quantity = 2100000000000000), isGateway = true),
            AssetBalance("2mX5DzVKWrAJw8iwdJnV2qtoeVG9h5nTDpTqC1wb1WEN", quantity = 100000000, issueTransaction = IssueTransaction(assetId = "2mX5DzVKWrAJw8iwdJnV2qtoeVG9h5nTDpTqC1wb1WEN", id = "2mX5DzVKWrAJw8iwdJnV2qtoeVG9h5nTDpTqC1wb1WEN", name = "TRY", decimals = 3, quantity = 100000000), isFiatMoney = true, isGateway = true),
            AssetBalance("B3uGHFRpSUuGEDWjqB9LWWxafQj8VTvpMucEyoxzws5H", quantity = 1890000000000000, issueTransaction = IssueTransaction(assetId = "B3uGHFRpSUuGEDWjqB9LWWxafQj8VTvpMucEyoxzws5H", id = "B3uGHFRpSUuGEDWjqB9LWWxafQj8VTvpMucEyoxzws5H", name = "DASH", decimals = 8, quantity = 1890000000000000), isGateway = true),
            AssetBalance("5WvPKSJXzVE2orvbkJ8wsQmmQKqTv9sGBPksV4adViw3", quantity = 1603984700000000, issueTransaction = IssueTransaction(assetId = "5WvPKSJXzVE2orvbkJ8wsQmmQKqTv9sGBPksV4adViw3", id = "5WvPKSJXzVE2orvbkJ8wsQmmQKqTv9sGBPksV4adViw3", name = "Monero", decimals = 8, quantity = 1603984700000000), isGateway = true))

    val defaultAssetsAvatar = hashMapOf(
            Pair("", R.drawable.logo_waves_48),
            Pair("8LQW8f7P5d5PZM7GtZEBgaqRPGSzS3DfPuiXrURJ4AJS", R.drawable.logo_bitcoin_48),
            Pair("474jTeYx2r2Va35794tCScAXWJG9hU2HcgxzMowaZUnu", R.drawable.logo_ethereum_48),
            Pair("Ft8X1v1LTa1ABafufpaCWyVj8KkaxUWE6xBhW6sNFJck", R.drawable.logo_usd_48),
            Pair("Gtb1WRznfchDnTh37ezoDTJ4wcoKaRsKqKjJjy7nm2zU", R.drawable.logo_euro_48),
            Pair("HZk1mbfuJpmxU1Fs4AX5MWLVYtctsNcg6e2C6VKqK8zk", R.drawable.logo_ltc_48),
            Pair("5WvPKSJXzVE2orvbkJ8wsQmmQKqTv9sGBPksV4adViw3", R.drawable.logo_monero_48),
            Pair("zMFqXuoyrn5w17PFurTqxB7GsS71fp9dfk6XFwxbPCy", R.drawable.logo_bitcoincash_48),
            Pair("BrjUWjndUanm5VsJkbUip8VRYy6LWJePtxya3FNv4TQa", R.drawable.logo_zec_48),
            Pair("2mX5DzVKWrAJw8iwdJnV2qtoeVG9h5nTDpTqC1wb1WEN", R.drawable.logo_lira_48),
            Pair("B3uGHFRpSUuGEDWjqB9LWWxafQj8VTvpMucEyoxzws5H", R.drawable.logo_dash_48))
//    val URL_ROADMAP = "https://wavesplatform.com/files/docs/Waves_terms_and_conditions.pdf"

    val defaultCrypto = arrayOf(
            "8LQW8f7P5d5PZM7GtZEBgaqRPGSzS3DfPuiXrURJ4AJS",
            "474jTeYx2r2Va35794tCScAXWJG9hU2HcgxzMowaZUnu",
            "HZk1mbfuJpmxU1Fs4AX5MWLVYtctsNcg6e2C6VKqK8zk",
            "5WvPKSJXzVE2orvbkJ8wsQmmQKqTv9sGBPksV4adViw3",
            "zMFqXuoyrn5w17PFurTqxB7GsS71fp9dfk6XFwxbPCy",
            "2mX5DzVKWrAJw8iwdJnV2qtoeVG9h5nTDpTqC1wb1WEN",
            "B3uGHFRpSUuGEDWjqB9LWWxafQj8VTvpMucEyoxzws5H")

    val defaultFiat = arrayOf(
            "Gtb1WRznfchDnTh37ezoDTJ4wcoKaRsKqKjJjy7nm2zU",
            "Ft8X1v1LTa1ABafufpaCWyVj8KkaxUWE6xBhW6sNFJck")

}
