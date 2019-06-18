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

class ScriptInvocationTransaction(@SerializedName("assetId") var assetId: String,
                                  @SerializedName("script") var script: String) : BaseTransaction(SCRIPT_INVOCATION) {

    override fun toBytes(): ByteArray {
        return try {
            Bytes.concat(
                    byteArrayOf(type.toByte()),
                    byteArrayOf(version.toByte()),
                    byteArrayOf(WavesPlatform.getEnvironment().scheme),
                    Base58.decode(senderPublicKey),
                    // todo check add ['dApp', txFields.recipient[1]],
                    // todo add txFields.functionCall,
                    // todo add txFields.payments,
                    Longs.toByteArray(fee),
                    // todo add ['feeAssetId', txFields.optionalAssetId[1]],
                    Longs.toByteArray(timestamp))
            // todo look https://github.com/wavesplatform/marshall/blob/master/src/schemas.ts : 320
        } catch (e: Exception) {
            Log.e("Sign", "Can't create bytes for sign in Script Invocation Transaction", e)
            ByteArray(0)
        }
    }
}