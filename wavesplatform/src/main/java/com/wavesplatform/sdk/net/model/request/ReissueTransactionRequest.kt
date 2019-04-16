/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.net.model.request

import android.util.Log

import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.crypto.CryptoProvider

class ReissueTransactionRequest(val assetId: String, val senderPublicKey: String, val quantity: Long,
                                val reissuable: Boolean, val timestamp: Long) {
    val fee: Long
    var signature: String? = null

    @Transient
    val txType = 5

    init {
        this.fee = MIN_FEE
    }

    fun toSignBytes(): ByteArray {
        try {
            val reissuableBytes = if (reissuable) byteArrayOf(1) else byteArrayOf(0)

            return Bytes.concat(byteArrayOf(txType.toByte()),
                    Base58.decode(senderPublicKey),
                    Base58.decode(assetId),
                    Longs.toByteArray(quantity),
                    reissuableBytes,
                    Longs.toByteArray(fee),
                    Longs.toByteArray(timestamp))
        } catch (e: Exception) {
            Log.e("ReissueRequest", "Couldn't create toSignBytes", e)
            return ByteArray(0)
        }

    }

    fun sign(privateKey: ByteArray) {
        if (signature == null) {
            signature = Base58.encode(CryptoProvider.sign(privateKey, toSignBytes()))
        }
    }

    companion object {
        const val MIN_FEE = 100000000L
    }
}
