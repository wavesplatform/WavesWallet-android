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

class BurnTransaction(
        @SerializedName("assetId") val assetId: String,
        @SerializedName("quantity") var quantity: Long) : BaseTransaction(BURN) {

    @SerializedName("chainId")
    val chainId: Byte = WavesPlatform.getEnvironment().scheme
    @SerializedName("id")
    var id: String? = null

    override fun toBytes(): ByteArray {
        return try {
            Bytes.concat(
                    byteArrayOf(type.toByte()),
                    byteArrayOf(version.toByte()),
                    byteArrayOf(chainId),
                    Base58.decode(senderPublicKey),
                    Base58.decode(assetId),
                    Longs.toByteArray(quantity),
                    Longs.toByteArray(fee),
                    Longs.toByteArray(timestamp))
        } catch (e: Exception) {
            Log.e("Sign", "Can't create bytes for sign in Burn Transaction", e)
            ByteArray(0)
        }
    }
}