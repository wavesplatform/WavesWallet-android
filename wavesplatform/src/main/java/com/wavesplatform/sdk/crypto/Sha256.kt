package com.wavesplatform.sdk.crypto

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object Sha256 {

    fun hash(input: ByteArray): ByteArray {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            throw IllegalStateException("NoSuchAlgorithmException", e)
        }
    }
}
