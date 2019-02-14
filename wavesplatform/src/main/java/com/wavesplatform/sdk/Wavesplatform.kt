package com.wavesplatform.sdk

import android.content.Context
import android.util.Log
import com.wavesplatform.sdk.service.DataManager
import com.wavesplatform.sdk.service.*
import retrofit2.CallAdapter
import java.util.*

class Wavesplatform private constructor(var context: Context, factory: CallAdapter.Factory?) {

    private var dataManager: DataManager = DataManager(context, factory)
    private var cookies: HashSet<String> = hashSetOf()
    private var wavesWallet: WavesWallet? = null
    private var guid: String = UUID.randomUUID().toString()

    companion object {

        private var instance: Wavesplatform? = null

        @JvmStatic
        fun init(context: Context, factory: CallAdapter.Factory? = null) {
            instance = Wavesplatform(context, factory)
        }

        private fun get(): Wavesplatform {
            if (instance == null) {
                throw NullPointerException("Wavesplatform must be init first!")
            }
            return instance!!
        }

        fun generateSeed(): String {
            return WalletManager.createWalletSeed(Wavesplatform.get().context)
        }

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

        fun getWallet(): WavesWallet {
            if (get().wavesWallet == null) {
                throw NullPointerException("Wavesplatform wallet must be created first!")
            } else {
                return get().wavesWallet!!
            }
        }

        fun resetWallet() {
            get().wavesWallet = null
        }

        fun isAuthenticated(): Boolean {
            return get().wavesWallet != null
        }

        fun getAddress(): String {
            return Wavesplatform.getWallet().address
        }

        fun getPublicKeyStr(): String {
            return Wavesplatform.getWallet().publicKeyStr
        }

        fun getApiService(): ApiService {
            return Wavesplatform.get().dataManager.apiService
        }

        fun getSpamService(): SpamService {
            return Wavesplatform.get().dataManager.spamService
        }

        fun getCoinomatService(): CoinomatService {
            return Wavesplatform.get().dataManager.coinomatService
        }

        fun getMatcherService(): MatcherService {
            return Wavesplatform.get().dataManager.matcherService
        }

        fun getNodeService(): NodeService {
            return Wavesplatform.get().dataManager.nodeService
        }

        fun getCookies(): HashSet<String> {
            return Wavesplatform.get().cookies
        }

        fun setCookies(cookies: HashSet<String>) {
            Wavesplatform.get().cookies = cookies
        }

        fun setCallAdapterFactory(factory: CallAdapter.Factory) {
            Wavesplatform.get().dataManager.setCallAdapterFactory(factory)
        }
    }
}