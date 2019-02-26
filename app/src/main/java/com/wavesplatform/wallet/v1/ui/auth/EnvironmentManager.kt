package com.wavesplatform.wallet.v1.ui.auth

import android.app.Application
import android.content.Intent
import android.os.Handler
import android.preference.PreferenceManager
import android.text.TextUtils

import com.google.gson.Gson
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.manager.GithubDataManager
import com.wavesplatform.wallet.v2.data.model.remote.response.GlobalConfiguration
import com.wavesplatform.wallet.v2.injection.module.HostSelectionInterceptor

import java.util.ArrayList

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class EnvironmentManager {

    private var current: Environment? = null
    private var application: Application? = null
    private var disposable: Disposable? = null
    private var interceptor: HostSelectionInterceptor? = null

    class Environment internal constructor(val name: String, val url: String, json: String) {
        var configuration: GlobalConfiguration? = null

        init {
            this.configuration = Gson().fromJson(json, GlobalConfiguration::class.java)
        }

        internal fun setConfiguration(configuration: GlobalConfiguration) {
            this.configuration = configuration
        }

        companion object {

            internal var environments: MutableList<Environment> = ArrayList()
            var TEST_NET = Environment(
                    KEY_ENV_TEST_NET, URL_CONFIG_TEST_NET, EnvironmentConstants.TEST_NET_JSON)
            var MAIN_NET = Environment(
                    KEY_ENV_MAIN_NET, URL_CONFIG_MAIN_NET, EnvironmentConstants.MAIN_NET_JSON)

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

        const val KEY_ENV_MAIN_NET = "env_prod"
        const val URL_CONFIG_TEST_NET = "https://github-proxy.wvservices.com/" +
                "wavesplatform/waves-client-config/master/environment_testnet.json"

        const val URL_COMMISSION_MAIN_NET = "https://github-proxy.wvservices.com/" +
                "wavesplatform/waves-client-config/master/fee.json"

        private var instance: EnvironmentManager? = null
        private val handler = Handler()

        fun init(application: Application) {
            instance = EnvironmentManager()
            instance!!.application = application

            val envName = environmentName
            if (!TextUtils.isEmpty(envName)) {
                for (environment in Environment.environments) {
                    if (envName!!.equals(environment.name, ignoreCase = true)) {
                        val preferenceManager = PreferenceManager
                                .getDefaultSharedPreferences(instance!!.application)
                        val json = preferenceManager.getString(
                                PrefsUtil.GLOBAL_CURRENT_ENVIRONMENT_DATA,
                                EnvironmentConstants.MAIN_NET_JSON)
                        environment.setConfiguration(Gson().fromJson(json, GlobalConfiguration::class.java))
                        instance!!.current = environment
                    }
                }
            }
        }

        fun createHostInterceptor(): HostSelectionInterceptor {
            instance!!.interceptor = HostSelectionInterceptor(environment.configuration!!.servers)
            return instance!!.interceptor!!
        }

        fun updateConfiguration(githubDataManager: GithubDataManager) {
            instance!!.disposable = githubDataManager.globalConfiguration(EnvironmentManager.environment.url)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ globalConfiguration ->
                        instance!!.interceptor!!.setHosts(globalConfiguration.servers)
                        val preferenceManager = PreferenceManager
                                .getDefaultSharedPreferences(App.getAppContext())
                        val editor = preferenceManager.edit()
                        editor.putString(PrefsUtil.GLOBAL_CURRENT_ENVIRONMENT_DATA,
                                Gson().toJson(globalConfiguration))
                                .apply()
                        instance!!.current!!.setConfiguration(globalConfiguration)
                        instance!!.disposable!!.dispose()
                    }, { error ->
                        Timber.e(error, "EnvironmentManager: Can't download GlobalConfiguration")
                        error.printStackTrace()
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

        fun findAssetIdByAssetId(assetId: String): GlobalConfiguration.GeneralAssetId? {
            for (asset in instance!!.current!!.configuration!!.generalAssetIds) {
                if (asset.assetId == assetId) {
                    return asset
                }
            }
            return null
        }

        val netCode: Byte
            get() = environment.configuration!!.scheme[0].toByte()

        val globalConfiguration: GlobalConfiguration
            get() = environment.configuration!!

        val name: String
            get() = environment.name

        val servers: GlobalConfiguration.Servers
            get() = environment.configuration!!.servers

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