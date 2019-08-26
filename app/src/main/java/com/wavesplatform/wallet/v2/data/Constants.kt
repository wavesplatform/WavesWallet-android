/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data

import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.service.cofigs.GlobalConfigurationResponse
import com.wavesplatform.wallet.v2.util.EnvironmentManager

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
    const val URL_REDDIT = "https://www.reddit.com/r/WavesSdk/"

    const val ACC_TWITTER = "wavesplatform"
    const val ACC_TELEGRAM = "wavesnews"

    const val SUPPORT_EMAIL = "support@wavesplatform.com"
    const val SUPPORT_SITE = "https://support.wavesplatform.com/"
    const val PRODUCATION_PACKAGE_NAME = "com.wavesplatform.wallet"

    const val MIN_WAVES_SPONSORED_BALANCE: Double = 1.005


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

    object Vostok {
        const val PROD_GATEWAY_URL = "https://gateways-dev.wvservices.com/api/v1/"
        const val TEST_GATEWAY_URL = "https://gw.wavesplatform.com"
        const val MAIN_NET_CODE = 'V'
        const val TEST_NET_CODE = 'F'
    }

    object GatewayType {
        const val COINOMAT = "coinomat"
        const val GATEWAY = "gateway"
    }

    object Fiat {
        const val USD_SYMBOL = "$"
        const val EUR_SYMBOL = "€"

        object MainNet {
            const val USD_ID = "Ft8X1v1LTa1ABafufpaCWyVj8KkaxUWE6xBhW6sNFJck"
            const val EUR_ID = "Gtb1WRznfchDnTh37ezoDTJ4wcoKaRsKqKjJjy7nm2zU"
        }

        object TestNet {
            const val USD_ID = "D6N2rAqWN6ZCWnCeNFWLGqqjS6nJLeK4m19XiuhdDenr"
            const val EUR_ID = "AsuWyM9MUUsMmWkK7jS48L3ky6gA1pxx7QtEYPbfLjAJ"
        }
    }

    object MatcherAddress {
        const val MAIN_NET = "3PJaDyprvekvPXPuAtxrapacuDJopgJRaU3"
        const val TEST_NET = "3N8aZG6ZDfnh8YxS6aNcteobN8eXTWHaBBd"
    }

    val alphabetColor: IntArray = App.getAppContext().resources.getIntArray(R.array.abc_colors)

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

    fun defaultGateways(): Array<String> {
        return EnvironmentManager.defaultAssets
                .filter { it.isGateway && !it.isFiatMoney }
                .map { it.assetId }
                .toTypedArray()
    }

    fun defaultFiat(): Array<String> {
        return EnvironmentManager.defaultAssets
                .filter { it.isFiatMoney }
                .map { it.assetId }
                .toTypedArray()
    }

    val MrtGeneralAsset = GlobalConfigurationResponse.ConfigAsset(
            assetId = "4uK8i4ThRGbehENwa6MxyLtxAjAo1Rj9fduborGExarC",
            gatewayId = "MRT",
            displayName = "MinersReward")

    val WctGeneralAsset = GlobalConfigurationResponse.ConfigAsset(
            assetId = "DHgwrRvVyqJsepd32YbBqUeDH4GJ1N984X8QoekjgH8J",
            gatewayId = "WCT",
            displayName = "WavesCommunity")

    val VstGeneralAsset = GlobalConfigurationResponse.ConfigAsset(
            assetId = "4LHHvYGNKJUg5hj65aGD5vgScvCBmLpdRFtjokvCjSL8",
            gatewayId = "VST",
            displayName = "Vostok") // github returns gatewayId = "Vostok"

    // HistoryTransactionResponse view types non HistoryTransactionResponse block-chain type
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
    const val ID_SCRIPT_INVOCATION_TYPE = 24
}
