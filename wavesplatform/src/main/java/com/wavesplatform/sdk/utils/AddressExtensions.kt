package com.wavesplatform.sdk.utils

import com.google.common.primitives.Bytes
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.crypto.Hash
import java.util.*

const val ADDRESS_VERSION: Byte = 1
const val CHECK_SUM_LENGTH = 4
const val HASH_LENGTH = 20
const val ADDRESS_LENGTH = 1 + 1 + CHECK_SUM_LENGTH + HASH_LENGTH
const val WAVES_PREFIX = "waves://"

fun String?.isValidAddress(): Boolean {
    if (this.isNullOrEmpty()) return false
    return try {
        val bytes = Base58.decode(this)
        if (bytes.size == ADDRESS_LENGTH
                && bytes[0] == ADDRESS_VERSION
                && bytes[1] == EnvironmentManager.netCode) {
            val checkSum = Arrays.copyOfRange(bytes, bytes.size - CHECK_SUM_LENGTH, bytes.size)
            val checkSumGenerated = calcCheckSum(Arrays.copyOf(bytes, bytes.size - CHECK_SUM_LENGTH))
            Arrays.equals(checkSum, checkSumGenerated)
        } else {
            false
        }
    } catch (e: Exception) {
        false
    }
}

fun String.makeAsAlias(): String {
    return "alias:${EnvironmentManager.netCode.toChar()}:$this"
}

fun String.clearAlias(): String {
    return this.substringAfterLast(":")
}

fun calcCheckSum(bytes: ByteArray): ByteArray {
    return Arrays.copyOfRange(Hash.secureHash(bytes), 0, CHECK_SUM_LENGTH)
}

fun addressFromPublicKey(publicKey: ByteArray): String {
    return try {
        val publicKeyHash = Hash.secureHash(publicKey).copyOf(HASH_LENGTH)
        val withoutChecksum = Bytes.concat(byteArrayOf(ADDRESS_VERSION, EnvironmentManager.netCode),
                publicKeyHash)
        Base58.encode(Bytes.concat(withoutChecksum, calcCheckSum(withoutChecksum)))
    } catch (e: Exception) {
        "Unknown address"
    }
}

fun addressFromPublicKey(publicKey: String): String {
    return addressFromPublicKey(Base58.decode(publicKey))
}