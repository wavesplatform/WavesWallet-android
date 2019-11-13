package com.wavesplatform.wallet.v2.util

import android.app.Application
import com.google.gson.Gson
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.local.EnvironmentExternalProperties
import com.wavesplatform.wallet.v2.data.model.service.configs.GlobalConfigurationResponse
import com.wavesplatform.wallet.v2.util.EnvironmentManager.Companion.URL_CONFIG_MAIN_NET
import com.wavesplatform.wallet.v2.util.EnvironmentManager.Companion.URL_CONFIG_STAGE_NET
import com.wavesplatform.wallet.v2.util.EnvironmentManager.Companion.URL_CONFIG_TEST_NET
import java.io.IOException
import java.nio.charset.Charset

class ClientEnvironment internal constructor(
        val name: String,
        val url: String,
        jsonFileName: String,
        val externalProperties: EnvironmentExternalProperties) {

    var configuration: GlobalConfigurationResponse

    init {
        configuration = Gson().fromJson(
                loadJsonFromAsset(App.appContext, jsonFileName),
                GlobalConfigurationResponse::class.java)
    }

    internal fun setConfiguration(configuration: GlobalConfigurationResponse) {
        this.configuration = configuration
    }

    companion object {
        const val KEY_ENV_STAGE_NET = "env_stagenet"
        const val KEY_ENV_TEST_NET = "env_testnet"
        const val KEY_ENV_MAIN_NET = "env_prod"

        const val FILENAME_TEST_NET = "environment_testnet.json"
        const val FILENAME_MAIN_NET = "environment_mainnet.json"
        const val FILENAME_STAGE_NET = "environment_stagenet.json"

        var MAIN_NET = ClientEnvironment(KEY_ENV_MAIN_NET, URL_CONFIG_MAIN_NET,
                FILENAME_MAIN_NET,
                EnvironmentExternalProperties(
                        R.string.environment_main_net,
                        Constants.WavesEnterprise.MAIN_NET_CODE,
                        Constants.Fiat.MainNet.USD_ID,
                        Constants.Fiat.MainNet.EUR_ID,
                        Constants.MatcherAddress.MAIN_NET))

        var TEST_NET = ClientEnvironment(KEY_ENV_TEST_NET,
                URL_CONFIG_TEST_NET, FILENAME_TEST_NET,
                EnvironmentExternalProperties(
                        R.string.environment_test_net,
                        Constants.WavesEnterprise.TEST_NET_CODE,
                        Constants.Fiat.TestNet.USD_ID,
                        Constants.Fiat.TestNet.EUR_ID,
                        Constants.MatcherAddress.TEST_NET))

        var STAGE_NET = ClientEnvironment(KEY_ENV_STAGE_NET,
                URL_CONFIG_STAGE_NET,
                FILENAME_STAGE_NET,
                EnvironmentExternalProperties(
                        R.string.environment_stage_net,
                        Constants.WavesEnterprise.STAGE_NET_CODE,
                        Constants.Fiat.StageNet.USD_ID,
                        Constants.Fiat.StageNet.EUR_ID,
                        Constants.MatcherAddress.STAGE_NET))

        internal var environments: MutableList<ClientEnvironment> = mutableListOf()

        init {
            environments.add(MAIN_NET)
            environments.add(TEST_NET)
            environments.add(STAGE_NET)
        }

        fun loadJsonFromAsset(application: Application, fileName: String): String {
            return try {
                val inputStream = application.assets.open(fileName)
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()
                String(buffer, Charset.defaultCharset())
            } catch (ex: IOException) {
                ex.printStackTrace()
                ""
            }
        }
    }
}