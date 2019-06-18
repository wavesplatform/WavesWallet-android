package com.wavesplatform.sdk.model.request.node

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.utils.SignUtil

class MassTransferTransaction(@SerializedName("attachment") var attachment: String? = null)
    : BaseTransaction(MASS_TRANSFER) {

    override fun toBytes(): ByteArray {
        return try {
            Bytes.concat(byteArrayOf(type.toByte()),
                    byteArrayOf(version.toByte()),
                    Base58.decode(senderPublicKey),
                    // todo txFields.optionalAssetId,
                    // todo txFields.transfers,
                    Longs.toByteArray(fee),
                    Longs.toByteArray(timestamp),
                    SignUtil.arrayWithSize(Base58.encode((attachment ?: "").toByteArray())))
            // todo check https://github.com/wavesplatform/marshall/blob/master/src/schemas.ts : 463
        } catch (e: Exception) {
            Log.e("Sign", "Can't create bytes for sign in SetScript Transaction", e)
            ByteArray(0)
        }
    }
}