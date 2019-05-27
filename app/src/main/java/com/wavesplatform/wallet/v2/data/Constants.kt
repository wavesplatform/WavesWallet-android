/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data

import com.wavesplatform.wallet.R

object Constants {

    // Production
    const val URL_VERSION = "https://github-proxy.wvservices.com/" +
            "wavesplatform/waves-client-config/master/version_android.json"
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

    object View {
        const val ENABLE_VIEW = 1f
        const val DISABLE_VIEW = 0.3f
        const val DEFAULT_ANIMATION_DURATION = 300L
        const val FULL_VISIBILITY = 1f
        const val FULL_GONE = 0f
    }

    val alphabetColor = mutableListOf(
            R.color.a,
            R.color.b,
            R.color.c,
            R.color.d,
            R.color.e,
            R.color.f,
            R.color.g,
            R.color.h,
            R.color.i,
            R.color.j,
            R.color.k,
            R.color.l,
            R.color.m,
            R.color.n,
            R.color.o,
            R.color.p,
            R.color.q,
            R.color.r,
            R.color.s,
            R.color.t,
            R.color.u,
            R.color.v,
            R.color.w,
            R.color.x,
            R.color.y,
            R.color.z)

    fun defaultAssetsAvatar(): MutableMap<String, String> {
        val allConfigAssets = com.wavesplatform.wallet.v2.util.EnvironmentManager.globalConfiguration.generalAssets
                .plus(com.wavesplatform.wallet.v2.util.EnvironmentManager.globalConfiguration.assets)
        return allConfigAssets.associateBy({ it.assetId }, { it.iconUrls.default }).toMutableMap()
    }

    fun coinomatCryptoCurrencies(): MutableMap<String, String> {
        return com.wavesplatform.wallet.v2.util.EnvironmentManager.globalConfiguration.generalAssets
                .associateBy({ it.assetId }, { it.gatewayId })
                .toMutableMap()
    }

    fun defaultCrypto(): Array<String> {
        return com.wavesplatform.wallet.v2.util.EnvironmentManager.defaultAssets
                .filter { !it.isFiatMoney }
                .map { it.assetId }
                .toTypedArray()
    }

    fun defaultFiat(): Array<String> {
        return com.wavesplatform.wallet.v2.util.EnvironmentManager.defaultAssets
                .filter { it.isFiatMoney }
                .map { it.assetId }
                .toTypedArray()
    }
}
