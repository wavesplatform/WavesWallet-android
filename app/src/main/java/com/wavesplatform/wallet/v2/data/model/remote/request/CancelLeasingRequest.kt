package com.wavesplatform.wallet.v2.data.model.remote.request

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.wallet.v1.crypto.Base58
import com.wavesplatform.wallet.v1.crypto.CryptoProvider
import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction

data class CancelLeasingRequest(
    @SerializedName("type") val type: Int = Transaction.LEASE_CANCEL,
    @SerializedName("chainId") var scheme: Int? = EnvironmentManager.netCode.toInt(),
    @SerializedName("senderPublicKey") var senderPublicKey: String? = "",
    @SerializedName("leaseId") var leaseId: String = "",
    @SerializedName("timestamp") var timestamp: Long = 0,
    @SerializedName("fee") var fee: Long = 0,
    @SerializedName("version") var version: Int = Constants.VERSION,
    @SerializedName("proofs") var proofs: MutableList<String?>? = null
) {

    fun toSignBytes(): ByteArray {
        return try {
            Bytes.concat(byteArrayOf(type.toByte()),
                    byteArrayOf(Constants.VERSION.toByte()),
                    byteArrayOf(EnvironmentManager.netCode),
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