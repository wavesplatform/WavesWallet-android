package com.wavesplatform.sdk

import android.app.Application
import android.util.Log
import com.wavesplatform.sdk.crypto.WalletManager
import com.wavesplatform.sdk.crypto.WavesWallet
import com.wavesplatform.sdk.net.CallAdapterFactory
import com.wavesplatform.sdk.net.DataManager
import com.wavesplatform.sdk.net.OnErrorListener
import com.wavesplatform.sdk.net.service.*
import com.wavesplatform.sdk.utils.EnvironmentManager
import retrofit2.CallAdapter
import java.util.*

class Wavesplatform private constructor(var context: Application, factory: CallAdapter.Factory?) {

    private var dataManager: DataManager = DataManager(context, factory)
    private var wavesWallet: WavesWallet? = null
    private var guid: String = UUID.randomUUID().toString()

    companion object {

        private var instance: Wavesplatform? = null

        /**
         * Initialisation Wavesplatform method must be call first.
         * @param application Application context ot the app
         * @param mainNet Optional parameter. Default true. Define net to use.
         * Default true means use MainNet. False - TestNet
         * @param factory Optional parameter. Add a call adapter factory
         * for supporting service method return types other than Call
         */
        @JvmStatic
        fun init(application: Application, mainNet: Boolean = true, factory: CallAdapterFactory? = null) {
            EnvironmentManager.init(application)
            if (!mainNet) {
                EnvironmentManager.setCurrentEnvironment(EnvironmentManager.Environment.TEST_NET)
            }
            instance = Wavesplatform(application, factory)
            EnvironmentManager.updateConfiguration(
                    getGithubService().globalConfiguration(EnvironmentManager.environment.url),
                    getApiService(),
                    getNodeService())
        }

        /**
         * Initialisation Wavesplatform method must be call first.
         * @param application Application context ot the app
         */
        @JvmStatic
        fun init(application: Application) {
            init(application, true, null)
        }

        @JvmStatic
        @Throws(NullPointerException::class)
        private fun get(): Wavesplatform {
            if (instance == null) {
                throw NullPointerException("Wavesplatform must be init first!")
            }
            return instance!!
        }

        /**
         * @return Generated random secret seed-phrase, seed recovery phrase or backup seed phrase
         * and contains 15 different words from dictionary.
         */
        @JvmStatic
        @Throws(NullPointerException::class)
        fun generateSeed(): String {
            return WalletManager.createWalletSeed(Wavesplatform.get().context)
        }

        /**
         * Create wallet from secret seed-phrase.
         * @param seed secret seed-phrase
         * @param guid
         */
        @JvmStatic
        fun createWallet(seed: String,
                         guid: String = UUID.randomUUID().toString()): String {
            return try {
                get().wavesWallet = WavesWallet(seed.toByteArray(Charsets.UTF_8))
                get().guid = guid
                guid
            } catch (e: Exception) {
                Log.e("Wavesplatform", "Error create Wavesplatform wallet from seed: ", e)
                e.printStackTrace()
                ""
            }
        }

        /**
         * Create wallet from secret seed-phrase.
         */
        @JvmStatic
        fun createWallet(encrypted: String,
                         password: String,
                         guid: String = UUID.randomUUID().toString()): String {
            return try {
                get().wavesWallet = WavesWallet(encrypted, password)
                get().guid = guid
                guid
            } catch (e: Exception) {
                Log.e("Wavesplatform", "Error create Wavesplatform wallet from encrypted data: ", e)
                e.printStackTrace()
                ""
            }
        }

        @JvmStatic
        @Throws(NullPointerException::class)
        fun getWallet(): WavesWallet {
            if (get().wavesWallet == null) {
                throw NullPointerException("Wavesplatform wallet must be created first!")
            } else {
                return get().wavesWallet!!
            }
        }

        @JvmStatic
        fun resetWallet() {
            get().wavesWallet = null
        }

        @JvmStatic
        fun isAuthenticated(): Boolean {
            return get().wavesWallet != null
        }

        @JvmStatic
        fun getAddress(): String {
            return Wavesplatform.getWallet().address
        }

        @JvmStatic
        fun getPublicKeyStr(): String {
            return Wavesplatform.getWallet().publicKeyStr
        }

        @JvmStatic
        fun getApiService(): ApiService {
            return Wavesplatform.get().dataManager.apiService
        }

        @JvmStatic
        fun getGithubService(): GithubService {
            return Wavesplatform.get().dataManager.githubService
        }

        @JvmStatic
        fun getCoinomatService(): CoinomatService {
            return Wavesplatform.get().dataManager.coinomatService
        }

        @JvmStatic
        fun getMatcherService(): MatcherService {
            return Wavesplatform.get().dataManager.matcherService
        }

        @JvmStatic
        fun getNodeService(): NodeService {
            return Wavesplatform.get().dataManager.nodeService
        }

        @JvmStatic
        fun setOnErrorListener(errorListener: OnErrorListener) {
            Wavesplatform.get().dataManager.setCallAdapterFactory(CallAdapterFactory(errorListener))
        }
    }
}