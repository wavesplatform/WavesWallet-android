/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.util

import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.wavesplatform.sdk.WavesSdk
import com.wavesplatform.sdk.crypto.WavesCrypto
import com.wavesplatform.sdk.model.request.data.AssetsRequest
import com.wavesplatform.sdk.model.response.data.AssetsInfoDataResponse
import com.wavesplatform.sdk.model.response.data.AssetsInfoResponse
import com.wavesplatform.sdk.model.response.node.AssetBalanceResponse
import com.wavesplatform.sdk.model.response.node.IssueTransactionResponse
import com.wavesplatform.sdk.model.response.node.UtilsTimeResponse
import com.wavesplatform.sdk.utils.Environment
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.sdk.utils.WavesConstants
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.BuildConfig
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.local.PreferencesHelper
import com.wavesplatform.wallet.v2.data.manager.GithubServiceManager
import com.wavesplatform.wallet.v2.data.model.service.configs.GlobalConfigurationResponse
import com.wavesplatform.wallet.v2.data.remote.GithubService
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import pers.victor.ext.currentTimeMillis
import timber.log.Timber
import kotlin.math.abs
import kotlin.system.exitProcess

class EnvironmentManager(var current: ClientEnvironment) {

    private var configurationDisposable: Disposable? = null
    private var versionDisposable: Disposable? = null
    private var devConfigDisposable: Disposable? = null
    private var gateWayHostInterceptor: HostInterceptor? = null
    private val onUpdateCompleteListeners: MutableList<OnUpdateCompleteListener> = mutableListOf()
    private var updateCompleted = false

    companion object {
        private const val BASE_RAW_CONFIG_URL = "https://raw.githubusercontent.com/"

        private const val BRANCH = "master"

        const val URL_CONFIG_MAIN_NET = BASE_RAW_CONFIG_URL +
                "wavesplatform/waves-client-config/$BRANCH/${ClientEnvironment.FILENAME_MAIN_NET}"
        const val URL_CONFIG_TEST_NET = BASE_RAW_CONFIG_URL +
                "wavesplatform/waves-client-config/$BRANCH/${ClientEnvironment.FILENAME_TEST_NET}"
        const val URL_CONFIG_STAGE_NET = BASE_RAW_CONFIG_URL +
                "wavesplatform/waves-client-config/$BRANCH/${ClientEnvironment.FILENAME_STAGE_NET}"
        const val URL_COMMISSION_MAIN_NET = BASE_RAW_CONFIG_URL +
                "wavesplatform/waves-client-config/$BRANCH/${Constants.GITHUB_FEE_FILE}"

        private var instance: EnvironmentManager? = null
        private val handler = Handler()

        private const val GLOBAL_CURRENT_ENVIRONMENT_DATA = "global_current_environment_data"
        private const val GLOBAL_CURRENT_ENVIRONMENT = "global_current_environment"
        private const val GLOBAL_CURRENT_TIME_CORRECTION = "global_current_time_correction"

        val netCode: Byte
            get() = environment.configuration.scheme[0].toByte()

        val wavesEnterpriseNetCode: Byte
            get() = environment.externalProperties.wavesEnterpriseNetCode.toByte()

        val globalConfiguration: GlobalConfigurationResponse
            get() = environment.configuration

        val name: String
            get() = environment.name

        @JvmStatic
        val servers: GlobalConfigurationResponse.Servers
            get() = environment.configuration.servers

        val defaultAssets = mutableListOf<AssetBalanceResponse>()

        val environment: ClientEnvironment
            get() = instance!!.current

        val environmentName: String?
            get() {
                val preferenceManager = PreferenceManager
                        .getDefaultSharedPreferences(App.appContext)
                return preferenceManager.getString(
                        GLOBAL_CURRENT_ENVIRONMENT, ClientEnvironment.MAIN_NET.name)
            }

        fun getTime(): Long {
            val timeCorrection = if (instance == null) {
                0L
            } else {
                PreferenceManager
                        .getDefaultSharedPreferences(App.appContext)
                        .getLong(GLOBAL_CURRENT_TIME_CORRECTION, 0L)
            }
            return currentTimeMillis + timeCorrection
        }

        fun getMatcherAddress(): String {
            return instance!!.current.externalProperties.matcherAddress
        }

        fun setCurrentEnvironment(current: ClientEnvironment) {
            PreferenceManager.getDefaultSharedPreferences(App.appContext)
                    .edit()
                    .putString(GLOBAL_CURRENT_ENVIRONMENT, current.name)
                    .remove(GLOBAL_CURRENT_ENVIRONMENT_DATA)
                    .apply()
            restartApp(App.appContext)
        }


        @JvmStatic
        fun update() {
            var initEnvironment: ClientEnvironment = ClientEnvironment.MAIN_NET
            for (environment in ClientEnvironment.environments) {
                if (environmentName!!.equals(environment.name, ignoreCase = true)) {
                    initEnvironment = environment
                    break
                }
            }
            instance = EnvironmentManager(initEnvironment)

            getDefaultConfig()

            val timeCorrection = PreferenceManager
                    .getDefaultSharedPreferences(App.appContext)
                    .getLong(GLOBAL_CURRENT_TIME_CORRECTION, 0)
            val config = getLocalSavedConfig()

            val environment = Environment(
                    Environment.Server.Custom(
                            config.servers.nodeUrl,
                            config.servers.matcherUrl,
                            config.servers.dataUrl,
                            config.scheme[0].toByte()),
                    timeCorrection)

            WavesSdk.setEnvironment(environment)

            loadConfiguration(GithubServiceManager.create(null))
        }

        fun getDefaultConfig(): GlobalConfigurationResponse? {
            return when (environmentName) {
                ClientEnvironment.KEY_ENV_MAIN_NET -> {
                    Gson().fromJson(
                            ClientEnvironment.loadJsonFromAsset(App.appContext, ClientEnvironment.FILENAME_MAIN_NET),
                            GlobalConfigurationResponse::class.java)
                }
                ClientEnvironment.KEY_ENV_TEST_NET -> {
                    Gson().fromJson(
                            ClientEnvironment.loadJsonFromAsset(App.appContext, ClientEnvironment.FILENAME_TEST_NET),
                            GlobalConfigurationResponse::class.java)
                }
                ClientEnvironment.KEY_ENV_STAGE_NET -> {
                    Gson().fromJson(
                            ClientEnvironment.loadJsonFromAsset(App.appContext, ClientEnvironment.FILENAME_STAGE_NET),
                            GlobalConfigurationResponse::class.java)
                }
                else -> {
                    Gson().fromJson(
                            ClientEnvironment.loadJsonFromAsset(App.appContext, ClientEnvironment.FILENAME_TEST_NET),
                            GlobalConfigurationResponse::class.java)
                }
            }
        }

        fun createGateWayHostInterceptor(): HostInterceptor {
            instance!!.gateWayHostInterceptor = HostInterceptor(servers.gatewayUrl)
            return instance!!.gateWayHostInterceptor!!
        }

        private fun loadConfiguration(githubService: GithubService) {
            instance?.updateCompleted = false
            instance!!.configurationDisposable =
                    Observable.zip(
                            githubService.globalConfiguration(environment.url),
                            WavesSdk.service().getNode().utilsTime(),
                            BiFunction { conf: GlobalConfigurationResponse, time: UtilsTimeResponse ->
                                return@BiFunction Pair(conf, time)
                            })
                            .onErrorReturn {
                                Timber.e(it, "EnvironmentManager: Can't download global configuration!")
                                val time = currentTimeMillis + PreferenceManager
                                        .getDefaultSharedPreferences(App.appContext)
                                        .getLong(GLOBAL_CURRENT_TIME_CORRECTION, 0L)
                                Pair(environment.configuration, UtilsTimeResponse(time, time))
                            }
                            .map { pair ->
                                val timeCorrection = pair.second.ntp - currentTimeMillis
                                setTimeCorrection(timeCorrection)
                                setConfiguration(pair.first)

                                val environment = Environment(
                                        Environment.Server.Custom(
                                                pair.first.servers.nodeUrl,
                                                pair.first.servers.matcherUrl,
                                                pair.first.servers.dataUrl,
                                                pair.first.scheme[0].toByte()),
                                        timeCorrection)

                                WavesSdk.setEnvironment(environment)
                                pair.first.servers.gatewayUrl

                                globalConfiguration.generalAssets.map { it.assetId }
                            }
                            .flatMap { WavesSdk.service().getDataService().assets(AssetsRequest(ids = it)) }
                            .map { info ->
                                setDefaultAssets(info)
                            }
                            .onErrorReturn {
                                Timber.e(it, "EnvironmentManager: Can't download general assets!")
                                val info = AssetsInfoResponse()
                                val list = mutableListOf<AssetsInfoDataResponse>()
                                environment.configuration.generalAssets.forEach { generalAsset ->
                                    val asset = AssetsInfoDataResponse()
                                    asset.assetInfo.id = generalAsset.assetId
                                    asset.assetInfo.name = generalAsset.displayName
                                    asset.assetInfo.precision = if (generalAsset.isFiat) {
                                        2
                                    } else {
                                        8
                                    }
                                    list.add(asset)
                                }
                                info.data = list
                                setDefaultAssets(info)
                            }
                            .flatMap {
                                WavesSdk.service().getMatcher().matcherPublicKey()
                            }
                            .map { matcherPublicKey ->
                                instance!!.current.externalProperties
                                        .matcherAddress = WavesCrypto.addressFromPublicKey(
                                        WavesCrypto.base58decode(matcherPublicKey
                                                .replace("\"", "")),
                                        netCode)
                            }
                            .onErrorReturn {
                                Timber.e(it, "EnvironmentManager: Can't download matcher address!")
                            }
                            .compose(RxUtil.applyObservableDefaultSchedulers())
                            .subscribe({
                                instance?.updateCompleted = true
                                instance?.onUpdateCompleteListeners?.forEach {
                                    it.onComplete()
                                }
                                instance!!.configurationDisposable!!.dispose()
                            }, { error ->
                                Timber.e(error, "EnvironmentManager: Can't download GlobalConfiguration!")
                                error.printStackTrace()
                                setConfiguration(environment.configuration)
                                instance?.updateCompleted = true
                                instance?.onUpdateCompleteListeners?.forEach {
                                    it.onError()
                                }
                                instance!!.configurationDisposable!!.dispose()
                            })

            instance!!.versionDisposable = githubService.loadLastAppVersion(Constants.URL_GITHUB_CONFIG_VERSION)
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({ version ->
                        val prefs = PreferencesHelper(App.appContext)
                        prefs.lastAppVersion = version.lastVersion
                        instance!!.versionDisposable!!.dispose()
                    }, { error ->
                        error.printStackTrace()
                        instance!!.versionDisposable!!.dispose()
                    })

            val preferenceHelper = PreferencesHelper(App.appContext)
            val devConfigPath = if (preferenceHelper.useTest) {
                Constants.URL_GITHUB_TEST_CONFIG_DEV
            } else {
                Constants.URL_GITHUB_CONFIG_DEV
            }

            instance!!.devConfigDisposable = githubService.devConfig(devConfigPath)
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({ devConfig ->
                        val prefs = PreferencesHelper(App.appContext)
                        prefs.forceUpdateAppVersion = devConfig.forceUpdateVersion ?: BuildConfig.VERSION_NAME
                        prefs.serviceAvailable = devConfig.serviceAvailable ?: true
                        prefs.matcherSwapTimestamp = devConfig.matcherSwapTimestamp ?: 1575288000L
                        instance!!.devConfigDisposable!!.dispose()
                    }, { error ->
                        error.printStackTrace()
                        instance!!.devConfigDisposable!!.dispose()
                    })
        }


        fun findAssetIdByAssetId(assetId: String): GlobalConfigurationResponse.ConfigAsset? {
            return instance?.current?.configuration?.generalAssets?.firstOrNull { it.assetId == assetId }
        }

        fun addOnUpdateCompleteListener(listener: OnUpdateCompleteListener) {
            if (instance?.onUpdateCompleteListeners?.contains(listener) != true) {
                instance?.onUpdateCompleteListeners?.add(listener)
            }
        }

        fun removeOnUpdateCompleteListener(listener: OnUpdateCompleteListener) {
            instance?.onUpdateCompleteListeners?.remove(listener)
        }

        fun isUpdateCompleted(): Boolean {
            return instance?.updateCompleted ?: false
        }

        private fun getLocalSavedConfig(): GlobalConfigurationResponse {

            val preferences = PreferenceManager.getDefaultSharedPreferences(App.appContext)
            val currentEnvName = preferences.getString(
                    GLOBAL_CURRENT_ENVIRONMENT, ClientEnvironment.MAIN_NET.name)

            return when (currentEnvName) {
                ClientEnvironment.MAIN_NET.name -> getConfiguration(preferences, ClientEnvironment.MAIN_NET)
                ClientEnvironment.TEST_NET.name -> getConfiguration(preferences, ClientEnvironment.TEST_NET)
                ClientEnvironment.STAGE_NET.name -> getConfiguration(preferences, ClientEnvironment.STAGE_NET)
                else -> getConfiguration(preferences, ClientEnvironment.MAIN_NET)
            }
        }

        private fun getConfiguration(preferences: SharedPreferences, environment: ClientEnvironment)
                : GlobalConfigurationResponse {
            return if (preferences.contains(GLOBAL_CURRENT_ENVIRONMENT_DATA)) {
                val json = preferences.getString(
                        GLOBAL_CURRENT_ENVIRONMENT_DATA,
                        Gson().toJson(environment.configuration))
                Gson().fromJson(json, GlobalConfigurationResponse::class.java)
            } else {
                environment.configuration
            }
        }

        private fun setTimeCorrection(timeCorrection: Long) {
            if (abs(timeCorrection) > 30_000) {
                PreferenceManager
                        .getDefaultSharedPreferences(App.appContext)
                        .edit()
                        .putLong(GLOBAL_CURRENT_TIME_CORRECTION,
                                timeCorrection)
                        .apply()
            }
        }

        private fun setDefaultAssets(info: AssetsInfoResponse) {
            defaultAssets.clear()
            for (assetInfo in info.data) {
                val assetBalance = AssetBalanceResponse(
                        assetId = if (assetInfo.assetInfo.id == WavesConstants.WAVES_ASSET_ID_FILLED) {
                            WavesConstants.WAVES_ASSET_ID_EMPTY
                        } else {
                            assetInfo.assetInfo.id
                        },
                        quantity = assetInfo.assetInfo.quantity,
                        isFavorite = assetInfo.assetInfo.id == WavesConstants.WAVES_ASSET_ID_FILLED,
                        issueTransaction = IssueTransactionResponse(
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

        private fun setConfiguration(globalConfiguration: GlobalConfigurationResponse) {
            if (instance!!.gateWayHostInterceptor == null) {
                createGateWayHostInterceptor()
            }
            instance!!.gateWayHostInterceptor!!.setHost(globalConfiguration.servers.gatewayUrl)
            PreferenceManager
                    .getDefaultSharedPreferences(App.appContext)
                    .edit()
                    .putString(GLOBAL_CURRENT_ENVIRONMENT_DATA,
                            Gson().toJson(globalConfiguration))
                    .apply()
            instance!!.current.configuration = globalConfiguration
        }

        fun restartApp(application: Application) {
            handler.postDelayed({
                val packageManager = application.packageManager
                val intent = packageManager.getLaunchIntentForPackage(application.packageName)
                if (intent != null) {
                    val componentName = intent.component
                    val mainIntent = Intent.makeRestartActivityTask(componentName)
                    application.startActivity(mainIntent)
                    exitProcess(0)
                }
            }, 300)
        }

        interface OnUpdateCompleteListener {
            fun onComplete()
            fun onError()
        }
    }
}