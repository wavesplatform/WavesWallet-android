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

/**
 * Reissue transaction is used to give the ability to reissue more tokens of an asset
 * by specifying the amount and the asset id. Only quantity and reissuable can be new values
 */
class ReissueTransaction(
        /**
         * Id of asset that should be changed
         */
        @SerializedName("assetId") val assetId: String,
        /**
         * Quantity defines the total tokens supply that your asset will contain
         */
        @SerializedName("quantity") val quantity: Long,
        /**
         * Reissuability allows for additional tokens creation that will be added
         * to the total token supply of asset.
         * A non-reissuable asset will be permanently limited to the total token supply
         * defined during the transaction.
         */
        @SerializedName("reissuable") val reissuable: Boolean) : BaseTransaction(REISSUE) {

    init {
        this.fee = WAVES_REISSUE_MIN_FEE
    }

    override fun toBytes(): ByteArray {
        try {
            val reissuableBytes = if (reissuable) {
                byteArrayOf(1)
            } else {
                byteArrayOf(0)
            }

            return Bytes.concat(
                    byteArrayOf(type.toByte()),
                    byteArrayOf(version.toByte()),
                    byteArrayOf(chainId),
                    Base58.decode(senderPublicKey),
                    Base58.decode(assetId),
                    Longs.toByteArray(quantity),
                    reissuableBytes,
                    Longs.toByteArray(fee),
                    Longs.toByteArray(timestamp))
        } catch (e: Exception) {
            Log.e("Sign", "Can't create bytes for sign in Reissue Transaction", e)
            return ByteArray(0)
        }
    }

    companion object {
        const val WAVES_REISSUE_MIN_FEE = 100000000L
    }
}
