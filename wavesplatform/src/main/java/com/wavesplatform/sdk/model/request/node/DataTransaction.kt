package com.wavesplatform.sdk.model.request.node

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.common.primitives.Shorts
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.crypto.WavesCrypto
import com.wavesplatform.sdk.utils.arrayWithIntSize
import com.wavesplatform.sdk.utils.arrayWithSize
import kotlinx.android.parcel.Parcelize
import java.nio.charset.Charset

/**
 * The Data transaction stores data in account data storage of the blockchain.
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
                val keyArray = oneData.key!!
                    .toByteArray(Charset.forName("UTF-8"))
                    .arrayWithSize()
                val valueArray = when (oneData.type) {
                    "string" -> {
                        stringValue(STRING_DATA_TYPE, oneData.value as String)
                    }
                    "integer" -> {
                        val longValue: Long = if (oneData.value is Int) {
                            (oneData.value as Int).toLong()
                        } else {
                            oneData.value as Long
                        }
                        integerValue(INTEGER_DATA_TYPE, longValue)
                    }
                    "boolean" -> {
                        booleanValue(BOOLEAN_DATA_TYPE, oneData.value as Boolean)
                    }
                    "binary" -> {
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
     * Data of Data transaction.
     */
    class Data(
        /**
         * Key of data of Data transaction
         */
        @SerializedName("key")
        var key: String?,

        /**
         * Type of data of Data transaction type can be only "string", "boolean", "integer", "binary"
         */
        @SerializedName("type")
        var type: String?,

        /**
         * Data transaction value can be one of four types:
         * [Long] for integer(0),
         * [Boolean] for boolean(1),
         * [String] for binary(2)
         * and [String] string(3). You can use "base64:binaryString" and just "binaryString". Can't be empty string
         */
        @SerializedName("value")
        var value: Any?) : Parcelable {

        private constructor(parcel: Parcel) : this(
            key = parcel.readString(),
            type = parcel.readString(),
            value = parcel.readValue(Any::class.java.classLoader)
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(key)
            parcel.writeString(type)
            parcel.writeValue(value)
        }

        override fun describeContents() = 0

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<Data> {
                override fun createFromParcel(parcel: Parcel) = Data(parcel)
                override fun newArray(size: Int) = arrayOfNulls<Data>(size)
            }
        }
    }
}