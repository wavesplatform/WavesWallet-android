package com.wavesplatform.wallet.v1.ui.auth

import android.app.Application
import android.content.Intent
import android.os.Handler
import android.preference.PreferenceManager
import android.text.TextUtils
import com.google.gson.Gson
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.manager.GithubDataManager
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.GlobalConfiguration
import com.wavesplatform.wallet.v2.data.model.remote.response.IssueTransaction
import com.wavesplatform.wallet.v2.injection.module.HostSelectionInterceptor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.IOException
import java.nio.charset.Charset
import java.util.*

class EnvironmentManager {

    private var current: Environment? = null
    private var application: Application? = null
    private var disposable: Disposable? = null
    private var interceptor: HostSelectionInterceptor? = null

    class Environment internal constructor(val name: String, val url: String, jsonFileName: String) {
        var configuration: GlobalConfiguration? = null

        init {
            this.configuration = Gson().fromJson(
                    loadJsonFromAsset(instance!!.application!!, jsonFileName),
                    GlobalConfiguration::class.java)
        }

        internal fun setConfiguration(configuration: GlobalConfiguration) {
            this.configuration = configuration
        }

        companion object {

            internal var environments: MutableList<Environment> = ArrayList()
            var TEST_NET = Environment(KEY_ENV_TEST_NET, URL_CONFIG_TEST_NET, FILENAME_TEST_NET)
            var MAIN_NET = Environment(KEY_ENV_MAIN_NET, URL_CONFIG_MAIN_NET, FILENAME_MAIN_NET)

            init {
                environments.add(TEST_NET)
                environments.add(MAIN_NET)
            }
        }
    }

    companion object {

        const val KEY_ENV_TEST_NET = "env_testnet"
        const val URL_CONFIG_MAIN_NET = "https://github-proxy.wvservices.com/" +
                "wavesplatform/waves-client-config/master/environment_mainnet.json"
        const val FILENAME_TEST_NET = "environment_testnet.json"

        const val KEY_ENV_MAIN_NET = "env_prod"
        const val URL_CONFIG_TEST_NET = "https://github-proxy.wvservices.com/" +
                "wavesplatform/waves-client-config/master/environment_testnet.json"
        const val FILENAME_MAIN_NET = "environment_mainnet.json"

        const val URL_COMMISSION_MAIN_NET = "https://github-proxy.wvservices.com/" +
                "wavesplatform/waves-client-config/master/fee.json"

        private var instance: EnvironmentManager? = null
        private val handler = Handler()

        @JvmStatic
        fun init(application: Application) {
            instance = EnvironmentManager()
            instance!!.application = application

            val envName = environmentName
            if (!TextUtils.isEmpty(envName)) {
                for (environment in Environment.environments) {
                    if (envName!!.equals(environment.name, ignoreCase = true)) {
                        val preferenceManager = PreferenceManager
                                .getDefaultSharedPreferences(instance!!.application)
                        if (preferenceManager.contains(PrefsUtil.GLOBAL_CURRENT_ENVIRONMENT_DATA)) {
                            val json = preferenceManager.getString(
                                    PrefsUtil.GLOBAL_CURRENT_ENVIRONMENT_DATA,
                                    Gson().toJson(environment.configuration))
                            environment.setConfiguration(Gson()
                                    .fromJson(json, GlobalConfiguration::class.java))
                        } else {
                            environment.setConfiguration(environment.configuration!!)
                        }
                        instance!!.current = environment
                        break
                    }
                }
            }
        }

        fun createHostInterceptor(): HostSelectionInterceptor {
            instance!!.interceptor = HostSelectionInterceptor(servers)
            return instance!!.interceptor!!
        }

        @JvmStatic
        fun updateConfiguration(githubDataManager: GithubDataManager) {
            if (instance == null) {
                throw NullPointerException("EnvironmentManager must be init first!")
            }

            instance!!.disposable = githubDataManager.globalConfiguration(EnvironmentManager.environment.url)
                    .map { globalConfiguration ->
                        instance!!.interceptor!!.setHosts(globalConfiguration.servers)
                        PreferenceManager
                                .getDefaultSharedPreferences(App.getAppContext())
                                .edit()
                                .putString(PrefsUtil.GLOBAL_CURRENT_ENVIRONMENT_DATA,
                                        Gson().toJson(globalConfiguration))
                                .apply()
                        instance!!.current!!.setConfiguration(globalConfiguration)

                        val list = mutableListOf<String>()
                        for (asset in globalConfiguration.generalAssetIds) {
                            list.add(asset.assetId)
                        }
                        list
                    }
                    .flatMap { githubDataManager.apiService.assetsInfoByIds(it) }
                    .map { info ->
                        defaultAssets.clear()
                        for (assetInfo in info.data) {
                            val assetBalance = AssetBalance(
                                    assetId = if (assetInfo.assetInfo.id == Constants.WAVES_ASSET_ID_FILLED) {
                                        Constants.WAVES_ASSET_ID_EMPTY
                                    } else {
                                        assetInfo.assetInfo.id
                                    },
                                    quantity = assetInfo.assetInfo.quantity,
                                    isFavorite = assetInfo.assetInfo.id == Constants.WAVES_ASSET_ID_FILLED,
                                    issueTransaction = IssueTransaction(
                                            id = assetInfo.assetInfo.id,
                                            name = findAssetIdByAssetId(
                                                    assetInfo.assetInfo.id)?.displayName
                                                    ?: assetInfo.assetInfo.name,
                                            decimals = assetInfo.assetInfo.precision,
                                            quantity = assetInfo.assetInfo.quantity,
                                            timestamp = assetInfo.assetInfo.timestamp.time),
                                    isGateway = findAssetIdByAssetId(
                                            assetInfo.assetInfo.id)?.isGateway
                                            ?: false)
                            defaultAssets.add(assetBalance)
                        }
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        instance!!.disposable!!.dispose()
                    }, { error ->
                        Timber.e(error, "EnvironmentManager: Can't download GlobalConfiguration")
                        error.printStackTrace()
                        PreferenceManager
                                .getDefaultSharedPreferences(App.getAppContext())
                                .edit()
                                .putString(PrefsUtil.GLOBAL_CURRENT_ENVIRONMENT_DATA,
                                        Gson().toJson(EnvironmentManager.Environment.MAIN_NET.configuration))
                                .apply()
                        instance!!.disposable!!.dispose()
                    })
        }

        fun setCurrentEnvironment(current: Environment) {
            PreferenceManager.getDefaultSharedPreferences(App.getAppContext())
                    .edit()
                    .putString(PrefsUtil.GLOBAL_CURRENT_ENVIRONMENT, current.name)
                    .remove(PrefsUtil.GLOBAL_CURRENT_ENVIRONMENT_DATA)
                    .apply()
            restartApp()
        }

        val environmentName: String?
            get() {
                val preferenceManager = PreferenceManager
                        .getDefaultSharedPreferences(instance!!.application)
                return preferenceManager.getString(
                        PrefsUtil.GLOBAL_CURRENT_ENVIRONMENT, Environment.MAIN_NET.name)
            }

        private fun findAssetIdByAssetId(assetId: String): GlobalConfiguration.GeneralAssetId? {
            for (asset in instance!!.current!!.configuration!!.generalAssetIds) {
                if (asset.assetId == assetId) {
                    return asset
                }
            }
            return null
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

        val netCode: Byte
            get() = environment.configuration!!.scheme[0].toByte()

        val globalConfiguration: GlobalConfiguration
            get() = environment.configuration!!

        val name: String
            get() = environment.name

        @JvmStatic
        val servers: GlobalConfiguration.Servers
            get() = environment.configuration!!.servers

        val defaultAssets = mutableListOf<AssetBalance>()

        val environment: Environment
            get() = instance!!.current!!

        private fun restartApp() {
            handler.postDelayed({
                val packageManager = App.getAppContext().packageManager
                val intent = packageManager.getLaunchIntentForPackage(App.getAppContext().packageName)
                if (intent != null) {
                    val componentName = intent.component
                    val mainIntent = Intent.makeRestartActivityTask(componentName)
                    App.getAppContext().startActivity(mainIntent)
                    System.exit(0)
                }
            }, 300)
        }
    }
}