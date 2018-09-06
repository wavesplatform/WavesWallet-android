package com.wavesplatform.wallet.v2.data.manager

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import com.wavesplatform.wallet.v1.crypto.AESUtil
import com.wavesplatform.wallet.v1.data.access.AccessState
import com.wavesplatform.wallet.v1.data.auth.WavesWallet
import com.wavesplatform.wallet.v1.data.rxjava.RxUtil
import com.wavesplatform.wallet.v1.data.services.PinStoreService
import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager
import com.wavesplatform.wallet.v1.util.AppUtil
import com.wavesplatform.wallet.v1.util.PrefsUtil
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.exceptions.Exceptions
import org.apache.commons.io.Charsets
import org.spongycastle.util.encoders.Hex
import java.security.SecureRandom
import java.util.*

class AccessManager(private var prefs: PrefsUtil, private var appUtil: AppUtil) {

    private val pinStore = PinStoreService()

    fun validatePassCodeObservable(guid: String, passCode: String): Observable<String> {
        return readPassCodeObservable(guid, passCode, AccessState.getInstance().passCodeInputFails)
                .flatMap { password ->
                    writePassCodeObservable(guid, password, passCode)
                            .andThen(Observable.just<String>(password))
                }
                .compose(RxUtil.applySchedulersToObservable<String>())
    }

    private fun readPassCodeObservable(guid: String, passedPin: String, tryCount: Int): Observable<String> {
        return pinStore.readPassword(guid, passedPin, tryCount)
                .map { seed ->
                    try {
                        val encryptedPassword = prefs.getValue(
                                guid, PrefsUtil.KEY_ENCRYPTED_PASSWORD, "")
                        AESUtil.decrypt(
                                encryptedPassword, seed, AESUtil.PIN_PBKDF2_ITERATIONS)
                    } catch (e: Exception) {
                        throw Exceptions.propagate(Throwable("Decrypt wallet failed"))
                    }
                }
    }

    fun writePassCodeObservable(guid: String, password: String, passCode: String): Completable {
        if (passCode == "0000" || passCode.length != 4) {
            return Completable.error(RuntimeException("Prohibited pin"))
        }

        appUtil.applyPRNGFixes()

        return Completable.create { subscriber ->
            try {
                val seed = randomString()
                pinStore.writePassword(guid, passCode, seed)
                        .subscribe({ res ->
                            val encryptedPassword = AESUtil.encrypt(
                                    password, seed, AESUtil.PIN_PBKDF2_ITERATIONS)
                            prefs.setValue(guid, PrefsUtil.KEY_ENCRYPTED_PASSWORD, encryptedPassword)
                            if (!subscriber.isDisposed) {
                                subscriber.onComplete()
                            }
                        }, { err ->
                            if (!subscriber.isDisposed) {
                                subscriber.onError(err)
                            }
                        })
            } catch (e: Exception) {
                Log.e(javaClass.simpleName, "createPinObservable", e)
                if (!subscriber.isDisposed) {
                    subscriber.onError(RuntimeException("Failed to encrypt password"))
                }
            }
        }.compose(RxUtil.applySchedulersToCompletable())
    }

    private fun randomString(): String {
        val bytes = ByteArray(16)
        val random = SecureRandom()
        random.nextBytes(bytes)
        return String(Hex.encode(bytes), charset("UTF-8"))
    }

    fun storeWavesWallet(seed: String, password: String, walletName: String, skipBackup: Boolean): String {
        try {
            val wallet = WavesWallet(seed.toByteArray(Charsets.UTF_8))
            val guid = UUID.randomUUID().toString()
            prefs.setGlobalValue(PrefsUtil.GLOBAL_LOGGED_IN_GUID, guid)
            prefs.addGlobalListValue(EnvironmentManager.get().current().getName()
                    + PrefsUtil.LIST_WALLET_GUIDS, guid)
            prefs.setValue(PrefsUtil.KEY_PUB_KEY, wallet.publicKeyStr)
            prefs.setValue(PrefsUtil.KEY_WALLET_NAME, walletName)
            prefs.setValue(PrefsUtil.KEY_ENCRYPTED_WALLET, wallet.getEncryptedData(password))
            if (skipBackup) {
                prefs.setValue(PrefsUtil.KEY_SKIP_BACKUP, true)
            }
            return guid
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "storeWavesWallet: ", e)
            return ""
        }
    }
}