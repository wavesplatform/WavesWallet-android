package com.wavesplatform.wallet.v2.data

import com.wavesplatform.wallet.R

object Constants {

    // Production
    const val URL_COINOMAT = "https://coinomat.com/api/"
    const val URL_WAVES_FORUM = "https://forum.wavesplatform.com/"
    const val URL_TERMS = "https://wavesplatform.com/files/docs/Privacy%20Policy_SW.pdf"
    const val URL_TERMS_AND_CONDITIONS = "https://wavesplatform.com/files/docs/Waves_terms_and_conditions.pdf"
    const val URL_WHITEPAPER = "https://wavesplatform.com/files/whitepaper_v0.pdf"
    const val URL_TELEGRAM = "https://telegram.me/wavesnews"
    const val URL_GITHUB = "https://github.com/wavesplatform/"
    const val URL_GITHUB_PROXY = "https://github-proxy.wvservices.com"
    const val URL_TWITTER = "https://twitter.com/wavesplatform"
    const val URL_FACEBOOK = "https://www.facebook.com/wavesplatform"
    const val URL_DISCORD = "https://discordapp.com/invite/cnFmDyA"
    const val URL_REDDIT = "https://www.reddit.com/r/Wavesplatform/"

    const val ACC_TWITTER = "wavesplatform"
    const val ACC_TELEGRAM = "wavesnews"

    const val SUPPORT_EMAIL = "support@wavesplatform.com"
    const val SUPPORT_SITE = "https://support.wavesplatform.com/"
    const val PRODUCATION_PACKAGE_NAME = "com.wavesplatform.wallet"


    // Custom Result code
    const val RESULT_CANCELED = 404
    const val RESULT_OK = 200
    const val RESULT_OK_NO_RESULT = 204
    const val RESULT_SMART_ERROR = 307

    const val ENABLE_VIEW = 1f
    const val DISABLE_VIEW = 0.3f

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

    // todo check move region to lib?

    val wavesAssetInfo = AssetInfo(id = WAVES_ASSET_ID_EMPTY, precision = 8, name = "WAVES", quantity = 10000000000000000L)

    var MRTGeneralAsset = GlobalConfiguration.ConfigAsset(assetId = "4uK8i4ThRGbehENwa6MxyLtxAjAo1Rj9fduborGExarC",
            gatewayId = "MRT", displayName = "MinersReward")

    var WCTGeneralAsset = GlobalConfiguration.ConfigAsset(assetId = "DHgwrRvVyqJsepd32YbBqUeDH4GJ1N984X8QoekjgH8J",
            gatewayId = "WCT", displayName = "WavesCommunity")

    fun find(assetId: String): AssetBalance? {
        return queryFirst { equalTo("assetId", assetId) }
    }

    fun findByGatewayId(gatewayId: String): AssetBalance? { // ticker
        for (asset in EnvironmentManager.globalConfiguration.generalAssets) {
            if (asset.gatewayId == gatewayId) {
                return find(asset.assetId)
            }
        }
        return null
    }

    fun defaultAssetsAvatar(): MutableMap<String, String> {
        val allConfigAssets = EnvironmentManager.globalConfiguration.generalAssets
                .plus(EnvironmentManager.globalConfiguration.assets)
        return allConfigAssets.associateBy({ it.assetId }, { it.iconUrls.default }).toMutableMap()
    }

    fun coinomatCryptoCurrencies(): MutableMap<String, String> {
        return EnvironmentManager.globalConfiguration.generalAssets
                .associateBy({ it.assetId }, { it.gatewayId })
                .toMutableMap()
    }

    fun defaultCrypto(): Array<String> {
        return EnvironmentManager.defaultAssets
                .filter { !it.isFiatMoney }
                .map { it.assetId }
                .toTypedArray()
    }

    fun defaultFiat(): Array<String> {
        return EnvironmentManager.defaultAssets
                .filter { it.isFiatMoney }
                .map { it.assetId }
                .toTypedArray()
    }
    // todo check end region to lib?
}
