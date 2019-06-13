/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data

import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.service.cofigs.GlobalConfigurationResponse

object Constants {

    // Production
    const val URL_COINOMAT = "https://coinomat.com/api/"
    const val URL_GITHUB = "https://github.com/wavesplatform/"
    const val URL_GITHUB_PROXY = "https://github-proxy.wvservices.com"
    const val URL_GITHUB_CONFIG = "https://github-proxy.wvservices.com/wavesplatform/waves-client-config/"
    const val URL_GITHUB_CONFIG_VERSION = "master/version_android.json"
    const val URL_GITHUB_CONFIG_SPAM_FILE = "master/Scam%20tokens%20according%20to%20the%20opinion%20of%20Waves%20Community.csv/"
    const val URL_WAVES_FORUM = "https://forum.wavesplatform.com/"
    const val URL_TERMS = "https://wavesplatform.com/files/docs/Privacy%20Policy_SW.pdf"
    const val URL_TERMS_AND_CONDITIONS = "https://wavesplatform.com/files/docs/Waves_terms_and_conditions.pdf"
    const val URL_WHITEPAPER = "https://wavesplatform.com/files/whitepaper_v0.pdf"
    const val URL_TELEGRAM = "https://telegram.me/wavesnews"
    const val URL_TWITTER = "https://twitter.com/wavesplatform"
    const val URL_FACEBOOK = "https://www.facebook.com/wavesplatform"
    const val URL_DISCORD = "https://discordapp.com/invite/cnFmDyA"
    const val URL_REDDIT = "https://www.reddit.com/r/WavesPlatform/"

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

    val alphabetColor: IntArray = App.getAppContext().resources.getIntArray(R.array.abc_colors)

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

    var MRTGeneralAsset = GlobalConfigurationResponse.ConfigAsset(
            assetId = "4uK8i4ThRGbehENwa6MxyLtxAjAo1Rj9fduborGExarC",
            gatewayId = "MRT",
            displayName = "MinersReward")

    var WCTGeneralAsset = GlobalConfigurationResponse.ConfigAsset(
            assetId = "DHgwrRvVyqJsepd32YbBqUeDH4GJ1N984X8QoekjgH8J",
            gatewayId = "WCT",
            displayName = "WavesCommunity")
}
