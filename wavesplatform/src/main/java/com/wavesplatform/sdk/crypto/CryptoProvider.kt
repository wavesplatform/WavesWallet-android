package com.wavesplatform.sdk.crypto

import org.whispersystems.curve25519.OpportunisticCurve25519Provider

import java.lang.reflect.Constructor

object CryptoProvider {

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
}
