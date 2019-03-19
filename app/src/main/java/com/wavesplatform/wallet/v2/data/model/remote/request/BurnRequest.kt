package com.wavesplatform.wallet.v2.data.model.remote.request

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v1.crypto.Base58
import com.wavesplatform.wallet.v1.crypto.CryptoProvider
import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import pers.victor.ext.currentTimeMillis

data class BurnRequest(
    @SerializedName("assetId") val assetId: String = "",
    @SerializedName("chainId") val chainId: Byte = EnvironmentManager.netCode,
    @SerializedName("fee") var fee: Long = 100000L,
    @SerializedName("quantity") var quantity: Long = 1,
    @SerializedName("senderPublicKey") var senderPublicKey: String? = "",
    @SerializedName("timestamp") var timestamp: Long = currentTimeMillis,
    @SerializedName("type") val type: Int = Transaction.BURN,
    @SerializedName("version") val version: Int = Constants.VERSION,
    @SerializedName("proofs") var proofs: MutableList<String?>? = null,
    @SerializedName("id") var id: String? = null
) {

    fun toSignBytes(): ByteArray {
        return try {
            Bytes.concat(
                    byteArrayOf(Transaction.BURN.toByte()),
                    byteArrayOf(Constants.VERSION.toByte()),
                    byteArrayOf(chainId),
                    Base58.decode(App.getAccessManager().getWallet()!!.publicKeyStr),
                    Base58.decode(assetId),
                    Longs.toByteArray(quantity),
                    Longs.toByteArray(fee),
                    Longs.toByteArray(timestamp))
        } catch (e: Exception) {
            Log.e("BurnRequest", "Couldn't create toSignBytes", e)
            ByteArray(0)
        }
    }

    fun sign(privateKey: ByteArray) {
        proofs = mutableListOf(Base58.encode(CryptoProvider.sign(privateKey, toSignBytes())))
    }
}