package com.wavesplatform.wallet.v2.util

import android.app.Application
import com.google.gson.Gson
import com.wavesplatform.sdk.net.model.response.GlobalConfigurationResponse
import com.wavesplatform.wallet.App
import java.io.IOException
import java.nio.charset.Charset

class ClientEnvironment internal constructor(
        val name: String,
        val url: String,
        jsonFileName: String) {

    var configuration: GlobalConfigurationResponse

    init {
        configuration = Gson().fromJson(
                loadJsonFromAsset(App.getAppContext(), jsonFileName),
                GlobalConfigurationResponse::class.java)
    }

    private fun loadJsonFromAsset(application: Application, fileName: String): String {
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

    companion object {

        const val KEY_ENV_TEST_NET = "env_testnet"
        const val KEY_ENV_MAIN_NET = "env_prod"

        private const val FILENAME_TEST_NET = "environment_testnet.json"
        private const val FILENAME_MAIN_NET = "environment_mainnet.json"

        private const val URL_CONFIG_MAIN_NET = "https://github-proxy.wvservices.com/" +
                "wavesplatform/waves-client-config/${EnvironmentManager.BRANCH}/environment_mainnet.json"
        private const val URL_CONFIG_TEST_NET = "https://github-proxy.wvservices.com/" +
                "wavesplatform/waves-client-config/${EnvironmentManager.BRANCH}/environment_testnet.json"

        var TEST_NET = ClientEnvironment(KEY_ENV_TEST_NET, URL_CONFIG_TEST_NET, FILENAME_TEST_NET)
        var MAIN_NET = ClientEnvironment(KEY_ENV_MAIN_NET, URL_CONFIG_MAIN_NET, FILENAME_MAIN_NET)

        internal var environments: MutableList<ClientEnvironment> = mutableListOf()

        init {
            environments.add(TEST_NET)
            environments.add(MAIN_NET)
        }
    }
}