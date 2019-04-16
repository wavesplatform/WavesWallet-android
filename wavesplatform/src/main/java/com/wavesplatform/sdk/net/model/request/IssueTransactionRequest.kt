/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.net.model.request

import android.util.Log

import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.common.primitives.Shorts
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.crypto.CryptoProvider
import com.wavesplatform.sdk.crypto.Hash

import org.apache.commons.lang3.ArrayUtils

class IssueTransactionRequest(val senderPublicKey: String, val name: String?, description: String?,
                              val quantity: Long, val decimals: Byte, val reissuable: Boolean, val timestamp: Long) {

    val id: String
    val description: String
    val fee: Long
    var signature: String? = null

    @Transient
    val txType = 3
    @Transient
    val nameBytes: ByteArray
    @Transient
    val descriptionBytes: ByteArray

    init {
        this.nameBytes = name?.toByteArray(org.apache.commons.io.Charsets.UTF_8)
                ?: ArrayUtils.EMPTY_BYTE_ARRAY
        this.descriptionBytes = description?.toByteArray(org.apache.commons.io.Charsets.UTF_8)
                ?: ArrayUtils.EMPTY_BYTE_ARRAY
        this.description = description ?: ""
        this.fee = MinFee.toLong()
        this.id = Base58.encode(Hash.fastHash(toSignBytes()))
    }

    fun toSignBytes(): ByteArray {
        try {
            val reissuableBytes = if (reissuable) byteArrayOf(1) else byteArrayOf(0)

            return Bytes.concat(byteArrayOf(txType.toByte()),
                    Base58.decode(senderPublicKey),
                    arrayWithSize(nameBytes),
                    arrayWithSize(descriptionBytes),
                    Longs.toByteArray(quantity),
                    byteArrayOf(decimals),
                    reissuableBytes,
                    Longs.toByteArray(fee),
                    Longs.toByteArray(timestamp))
        } catch (e: Exception) {
            Log.e("Wallet", "Couldn't create issue transaction sign", e)
            return ByteArray(0)
        }

    }

    fun sign(privateKey: ByteArray) {
        if (signature == null) {
            signature = Base58.encode(CryptoProvider.sign(privateKey, toSignBytes()))
        }
    }

    companion object {
        var MaxDescriptionLength = 1000
        var MinFee = 100000000
        var MaxAssetNameLength = 16
        var MinAssetNameLength = 4
        var MaxDecimals = 8

        private fun arrayWithSize(b: ByteArray): ByteArray {
            return Bytes.concat(Shorts.toByteArray(b.size.toShort()), b)
        }
    }
}
