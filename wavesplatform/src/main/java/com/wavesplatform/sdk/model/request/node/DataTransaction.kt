package com.wavesplatform.sdk.model.request.node

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.common.primitives.Shorts
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.crypto.WavesCrypto
import com.wavesplatform.sdk.utils.arrayWithIntSize
import com.wavesplatform.sdk.utils.arrayWithSize
import java.nio.charset.Charset

/**
 * The data transaction stores data in account data storage of the blockchain.
 *
 * The storage contains data recorded using a data transaction or an invoke script transaction.
 * The maximum length of the data array is 100 elements.
 * The maximum size of the data array is 140 kilobytes.
 * Each element of the data array is an object that has 3 fields: key, type, value.
 * The array of data cannot contain two elements with the same key field.
 *
 * Fee depends of data transaction length (0.001 per 1kb)
 */
class DataTransaction(
        /**
         * Data as JSON-string as byte array
         * The value of the key field is a UTF-8 encoded string
         * of length from 1 to 100 characters inclusive.
         * It can be of four types - integer(0), boolean(1), binary array(2) and string(3).
         * The size of value field can be from 0 to 65025 bytes.
         * Example:
         * "data": [
         *      {"key": "int", "type": "integer", "value": 24},
         *      {"key": "bool", "type": "boolean", "value": true},
         *      {"key": "blob", "type": "binary", "value": "base64:BzWHaQU="}
         *      {"key": "My poem", "type": "string", "value": "Oh waves!"}
         * ],
         */
        @SerializedName("data") var data: List<Data> = mutableListOf())
    : BaseTransaction(DATA) {

    override fun sign(seed: String): String {
        version = 1
        signature = super.sign(seed)
        return signature ?: ""
    }

    override fun toBytes(): ByteArray {
        return try {
            Bytes.concat(byteArrayOf(type.toByte()),
                    byteArrayOf(version.toByte()),
                    Base58.decode(senderPublicKey),
                    dataBytes(),
                    Longs.toByteArray(timestamp),
                    Longs.toByteArray(fee))
        } catch (e: Exception) {
            Log.e("Sign", "Can't create bytes for sign in Data Transaction", e)
            ByteArray(0)
        }
    }

    private fun dataBytes(): ByteArray {
        val allDataArray = if (data.isNotEmpty()) {
            var keyValueChainArray = byteArrayOf()
            for (oneData in data) {
                val keyArray: ByteArray
                val valueArray = when (oneData.type) {
                    "string" -> {
                        keyArray = oneData.key!!
                                .toByteArray(Charset.forName("UTF-8"))
                                .arrayWithSize()
                        stringValue(STRING_DATA_TYPE, oneData.value as String)
                    }
                    "integer" -> {
                        keyArray = oneData.key!!
                                .toByteArray(Charset.forName("UTF-8"))
                                .arrayWithSize()
                        integerValue(INTEGER_DATA_TYPE, oneData.value as Long)
                    }
                    "boolean" -> {
                        keyArray = oneData.key!!
                                .toByteArray(Charset.forName("UTF-8"))
                                .arrayWithSize()
                        booleanValue(BOOLEAN_DATA_TYPE, oneData.value as Boolean)
                    }
                    "binary" -> {
                        keyArray = oneData.key!!
                                .toByteArray(Charset.forName("UTF-8"))
                                .arrayWithSize()
                        binaryValue(BINARY_DATA_TYPE, (oneData.value as String)
                                .replace("base64:", ""))
                    }
                    else -> {
                        throw Error("There is no the data type")
                    }
                }
                val keyValueArray = Bytes.concat(keyArray, valueArray)
                keyValueChainArray = Bytes.concat(keyValueChainArray, keyValueArray)
            }

            val lengthBytes = Shorts.toByteArray(data.size.toShort())
            Bytes.concat(lengthBytes, keyValueChainArray)
        } else {
            byteArrayOf(0, 0)
        }

        if (allDataArray.size > DATA_ENTRIES_BYTE_LIMIT) {
            throw Error("Data transaction is too large > 140KB(max)")
        }

        return allDataArray
    }

    companion object {
        const val INTEGER_DATA_TYPE: Byte = 0
        const val BOOLEAN_DATA_TYPE: Byte = 1
        const val BINARY_DATA_TYPE: Byte = 2
        const val STRING_DATA_TYPE: Byte = 3

        private const val DATA_TX_SIZE_WITHOUT_ENTRIES = 52
        const val DATA_ENTRIES_BYTE_LIMIT = 140 * 1024 - DATA_TX_SIZE_WITHOUT_ENTRIES

        fun integerValue(type: Byte, int64Value: Long): ByteArray {
            return Bytes.concat(byteArrayOf(type), Longs.toByteArray(int64Value))
        }

        fun stringValue(type: Byte, stringValue: String, useInt: Boolean = false): ByteArray {
            val array = stringValue.toByteArray(Charset.forName("UTF-8"))
            val withSize = if (useInt) {
                array.arrayWithIntSize()
            } else {
                array.arrayWithSize()
            }
            return Bytes.concat(byteArrayOf(type), withSize)
        }

        fun binaryValue(type: Byte, binaryValue: String, useInt: Boolean = false): ByteArray {
            val array = WavesCrypto.base64decode(binaryValue)
            val withSize = if (useInt) {
                array.arrayWithIntSize()
            } else {
                array.arrayWithSize()
            }
            return Bytes.concat(byteArrayOf(type), withSize)
        }

        fun booleanValue(type: Byte, booleanValue: Boolean): ByteArray {
            val bytes = if (booleanValue) {
                byteArrayOf(1)
            } else {
                byteArrayOf(0)
            }
            return Bytes.concat(byteArrayOf(type), bytes)
        }
    }

    /**
     * Data transaction a entity type.
     */
    class Data(key: String, type: String, value: Any) {

        /**
         * Data transaction key
         */
        @SerializedName("key")
        var key: String? = key

        /**
         * Data transaction type can be only "string", "boolean", "integer", "binary"
         */
        @SerializedName("type")
        var type: String? = type

        /**
         * Data transaction value can be string, boolean, Long, and binary string as Base64
         */
        @SerializedName("value")
        var value: Any? = value
    }
}