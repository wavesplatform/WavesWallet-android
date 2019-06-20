package com.wavesplatform.sdk.crypto

import org.whispersystems.curve25519.OpportunisticCurve25519Provider

import java.lang.reflect.Constructor

internal object CryptoProvider {

    private var SIGNATURE_LENGTH = 64

    private var provider: OpportunisticCurve25519Provider? = null
    fun get(): OpportunisticCurve25519Provider {
        if (provider == null) {
            val constructor: Constructor<OpportunisticCurve25519Provider>
            try {
                constructor = OpportunisticCurve25519Provider::class.java.getDeclaredConstructor()
                constructor.isAccessible = true
                provider = constructor.newInstance()
            } catch (e: Exception) {
                e.printStackTrace()
                throw RuntimeException("Couldn't create crypto provider", e)
            }
        }
        return provider!!
    }

    @JvmStatic
    fun sign(key: ByteArray, msg: ByteArray): ByteArray {
        return get().calculateSignature(CryptoProvider.get().getRandom(SIGNATURE_LENGTH), key, msg)
    }
}
