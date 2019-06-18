/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model.request.node

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.crypto.WavesCrypto
import com.wavesplatform.sdk.utils.SignUtil.arrayWithSize

class IssueTransaction(
        @SerializedName("name") val name: String,
        @SerializedName("description") val description: String = "",
        @SerializedName("quantity") val quantity: Long,
        @SerializedName("decimals") val decimals: Byte,
        @SerializedName("reissuable") val reissuable: Boolean,
        @SerializedName("script") val script: String? = null) : BaseTransaction(ISSUE) {

    @SerializedName("id") val id: String

    init {
        this.fee = WAVES_ISSUE_MIN_FEE
        this.id = WavesCrypto.base58encode(WavesCrypto.blake2b(toBytes()))
    }

    override fun toBytes(): ByteArray {
        try {
            val reissuableBytes = if (reissuable) {
                byteArrayOf(1)
            } else {
                byteArrayOf(0)
            }

            return Bytes.concat(byteArrayOf(type.toByte()),
                    Base58.decode(senderPublicKey),
                    arrayWithSize(name),
                    arrayWithSize(description),
                    Longs.toByteArray(quantity),
                    byteArrayOf(decimals),
                    reissuableBytes,
                    Longs.toByteArray(fee),
                    Longs.toByteArray(timestamp))
        } catch (e: Exception) {
            Log.e("Sign", "Can't create bytes for sign in Issue Transaction", e)
            return ByteArray(0)
        }
    }

    companion object {
        var WAVES_ISSUE_MIN_FEE = 100000000L
        var MaxDescriptionLength = 1000
        var MaxAssetNameLength = 16
        var MinAssetNameLength = 4
        var MaxDecimals = 8
    }
}
