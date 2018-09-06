package com.wavesplatform.wallet.v2.data.manager

import android.content.Context
import android.util.Log
import com.wavesplatform.wallet.v1.crypto.AESUtil
import com.wavesplatform.wallet.v1.data.access.AccessState
import com.wavesplatform.wallet.v1.data.rxjava.RxUtil
import com.wavesplatform.wallet.v1.data.services.PinStoreService
import com.wavesplatform.wallet.v1.util.AppUtil
import com.wavesplatform.wallet.v1.util.PrefsUtil
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.exceptions.Exceptions
import org.spongycastle.util.encoders.Hex
import java.security.SecureRandom

class AccessManager(context: Context) {

    private var prefs: PrefsUtil = PrefsUtil(context)
    private var appUtil: AppUtil = AppUtil(context)
    private val pinStore = PinStoreService()

    fun validatePassCodeObservable(guid: String, passCode: String): Observable<String> {
        val fails = AccessState.getInstance().pinFails
        return readPassCodeObservable(guid, passCode, fails)
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
}