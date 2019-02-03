package com.wavesplatform.sdk.crypto

import com.wavesplatform.sdk.crypto.hash.Blake2b
import com.wavesplatform.sdk.crypto.hash.DigestEngine
import com.wavesplatform.sdk.crypto.hash.Keccak256

object Hash {
    var DigestSize = 32

    private val blake = Blake2b.Digest.newInstance(DigestSize)
    private val keccak256 = Keccak256()

    @JvmStatic
    fun hashChain(input: ByteArray, vararg engines: DigestEngine): ByteArray {
        var input = input
        for (engine in engines) {
            input = engine.digest(input)
        }
        return input
    }

    @JvmStatic
    fun secureHash(input: ByteArray): ByteArray {
        return keccak256.digest(blake.digest(input))
    }

    @JvmStatic
    fun fastHash(input: ByteArray): ByteArray {
        return blake.digest(input)
    }

}
