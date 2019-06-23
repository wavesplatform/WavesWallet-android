package com.wavesplatform.sdk.model.request.node

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.crypto.Base58

/**
 * The data transaction stores data in account data storage of the blockchain.
 * The storage contains data recorded using a data transaction or an invoke script transaction.
 * The maximum length of the data array is 100 elements.
 * The maximum size of the data array is 140 kilobytes.
 * Each element of the data array is an object that has 3 fields: key, type, value.
 * The array of data cannot contain two elements with the same key field.
 */
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
        @SerializedName("data") var data: ByteArray)
    : BaseTransaction(DATA) {

    override fun toBytes(): ByteArray {
        if (data.size < 1024 * 140) {
            return try {
                Bytes.concat(byteArrayOf(type.toByte()),
                        byteArrayOf(version.toByte()),
                        Base58.decode(senderPublicKey),
                        data,
                        Longs.toByteArray(timestamp),
                        Longs.toByteArray(fee))
            } catch (e: Exception) {
                Log.e("Sign", "Can't create bytes for sign in Data Transaction", e)
                ByteArray(0)
            }
        } else {
            Log.e("Sign", "Can't create bytes for sign in Data Transaction, data > 140kb")
            return ByteArray(0)
        }
    }
}