package com.wavesplatform.sdk

import android.app.Application
import android.util.Log
import com.wavesplatform.sdk.manager.base.BaseDataManager
import com.wavesplatform.sdk.service.ApiService
import java.util.*
import javax.inject.Inject

class Wavesplatform private constructor(private var app: Application) {

    private var wavesWallet: WavesWallet? = null
    var cookies: HashSet<String> = hashSetOf()
    lateinit var loader: BaseDataManager

    fun createWallet(seed: String, password: String, name: String = ""): String {
        loader = BaseDataManager()
        loader.apiService = ApiService.create()
        return try {
            wavesWallet = WavesWallet(seed.toByteArray(Charsets.UTF_8))
            val guid = UUID.randomUUID().toString()
            /*prefs.setValue(PrefsUtil.KEY_PUB_KEY, wavesWallet!!.publicKeyStr)
            prefs.setValue(PrefsUtil.KEY_ENCRYPTED_WALLET, wavesWallet!!.getEncryptedData(password))*/
            guid
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "storeWalletData: ", e)
            ""
        }
    }

    fun getWallet(): WavesWallet {
        return wavesWallet!!
    }

    companion object {

        private var instance: Wavesplatform? = null

        @JvmStatic
        fun init(app: Application) {
            instance = Wavesplatform(app)
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