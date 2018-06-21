package com.wavesplatform.wallet.v2.data

import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.IssueTransaction

object Constants {

    // Production
    val URL_WAVES_COMMUNITY = "https://wavescommunity.com"
    val URL_TERMS = "https://wavesplatform.com/files/docs/Waves_terms_and_conditions.pdf"
    val URL_WHITEPAPER = "https://wavesplatform.com/files/images/whitepaper_v0.pdf"
    val URL_TELEGRAM = "https://telegram.me/wavesnews"
    val URL_GITHUB = "https://github.com/wavesplatform/"
    val URL_TWITTER = "https://twitter.com/wavesplatform"
    val URL_FACEBOOK = "https://www.facebook.com/wavesplatform"
    val URL_DISCORD = "https://discordapp.com/invite/cnFmDyA"

    val ACC_TWITTER = "wavesplatform"
    val ACC_TELEGRAM = "wavesnews"

    var alphabetColor = hashMapOf<String, Int>(
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

    var defaultAssets = listOf<AssetBalance>(
            AssetBalance("q", quantity = 10000000000000000L, isFavorite = true, issueTransaction = IssueTransaction(name = "WAVES", decimals = 8)),
            AssetBalance("8LQW8f7P5d5PZM7GtZEBgaqRPGSzS3DfPuiXrURJ4AJS", quantity = 2100000000000000, issueTransaction = IssueTransaction(assetId = "8LQW8f7P5d5PZM7GtZEBgaqRPGSzS3DfPuiXrURJ4AJS", id = "8LQW8f7P5d5PZM7GtZEBgaqRPGSzS3DfPuiXrURJ4AJS", name = "BTC", decimals = 8)),
            AssetBalance("474jTeYx2r2Va35794tCScAXWJG9hU2HcgxzMowaZUnu", quantity = 10000000000000000, issueTransaction = IssueTransaction(assetId = "474jTeYx2r2Va35794tCScAXWJG9hU2HcgxzMowaZUnu", id = "474jTeYx2r2Va35794tCScAXWJG9hU2HcgxzMowaZUnu", name = "ETH", decimals = 8)),
            AssetBalance("Ft8X1v1LTa1ABafufpaCWyVj8KkaxUWE6xBhW6sNFJck", quantity = 100000000000, issueTransaction = IssueTransaction(assetId = "Ft8X1v1LTa1ABafufpaCWyVj8KkaxUWE6xBhW6sNFJck", id = "Ft8X1v1LTa1ABafufpaCWyVj8KkaxUWE6xBhW6sNFJck", name = "USD", decimals = 2)),
            AssetBalance("Gtb1WRznfchDnTh37ezoDTJ4wcoKaRsKqKjJjy7nm2zU", quantity = 100000000000, issueTransaction = IssueTransaction(assetId = "Gtb1WRznfchDnTh37ezoDTJ4wcoKaRsKqKjJjy7nm2zU", id = "Gtb1WRznfchDnTh37ezoDTJ4wcoKaRsKqKjJjy7nm2zU", name = "EUR", decimals = 2)),
            AssetBalance("HZk1mbfuJpmxU1Fs4AX5MWLVYtctsNcg6e2C6VKqK8zk", quantity = 8400000000000000, issueTransaction = IssueTransaction(assetId = "HZk1mbfuJpmxU1Fs4AX5MWLVYtctsNcg6e2C6VKqK8zk", id = "HZk1mbfuJpmxU1Fs4AX5MWLVYtctsNcg6e2C6VKqK8zk", name = "LTC", decimals = 8)),
            AssetBalance("zMFqXuoyrn5w17PFurTqxB7GsS71fp9dfk6XFwxbPCy", quantity = 2100000000000000, issueTransaction = IssueTransaction(assetId = "zMFqXuoyrn5w17PFurTqxB7GsS71fp9dfk6XFwxbPCy", id = "zMFqXuoyrn5w17PFurTqxB7GsS71fp9dfk6XFwxbPCy", name = "BCH", decimals = 8)),
            AssetBalance("2mX5DzVKWrAJw8iwdJnV2qtoeVG9h5nTDpTqC1wb1WEN", quantity = 100000000, issueTransaction = IssueTransaction(assetId = "2mX5DzVKWrAJw8iwdJnV2qtoeVG9h5nTDpTqC1wb1WEN", id = "2mX5DzVKWrAJw8iwdJnV2qtoeVG9h5nTDpTqC1wb1WEN", name = "TRY", decimals = 3)),
            AssetBalance("B3uGHFRpSUuGEDWjqB9LWWxafQj8VTvpMucEyoxzws5H", quantity = 1890000000000000, issueTransaction = IssueTransaction(assetId = "B3uGHFRpSUuGEDWjqB9LWWxafQj8VTvpMucEyoxzws5H", id = "B3uGHFRpSUuGEDWjqB9LWWxafQj8VTvpMucEyoxzws5H", name = "DASH", decimals = 8)))

    var defaultAssetsAvatar = hashMapOf<String, Int>(
            Pair("", R.drawable.logo_waves_48),
            Pair("8LQW8f7P5d5PZM7GtZEBgaqRPGSzS3DfPuiXrURJ4AJS", R.drawable.logo_bitcoin_48),
            Pair("474jTeYx2r2Va35794tCScAXWJG9hU2HcgxzMowaZUnu", R.drawable.logo_ethereum_48),
            Pair("Ft8X1v1LTa1ABafufpaCWyVj8KkaxUWE6xBhW6sNFJck", R.drawable.logo_usd_48),
            Pair("Gtb1WRznfchDnTh37ezoDTJ4wcoKaRsKqKjJjy7nm2zU", R.drawable.logo_euro_48),
            Pair("HZk1mbfuJpmxU1Fs4AX5MWLVYtctsNcg6e2C6VKqK8zk", R.drawable.logo_ltc_48),
//            Pair("BrjUWjndUanm5VsJkbUip8VRYy6LWJePtxya3FNv4TQa", R.drawable.logo),
            Pair("zMFqXuoyrn5w17PFurTqxB7GsS71fp9dfk6XFwxbPCy", R.drawable.logo_bitcoincash_48),
            Pair("2mX5DzVKWrAJw8iwdJnV2qtoeVG9h5nTDpTqC1wb1WEN", R.drawable.logo_lira_48),
            Pair("B3uGHFRpSUuGEDWjqB9LWWxafQj8VTvpMucEyoxzws5H", R.drawable.logo_dash_48))
//    val URL_ROADMAP = "https://wavesplatform.com/files/docs/Waves_terms_and_conditions.pdf"

}
