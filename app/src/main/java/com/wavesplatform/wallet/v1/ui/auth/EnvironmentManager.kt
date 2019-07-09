/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

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
import com.wavesplatform.wallet.v2.data.model.local.EnvironmentExternalProperties
import com.wavesplatform.wallet.v2.data.model.remote.request.AssetsInfoRequest
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetsInfoResponse
import com.wavesplatform.wallet.v2.data.model.remote.response.GlobalConfiguration
import com.wavesplatform.wallet.v2.data.model.remote.response.IssueTransaction
import com.wavesplatform.wallet.v2.injection.module.HostSelectionInterceptor
import com.wavesplatform.wallet.v2.util.MonkeyTest
import com.wavesplatform.wallet.v2.util.RxUtil
import io.reactivex.disposables.Disposable
import pers.victor.ext.currentTimeMillis
import timber.log.Timber
import java.io.IOException
import java.nio.charset.Charset

class EnvironmentManager {

    private var current: Environment? = null
    private var application: Application? = null
    private var configurationDisposable: Disposable? = null
    private var timeDisposable: Disposable? = null
    private var versionDisposable: Disposable? = null
    private var interceptor: HostSelectionInterceptor? = null

    class Environment internal constructor(val name: String, val url: String, val rawUrl: String, jsonFileName: String, val externalProperties: EnvironmentExternalProperties) {
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

            internal var environments: MutableList<Environment> = mutableListOf()
            var TEST_NET = Environment(KEY_ENV_TEST_NET, URL_CONFIG_TEST_NET, URL_RAW_CONFIG_MAIN_NET, FILENAME_TEST_NET, EnvironmentExternalProperties(Constants.Vostok.TEST_NET_CODE))
            var MAIN_NET = Environment(KEY_ENV_MAIN_NET, URL_CONFIG_MAIN_NET, URL_RAW_CONFIG_TEST_NET, FILENAME_MAIN_NET, EnvironmentExternalProperties(Constants.Vostok.MAIN_NET_CODE))

            init {
                environments.add(TEST_NET)
                environments.add(MAIN_NET)
            }
        }
    }


    companion object {
        private const val BASE_PROXY_CONFIG_URL = "https://github-proxy.wvservices.com/"
        private const val BASE_RAW_CONFIG_URL = "https://raw.githubusercontent.com/"

        private const val BRANCH = "mobile/v2.5"

        const val KEY_ENV_TEST_NET = "env_testnet"
        const val KEY_ENV_MAIN_NET = "env_prod"

        const val FILENAME_TEST_NET = "environment_testnet.json"
        const val FILENAME_MAIN_NET = "environment_mainnet.json"

        const val URL_CONFIG_MAIN_NET = BASE_PROXY_CONFIG_URL +
                "wavesplatform/waves-client-config/$BRANCH/environment_mainnet.json"
        const val URL_CONFIG_TEST_NET = BASE_PROXY_CONFIG_URL +
                "wavesplatform/waves-client-config/$BRANCH/environment_testnet.json"
        const val URL_COMMISSION_MAIN_NET = BASE_PROXY_CONFIG_URL +
                "wavesplatform/waves-client-config/$BRANCH/fee.json"

        const val URL_RAW_CONFIG_MAIN_NET = BASE_RAW_CONFIG_URL +
                "wavesplatform/waves-client-config/$BRANCH/environment_mainnet.json"
        const val URL_RAW_CONFIG_TEST_NET = BASE_RAW_CONFIG_URL +
                "wavesplatform/waves-client-config/$BRANCH/environment_testnet.json"
        const val URL_RAW_COMMISSION_MAIN_NET = BASE_RAW_CONFIG_URL +
                "wavesplatform/waves-client-config/$BRANCH/fee.json"

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

            instance!!.configurationDisposable = githubDataManager.globalConfiguration(environment.url)
                    .onErrorResumeNext(githubDataManager.globalConfiguration(environment.rawUrl)
                            .onErrorReturnItem(environment.configuration))
                    .map { globalConfiguration ->
                        setConfiguration(globalConfiguration)
                        globalConfiguration.generalAssets.map { it.assetId }
                    }
                    .flatMap { githubDataManager.apiService.assetsInfoByIds(AssetsInfoRequest(it)) }
                    .map { info ->
                        setDefaultAssets(info)
                    }
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({
                        MonkeyTest.startIfNeed()
                        instance!!.configurationDisposable!!.dispose()
                    }, { error ->
                        Timber.e(error, "EnvironmentManager: Can't download global configuration & set default assets!")
                        instance!!.configurationDisposable!!.dispose()
                    })

            instance!!.timeDisposable = githubDataManager.nodeService.utilsTime()
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({
                        val timeCorrection = it.ntp - currentTimeMillis
                        if (Math.abs(timeCorrection) > 30_000) {
                            PreferenceManager
                                    .getDefaultSharedPreferences(App.getAppContext())
                                    .edit()
                                    .putLong(PrefsUtil.KEY_GLOBAL_CURRENT_TIME_CORRECTION,
                                            timeCorrection)
                                    .apply()
                        }
                        instance!!.timeDisposable!!.dispose()
                    }, { error ->
                        Timber.e(error, "EnvironmentManager: Can't download time correction!")
                        error.printStackTrace()
                        instance!!.timeDisposable!!.dispose()
                    })

            instance!!.versionDisposable = githubDataManager.loadLastAppVersion()
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({ version ->
                        githubDataManager.preferencesHelper.lastAppVersion = version.lastVersion
                        instance!!.versionDisposable!!.dispose()
                    }, { error ->
                        error.printStackTrace()
                        instance!!.timeDisposable!!.dispose()
                    })
        }

        private fun setDefaultAssets(info: AssetsInfoResponse) {
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
                                assetId = assetInfo.assetInfo.id,
                                name = findAssetIdByAssetId(
                                        assetInfo.assetInfo.id)?.displayName
                                        ?: assetInfo.assetInfo.name,
                                decimals = assetInfo.assetInfo.precision,
                                quantity = assetInfo.assetInfo.quantity,
                                description = assetInfo.assetInfo.description,
                                sender = assetInfo.assetInfo.sender,
                                timestamp = assetInfo.assetInfo.timestamp.time),
                        isGateway = findAssetIdByAssetId(
                                assetInfo.assetInfo.id)?.isGateway ?: false,
                        isFiatMoney = findAssetIdByAssetId(
                                assetInfo.assetInfo.id)?.isFiat ?: false)
                defaultAssets.add(assetBalance)
            }
        }

        private fun setConfiguration(globalConfiguration: GlobalConfiguration) {
            instance!!.interceptor!!.setHosts(globalConfiguration.servers)
            PreferenceManager
                    .getDefaultSharedPreferences(App.getAppContext())
                    .edit()
                    .putString(PrefsUtil.GLOBAL_CURRENT_ENVIRONMENT_DATA,
                            Gson().toJson(globalConfiguration))
                    .apply()
            instance!!.current!!.setConfiguration(globalConfiguration)
        }

        @JvmStatic
        fun getTime(): Long {
            val timeCorrection = if (App.getAppContext() == null) {
                0L
            } else {
                PreferenceManager
                        .getDefaultSharedPreferences(App.getAppContext())
                        .getLong(PrefsUtil.KEY_GLOBAL_CURRENT_TIME_CORRECTION, 0L)
            }
            return currentTimeMillis + timeCorrection
        }

        fun setCurrentEnvironment(current: Environment) {
            PreferenceManager.getDefaultSharedPreferences(App.getAppContext())
                    .edit()
                    .putString(PrefsUtil.GLOBAL_CURRENT_ENVIRONMENT, current.name)
                    .remove(PrefsUtil.GLOBAL_CURRENT_ENVIRONMENT_DATA)
                    .apply()
            restartApp()
        }

        fun getDefaultConfig(): GlobalConfiguration? {
            return when (environmentName) {
                KEY_ENV_MAIN_NET -> {
                    Gson().fromJson(
                            loadJsonFromAsset(instance!!.application!!, FILENAME_MAIN_NET),
                            GlobalConfiguration::class.java)
                }
                KEY_ENV_TEST_NET -> {
                    Gson().fromJson(
                            loadJsonFromAsset(instance!!.application!!, FILENAME_TEST_NET),
                            GlobalConfiguration::class.java)
                }
                else -> {
                    Gson().fromJson(
                            loadJsonFromAsset(instance!!.application!!, FILENAME_TEST_NET),
                            GlobalConfiguration::class.java)
                }
            }
        }

        val environmentName: String?
            get() {
                val preferenceManager = PreferenceManager
                        .getDefaultSharedPreferences(instance!!.application)
                return preferenceManager.getString(
                        PrefsUtil.GLOBAL_CURRENT_ENVIRONMENT, KEY_ENV_MAIN_NET)
            }

        fun findAssetIdByAssetId(assetId: String): GlobalConfiguration.ConfigAsset? {
            return instance?.current?.configuration?.generalAssets?.firstOrNull { it.assetId == assetId }
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

        val vostokNetCode: Byte
            get() = environment.externalProperties.vostokNetCode.toByte()

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