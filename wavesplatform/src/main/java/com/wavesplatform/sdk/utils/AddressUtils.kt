package com.wavesplatform.sdk.utils

import com.google.common.primitives.Bytes
import com.wavesplatform.sdk.Constants
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.crypto.Hash
import java.util.*

class AddressUtil {

    companion object {

        var addressVersion: Byte = 1
        var checksumLength = 4
        var hashLength = 20
        const val WAVES_PREFIX = "waves://"



        fun calcCheckSum(bytes: ByteArray): ByteArray {
            return Arrays.copyOfRange(Hash.secureHash(bytes), 0, checksumLength)
        }


        fun addressFromPublicKey(publicKey: ByteArray): String {
            val publicKeyHash = Hash.secureHash(publicKey).copyOf(hashLength)
            val withoutChecksum = Bytes.concat(byteArrayOf(addressVersion, Constants.NET_CODE), publicKeyHash)
            return Base58.encode(Bytes.concat(withoutChecksum, calcCheckSum(withoutChecksum)))
        }

        fun addressFromPublicKey(publicKey: String): String {
            return try {
                val bytes = Base58.decode(publicKey)
                val publicKeyHash = Arrays.copyOf(Hash.secureHash(bytes), hashLength)
                val withoutChecksum = Bytes.concat(byteArrayOf(addressVersion, Constants.NET_CODE), publicKeyHash)
                Base58.encode(Bytes.concat(withoutChecksum, calcCheckSum(withoutChecksum)))
            } catch (e: Exception) {
                "Unknown address"
            }
        }
    }
}
