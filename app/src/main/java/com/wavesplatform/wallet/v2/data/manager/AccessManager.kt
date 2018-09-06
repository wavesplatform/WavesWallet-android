package com.wavesplatform.wallet.v2.data.manager

import android.text.TextUtils
import android.util.Log
import com.wavesplatform.wallet.BlockchainApplication
import com.wavesplatform.wallet.v1.crypto.AESUtil
import com.wavesplatform.wallet.v1.data.auth.WavesWallet
import com.wavesplatform.wallet.v1.data.rxjava.RxUtil
import com.wavesplatform.wallet.v1.data.services.PinStoreService
import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager
import com.wavesplatform.wallet.v1.util.AddressUtil
import com.wavesplatform.wallet.v1.util.AppUtil
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser
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
        return readPassCodeObservable(
                guid, passCode, BlockchainApplication.getAccessManager().getPassCodeInputFails())
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

    fun storeWalletData(seed: String, password: String, walletName: String, skipBackup: Boolean): String {
        try {
            val wallet = WavesWallet(seed.toByteArray(Charsets.UTF_8))
            val guid = UUID.randomUUID().toString()
            prefs.setGlobalValue(PrefsUtil.GLOBAL_LOGGED_IN_GUID, guid)
            prefs.addGlobalListValue(PrefsUtil.LIST_WALLET_GUIDS, guid)
            prefs.setValue(PrefsUtil.KEY_PUB_KEY, wallet.publicKeyStr)
            prefs.setValue(PrefsUtil.KEY_WALLET_NAME, walletName)
            prefs.setValue(PrefsUtil.KEY_ENCRYPTED_WALLET, wallet.getEncryptedData(password))
            if (skipBackup) {
                prefs.setValue(PrefsUtil.KEY_SKIP_BACKUP, true)
            }
            return guid
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "storeWalletData: ", e)
            return ""
        }
    }

    fun storeWalletName(address: String, name: String) {
        if (!TextUtils.isEmpty(address) && !TextUtils.isEmpty(name)) {
            val searchWalletGuid = findGuidBy(address)
            prefs.setGlobalValue(searchWalletGuid + PrefsUtil.KEY_WALLET_NAME, name)
        }
    }

    fun findGuidBy(address: String): String {
        val guids = prefs.getGlobalValueList(PrefsUtil.LIST_WALLET_GUIDS)
        var resultGuid = ""
        for (guid in guids) {
            val publicKey = prefs.getValue(guid, PrefsUtil.KEY_PUB_KEY, "")
            if (AddressUtil.addressFromPublicKey(publicKey) == address) {
                resultGuid = guid
            }
        }
        return resultGuid
    }

    fun getCurrentWavesWalletEncryptedData(): String {
        return getWalletData(getCurrentGuid())
    }

    fun getCurrentGuid(): String {
        return prefs.guid
    }

    fun createAddressBookCurrentAccount(): AddressBookUser? {
        val guid = getCurrentGuid()
        if (TextUtils.isEmpty(guid)) {
            return null
        }

        val name = prefs.getGlobalValue(guid + PrefsUtil.KEY_WALLET_NAME, "")
        val publicKey = prefs.getGlobalValue(guid + PrefsUtil.KEY_PUB_KEY, "")

        return if (TextUtils.isEmpty(publicKey) || TextUtils.isEmpty(name)) {
            null
        } else AddressBookUser(AddressUtil.addressFromPublicKey(publicKey), name)

    }

    fun deleteCurrentWavesWallet(): Boolean {
        val currentUser = createAddressBookCurrentAccount()
        if (currentUser == null) {
            return false
        } else {
            deleteWavesWallet(currentUser.address)
            return true
        }
    }


    fun deleteWavesWallet(address: String) {
        val searchWalletGuid = BlockchainApplication.getAccessManager().findGuidBy(address)

        prefs.removeValue(searchWalletGuid, PrefsUtil.KEY_PUB_KEY)
        prefs.removeValue(searchWalletGuid, PrefsUtil.KEY_WALLET_NAME)
        prefs.removeValue(searchWalletGuid, PrefsUtil.KEY_ENCRYPTED_WALLET)

        prefs.setGlobalValue(PrefsUtil.LIST_WALLET_GUIDS, createGuidsListWithout(searchWalletGuid))

        if (searchWalletGuid == prefs.getGlobalValue(PrefsUtil.GLOBAL_LOGGED_IN_GUID, "")) {
            prefs.removeGlobalValue(PrefsUtil.GLOBAL_LOGGED_IN_GUID)
        }
    }

    private fun createGuidsListWithout(guidToRemove: String): Array<String> {
        val guids = prefs.getGlobalValueList(PrefsUtil.LIST_WALLET_GUIDS)
        val resultGuidsList = ArrayList<String>()
        for (guid in guids) {
            if (guid != guidToRemove) {
                resultGuidsList.add(guid)
            }
        }
        return resultGuidsList.toTypedArray()
    }


    fun getWalletData(guid: String): String {
        return if (TextUtils.isEmpty(guid)) {
            ""
        } else prefs.getGlobalValue(guid + PrefsUtil.KEY_ENCRYPTED_WALLET, "")
    }

    fun getWalletName(guid: String): String {
        return if (TextUtils.isEmpty(guid)) {
            ""
        } else prefs.getGlobalValue(guid + PrefsUtil.KEY_WALLET_NAME, "")
    }

    fun getWalletAddress(guid: String): String {
        if (TextUtils.isEmpty(guid)) {
            return ""
        }
        val publicKey = prefs.getValue(guid, PrefsUtil.KEY_PUB_KEY, "")
        return AddressUtil.addressFromPublicKey(publicKey)
    }

    fun findPublicKeyBy(address: String): String {
        if (TextUtils.isEmpty(address)) {
            return ""
        }
        return prefs.getValue(BlockchainApplication.getAccessManager().findGuidBy(address),
                PrefsUtil.KEY_PUB_KEY, "")
    }

    fun storePassword(guid: String, publicKeyStr: String, encryptedPassword: String) {
        prefs.setGlobalValue(PrefsUtil.GLOBAL_LOGGED_IN_GUID, guid)
        prefs.setValue(PrefsUtil.KEY_PUB_KEY, publicKeyStr)
        prefs.setValue(PrefsUtil.KEY_ENCRYPTED_WALLET, encryptedPassword)
    }

    fun setCurrentAccount(guid: String) {
        prefs.setValue(PrefsUtil.GLOBAL_LOGGED_IN_GUID, guid)
    }

    fun incrementPassCodeInputFails() {
        var fails = prefs.getValue(PrefsUtil.KEY_PIN_FAILS, 0)
        prefs.setValue(PrefsUtil.KEY_PIN_FAILS, ++fails)
    }

    fun getPassCodeInputFails(): Int {
        return prefs.getValue(PrefsUtil.KEY_PIN_FAILS, 0)
    }

    fun resetPassCodeInputFails() {
        prefs.removeValue(PrefsUtil.KEY_PIN_FAILS)
    }

    fun setCurrentAccountBackupSkipped() {
        prefs.setValue(PrefsUtil.KEY_SKIP_BACKUP, false)
    }

    fun isCurrentAccountBackupSkipped(): Boolean {
        return prefs.getValue(PrefsUtil.KEY_SKIP_BACKUP, true)
    }

    fun setUseFingerPrint(use: Boolean) {
        prefs.setValue(PrefsUtil.KEY_USE_FINGERPRINT, use)
    }

    fun isGuidUseFingerPrint(guid: String): Boolean {
        return prefs.getGuidValue(guid, PrefsUtil.KEY_USE_FINGERPRINT, false)
    }

    fun isUseFingerPrint(): Boolean {
        return prefs.getValue(PrefsUtil.KEY_USE_FINGERPRINT, false)
    }

    fun setEncryptedPassCode(guid: String, data: String) {
        prefs.setValue(guid, PrefsUtil.KEY_ENCRYPTED_PIN, data)
    }

    fun getEncryptedPassCode(guid: String): String {
        return prefs.getValue(guid, PrefsUtil.KEY_ENCRYPTED_PIN, "")
    }
}