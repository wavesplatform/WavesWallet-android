package com.wavesplatform.wallet.v2.data.helpers

import com.wavesplatform.wallet.v1.crypto.PublicKeyAccount
import javax.inject.Inject

class PublicKeyAccountHelper @Inject constructor() {
    var publicKeyAccount: PublicKeyAccount? = null

    fun isPublicKeyAccountAvailable(publicKey: String): Boolean {
        try {
            publicKeyAccount = PublicKeyAccount(publicKey)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return publicKeyAccount != null
    }
}
