package com.wavesplatform.wallet.v2.util

import android.app.Application
import com.google.gson.Gson
import com.wavesplatform.wallet.v2.data.model.service.cofigs.GlobalConfigurationResponse
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.local.EnvironmentExternalProperties
import com.wavesplatform.wallet.v2.util.EnvironmentManager.Companion.URL_CONFIG_MAIN_NET
import com.wavesplatform.wallet.v2.util.EnvironmentManager.Companion.URL_CONFIG_TEST_NET
import com.wavesplatform.wallet.v2.util.EnvironmentManager.Companion.URL_RAW_CONFIG_MAIN_NET
import com.wavesplatform.wallet.v2.util.EnvironmentManager.Companion.URL_RAW_CONFIG_TEST_NET
import java.io.IOException
import java.nio.charset.Charset

class ClientEnvironment internal constructor(
        val name: String,
        val url: String,
        val rawUrl: String,
        jsonFileName: String,
        val externalProperties: EnvironmentExternalProperties) {

    var configuration: GlobalConfigurationResponse

    init {
        configuration = Gson().fromJson(
                loadJsonFromAsset(App.getAppContext(), jsonFileName),
                GlobalConfigurationResponse::class.java)
    }

    internal fun setConfiguration(configuration: GlobalConfigurationResponse) {
        this.configuration = configuration
    }

    companion object {

        const val KEY_ENV_TEST_NET = "env_testnet"
        const val KEY_ENV_MAIN_NET = "env_prod"

        private const val FILENAME_TEST_NET = "environment_testnet.json"
        private const val FILENAME_MAIN_NET = "environment_mainnet.json"

        var TEST_NET = ClientEnvironment(KEY_ENV_TEST_NET, URL_CONFIG_TEST_NET,
                URL_RAW_CONFIG_MAIN_NET, FILENAME_TEST_NET,
                EnvironmentExternalProperties(Constants.Vostok.TEST_NET_CODE, Constants.Fiat.TestNet.USD_ID, Constants.Fiat.TestNet.EUR_ID))
        var MAIN_NET = ClientEnvironment(KEY_ENV_MAIN_NET, URL_CONFIG_MAIN_NET,
                URL_RAW_CONFIG_TEST_NET, FILENAME_MAIN_NET,
                EnvironmentExternalProperties(Constants.Vostok.MAIN_NET_CODE, Constants.Fiat.MainNet.USD_ID, Constants.Fiat.MainNet.EUR_ID))


        internal var environments: MutableList<ClientEnvironment> = mutableListOf()

        init {
            environments.add(TEST_NET)
            environments.add(MAIN_NET)
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