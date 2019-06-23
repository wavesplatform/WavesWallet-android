/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model.request.node

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.WavesPlatform
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.model.response.node.transaction.ScriptInvocationTransactionResponse

/**
 * Script invocation transaction - execution of script functions (test net only)
 */
class ScriptInvocationTransaction(
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
        @SerializedName("call") var call: ScriptInvocationTransactionResponse.Call?,
        /**
         * (while 1 payment is supported)
         */
        @SerializedName("payment") var payment: Array<ScriptInvocationTransactionResponse.Payment>)
    : BaseTransaction(SCRIPT_INVOCATION) {

    override fun toBytes(): ByteArray {
        return try {
            Bytes.concat(
                    byteArrayOf(type.toByte()),
                    byteArrayOf(version.toByte()),
                    byteArrayOf(WavesPlatform.getEnvironment().scheme),
                    Base58.decode(senderPublicKey),
                    Base58.decode(dApp),
                    // todo add txFields.functionCall,
                    // todo add txFields.payments,


                    /*alias.toByteArray(
                            Charset.forName("UTF-8")).arrayWithSize()*/


                    Longs.toByteArray(fee),
                    Base58.decode(payment[1].assetId ?: ""), // now it works with only one
                    Longs.toByteArray(timestamp))
            // todo look https://github.com/wavesplatform/marshall/blob/master/src/schemas.ts : 320
        } catch (e: Exception) {
            Log.e("Sign", "Can't create bytes for sign in Script Invocation Transaction", e)
            ByteArray(0)
        }
    }
}