package com.wavesplatform.sdk.model.request.node

import android.os.Parcelable
import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.common.primitives.Shorts
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.utils.arrayWithSize
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import java.nio.charset.Charset

/**
 * The data transaction stores data in account data storage of the blockchain.
 * The storage contains data recorded using a data transaction or an invoke script transaction.
 * The maximum length of the data array is 100 elements.
 * The maximum size of the data array is 140 kilobytes.
 * Each element of the data array is an object that has 3 fields: key, type, value.
 * The array of data cannot contain two elements with the same key field.
 *
 * Fee depends of data transaction length
 */
@Parcelize
class DataTransaction(
        /**
         * Data as JSON-string as byte array
         * The value of the key field is a UTF-8 encoded string
         * of length from 1 to 100 characters inclusive.
         * It can be of four types - integer(0), boolean(1), binary array(2) and string(3).
         * The size of value field can be from 0 to 32767 bytes.
         * Example:
         * "data": [
         *      {"key": "int", "type": "integer", "value": 24},
         *      {"key": "bool", "type": "boolean", "value": true},
         *      {"key": "blob", "type": "binary", "value": "base64:BzWHaQU"}
         *      {"key": "My poem", "type": "string", "value": "Oh waves!"}
         * ],
         */
        @SerializedName("data") var data: List<DataEntity> = mutableListOf())
    : BaseTransaction(DATA), Parcelable {

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
        Shorts.toByteArray(data.size.toShort())
        val lengthBytes = Shorts.toByteArray(data.size.toShort())
        val dataArray = if (data.isNotEmpty()) {
            var tempDataArray = byteArrayOf()
            for (oneData in data) {
                val keyArray: ByteArray
                val valueArray = when (oneData) {
                    is DataEntity.Text -> {
                        keyArray = oneData.key!!.toByteArray(Charset.forName("UTF-8")).arrayWithSize()
                        typedValue(STRING_DATA_TYPE, oneData.value!!)
                    }
                    is DataEntity.Digit -> {
                        keyArray = oneData.key!!.toByteArray(Charset.forName("UTF-8")).arrayWithSize()
                        typedValue(INTEGER_DATA_TYPE, oneData.value!!)
                    }
                    is DataEntity.Bool -> {
                        keyArray = oneData.key!!.toByteArray(Charset.forName("UTF-8")).arrayWithSize()
                        typedValue(BOOLEAN_DATA_TYPE, oneData.value!!)
                    }
                    is DataEntity.Binary -> {
                        keyArray = oneData.key!!.toByteArray(Charset.forName("UTF-8")).arrayWithSize()
                        typedValue(BINARY_DATA_TYPE, oneData.value!!)
                    }
                    else -> {
                        throw Error("There is no the data type")
                    }
                }
                val keyValueArray = Bytes.concat(keyArray, valueArray)
                tempDataArray = Bytes.concat(tempDataArray, keyValueArray)
            }
            Bytes.concat(lengthBytes, tempDataArray)
        } else {
            byteArrayOf(0, 0)
        }

        if (dataArray.size > DATA_ENTRIES_BYTE_LIMIT) {
            throw Error("Data transaction is too large > 140KB(max)")
        }

        return dataArray
    }

    private fun typedValue(type: Byte, bytes: ByteArray): ByteArray {
        return Bytes.concat(byteArrayOf(type), bytes.arrayWithSize())
    }

    private fun typedValue(type: Byte, int64Value: Long): ByteArray {
        return Bytes.concat(byteArrayOf(type), Longs.toByteArray(int64Value))
    }

    private fun typedValue(type: Byte, stringValue: String): ByteArray {
        return Bytes.concat(byteArrayOf(type),
                stringValue.toByteArray(Charset.forName("UTF-8"))
                        .arrayWithSize())
    }

    private fun typedValue(type: Byte, booleanValue: Boolean): ByteArray {
        val bytes = if (booleanValue) {
            byteArrayOf(1)
        } else {
            byteArrayOf(0)
        }
        return Bytes.concat(byteArrayOf(type), bytes)
    }

    override fun sign(seed: String): String {
        version = 1
        signature = super.sign(seed)
        return signature ?: ""
    }

    companion object {
        const val INTEGER_DATA_TYPE: Byte = 0
        const val BOOLEAN_DATA_TYPE: Byte = 1
        const val BINARY_DATA_TYPE: Byte = 2
        const val STRING_DATA_TYPE: Byte = 3

        private const val DATA_TX_SIZE_WITHOUT_ENTRIES = 52
        const val DATA_ENTRIES_BYTE_LIMIT = 140 * 1024 - DATA_TX_SIZE_WITHOUT_ENTRIES
    }
}