/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

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

data class BurnRequest(
    @SerializedName("assetId") val assetId: String = "",
    @SerializedName("chainId") val chainId: Byte = EnvironmentManager.netCode,
    @SerializedName("fee") var fee: Long = 100000L,
    @SerializedName("quantity") var quantity: Long = 1,
    @SerializedName("senderPublicKey") var senderPublicKey: String? = "",
    @SerializedName("timestamp") var timestamp: Long = EnvironmentManager.getTime(),
    @SerializedName("type") val type: Int = Transaction.BURN,
    @SerializedName("version") val version: Byte = Constants.VERSION,
    @SerializedName("proofs") var proofs: MutableList<String?>? = null,
    @SerializedName("id") var id: String? = null
) {

    fun toSignBytes(): ByteArray {
        return try {
            Bytes.concat(
                    byteArrayOf(Transaction.BURN.toByte()),
                    byteArrayOf(Constants.VERSION),
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