package com.wavesplatform.sdk.crypto

import com.wavesplatform.sdk.crypto.hash.Blake2b
import com.wavesplatform.sdk.crypto.hash.DigestEngine
import com.wavesplatform.sdk.crypto.hash.Keccak256

object Hash {

    private const val DIGEST_SIZE = 32

    private val blake = Blake2b.Digest.newInstance(DIGEST_SIZE)
    private val keccak256 = Keccak256()

    @JvmStatic
    fun secureHash(input: ByteArray): ByteArray {
        return keccak256.digest(blake.digest(input))
    }

    @JvmStatic
    fun fastHash(input: ByteArray): ByteArray {
        return blake.digest(input)
    }
}
