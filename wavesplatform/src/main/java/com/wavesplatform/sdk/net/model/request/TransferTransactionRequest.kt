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
import com.wavesplatform.sdk.utils.SignUtil

class TransferTransactionRequest {

    var assetId: String? = null
    var senderPublicKey: String? = null
    var recipient: String? = null
    var amount: Long = 0
    var timestamp: Long = 0
    var feeAssetId: String? = null
    var fee: Long = 0
    var attachment: String? = null
    var signature: String? = null

    @Transient
    val txType = 4

    fun toSignBytes(): ByteArray {
        try {
            val timestampBytes = Longs.toByteArray(timestamp)
            val assetIdBytes = SignUtil.arrayOption(assetId!!)
            val amountBytes = Longs.toByteArray(amount)
            val feeAssetIdBytes = SignUtil.arrayOption(feeAssetId!!)
            val feeBytes = Longs.toByteArray(fee)

            return Bytes.concat(byteArrayOf(txType.toByte()),
                    Base58.decode(senderPublicKey!!),
                    assetIdBytes,
                    feeAssetIdBytes,
                    timestampBytes,
                    amountBytes,
                    feeBytes,
                    Base58.decode(recipient!!),
                    SignUtil.arrayWithSize(attachment))
        } catch (e: Exception) {
            Log.e("Wallet", "Couldn't create transaction sign", e)
            return ByteArray(0)
        }

    }

    fun sign(privateKey: ByteArray) {
        if (signature == null) {
            signature = Base58.encode(CryptoProvider.sign(privateKey, toSignBytes()))
        }
    }

    companion object {
        const val SignatureLength = 64
        const val KeyLength = 32
        const val MaxAttachmentSize = 140
        const val MinFee = 100000
    }
}
