package com.wavesplatform.sdk

import android.app.Application
import android.util.Log
import com.wavesplatform.sdk.manager.base.BaseDataManager
import java.util.*

class Wavesplatform private constructor() {

    var dataManager: BaseDataManager = BaseDataManager()
    var cookies: HashSet<String> = hashSetOf()
    private var wavesWallet: WavesWallet? = null

    fun createWallet(seed: String): String {

        return try {
            wavesWallet = WavesWallet(seed.toByteArray(Charsets.UTF_8))
            val guid = UUID.randomUUID().toString()
            guid
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "storeWalletData: ", e)
            ""
        }
    }

    fun createWallet(encrypted: String, password: String): String {
        return try {
            wavesWallet = WavesWallet(encrypted, password)
            val guid = UUID.randomUUID().toString()
            guid
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "storeWalletData: ", e)
            ""
        }
    }

    fun getWallet(): WavesWallet? {
        return wavesWallet
    }

    fun resetWallet() {
        wavesWallet = null
    }

    companion object {

        private var instance: Wavesplatform? = null

        @JvmStatic
        fun init(app: Application) {
            if (instance == null) {
                instance = Wavesplatform()
            }
        }

        @JvmStatic
        fun get(): Wavesplatform {
            if (instance == null) {
                throw Exception("Wavesplatform must be init() first!")
            } else {
                return instance!!
            }
        }
    }
}