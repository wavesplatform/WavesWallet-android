/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model.request.node

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.WavesSdk
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.model.response.node.transaction.InvokeScriptTransactionResponse
import com.wavesplatform.sdk.utils.arrayWithSize
import java.nio.charset.Charset

/**
 * Not available now
 *
 * Invoke script transaction is a transaction that invokes functions of the dApp script.
 */
class InvokeScriptTransaction(
        /**
         * Asset id instead Waves for transaction commission withdrawal
         */
        @SerializedName("feeAssetId") var feeAssetId: String,
        /**
         * dApp – address of contract
         */
        @SerializedName("dApp") var dApp: String,
        /**
         * Function name in dApp with array of arguments
         */
        @SerializedName("call") var call: Call,
        /**
         * (while 1 payment is supported)
         */
        @SerializedName("payment") var payment: Array<Payment>)
    : BaseTransaction(SCRIPT_INVOCATION) {

    override fun toBytes(): ByteArray {
        return try {
            Bytes.concat(
                    byteArrayOf(type.toByte()),
                    byteArrayOf(version.toByte()),
                    byteArrayOf(WavesSdk.getEnvironment().scheme),
                    Base58.decode(senderPublicKey),
                    Base58.decode(dApp),
                    functionCallArray(),
                    paymentsArray(),
                    Longs.toByteArray(fee),
                    Base58.decode(payment[1].assetId), // now it works with only one
                    Longs.toByteArray(timestamp))
        } catch (e: Exception) {
            Log.e("Sign", "Can't create bytes for sign in Script Invocation Transaction", e)
            ByteArray(0)
        }
    }

    override fun sign(seed: String): String {
        version = 1
        signature = super.sign(seed)
        return signature ?: ""
    }

    private fun functionCallArray(): ByteArray {
        val array = byteArrayOf(9, 1)  // special bytes to indicate function call. Used in Serde serializer
        return Bytes.concat(array, functionArray(), argsArray())
    }

    private fun functionArray(): ByteArray {
        return call.function
                .toByteArray(Charset.forName("UTF-8"))
                .arrayWithSize()
    }

    private fun argsArray(): ByteArray {
        val array = byteArrayOf()
        for (arg in call.args) {
            when (arg.type) {
                "integer" ->
                    Bytes.concat(array, DataTransaction.integerValue(0, arg.value as Long))
                "binary" ->
                    Bytes.concat(
                            array,
                            DataTransaction.binaryValue(1, (arg.value as String)
                                    .replace("base64:", "")))
                "string" ->
                    Bytes.concat(array, DataTransaction.stringValue(2, arg.value as String))
                "boolean" -> {
                    val value = arg.value as Boolean
                    val byte: Byte = if (value) 6 else 7
                    Bytes.concat(array, DataTransaction.booleanValue(byte, value))
                }
            }
        }
        return array;
    }

    private fun paymentsArray(): ByteArray {
        var array = byteArrayOf()
        for (paymentItem in payment) {
            val amount = Longs.toByteArray(paymentItem.amount)
            val assetId = Base58.decode(paymentItem.assetId)
            array = Bytes.concat(array, Bytes.concat(amount, assetId))
        }
        return array.arrayWithSize()
    }

    class Payment(
            @SerializedName("feeAssetAmount")
            var amount: Long,
            @SerializedName("feeAssetId")
            var assetId: String)

    class Call(
            /**
             * Function name
             */
            @SerializedName("function") var function: String = "",
            /**
             * Array of arguments
             */
            @SerializedName("args") var args: Array<Args> = arrayOf())

    class Args(
            /**
             * Type can be of four types - integer(0), boolean(1), binary array(2) and string(3).
             */
            @SerializedName("type") var type: String = "",
            /**
             * Value can of four types - integer(0), boolean(1), binary array(2) and string(3).
             * And it depends on type.
             */
            @SerializedName("value") var value: Any)
}