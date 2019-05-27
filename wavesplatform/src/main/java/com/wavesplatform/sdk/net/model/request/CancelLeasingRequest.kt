/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.net.model.request

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.utils.Constants
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.crypto.CryptoProvider
import com.wavesplatform.sdk.net.model.response.TransactionResponse
import com.wavesplatform.sdk.Wavesplatform

data class CancelLeasingRequest(
        @SerializedName("type") val type: Int = TransactionResponse.LEASE_CANCEL,
        @SerializedName("chainId") var scheme: Int? = Wavesplatform.getNetCode().toInt(),
        @SerializedName("senderPublicKey") var senderPublicKey: String = "",
        @SerializedName("leaseId") var leaseId: String = "",
        @SerializedName("timestamp") var timestamp: Long = Wavesplatform.getTime(),
        @SerializedName("fee") var fee: Long = 0,
        @SerializedName("version") var version: Int = Constants.VERSION,
        @SerializedName("proofs") var proofs: MutableList<String?>? = null
) {

    fun toSignBytes(): ByteArray {
        return try {
            Bytes.concat(byteArrayOf(type.toByte()),
                    byteArrayOf(Constants.VERSION.toByte()),
                    byteArrayOf(Wavesplatform.getNetCode()),
                    Base58.decode(senderPublicKey),
                    Longs.toByteArray(fee),
                    Longs.toByteArray(timestamp),
                    Base58.decode(leaseId)
            )
        } catch (e: Exception) {
            Log.e("CancelLeasingRequest", "Couldn't create toSignBytes", e)
            ByteArray(0)
        }
    }

    fun sign(privateKey: ByteArray) {
        proofs = mutableListOf(Base58.encode(CryptoProvider.sign(privateKey, toSignBytes())))
    }
}