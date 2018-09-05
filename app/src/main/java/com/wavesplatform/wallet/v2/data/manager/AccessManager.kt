package com.wavesplatform.wallet.v2.data.manager

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
import javax.inject.Inject

open class AccessManager @Inject constructor() {

    @Inject
    lateinit var prefs: PrefsUtil
    @Inject
    lateinit var appUtil: AppUtil
    private val pinStore = PinStoreService()

    private object Holder {
        val INSTANCE = AccessManager()
    }


    fun validatePassCodeObservable(guid: String, passCode: String): Observable<String> {
        return createValidateObservable(guid, passCode)
                .flatMap { password ->
                    createPassCodeObservable(guid, password, passCode)
                            .andThen(Observable.just<String>(password))
                }
                .compose(RxUtil.applySchedulersToObservable<String>())
    }

    private fun createValidateObservable(guid: String, passedPin: String): Observable<String> {
        val fails = AccessState.getInstance().pinFails

        return pinStore.readPassword(fails, guid, passedPin)
                .map { value ->
                    try {
                        val encryptedPassword = prefs.getValue(guid, PrefsUtil.KEY_ENCRYPTED_PASSWORD, "")
                        AESUtil
                                .decrypt(encryptedPassword, value, AESUtil.PIN_PBKDF2_ITERATIONS)
                    } catch (e: Exception) {
                        throw Exceptions.propagate(Throwable("Decrypt wallet failed"))
                    }
                }
    }

    fun createPassCodeObservable(guid: String, password: String, passedPassCode: String): Completable {
        if (passedPassCode == "0000" || passedPassCode.length != 4) {
            return Completable.error(RuntimeException("Prohibited pin"))
        }

        appUtil.applyPRNGFixes()

        return Completable.create { subscriber ->
            try {
                val randomString = randomString()
                pinStore.savePasswordByKey(guid, randomString, passedPassCode)
                        .subscribe({ res ->
                            val encryptedPassword = AESUtil
                                    .encrypt(password, randomString, AESUtil.PIN_PBKDF2_ITERATIONS)
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
        val value = String(Hex.encode(bytes), charset("UTF-8"))
        return value
    }

    companion object {
        val instance: AccessManager by lazy { Holder.INSTANCE }
    }
}