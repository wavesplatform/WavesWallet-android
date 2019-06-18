/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model.request.matcher

import android.util.Log

import com.google.common.primitives.Bytes
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.crypto.CryptoProvider

class CancelOrderRequest(
    @SerializedName("orderId") var orderId: String = "",
    @SerializedName("sender") var sender: String = "",
    @SerializedName("signature") var signature: String? = null
) {

    private fun toSignBytes(): ByteArray {
        return try {
            Bytes.concat(
                    Base58.decode(sender),
                    Base58.decode(orderId)
            )
        } catch (e: Exception) {
            Log.e("Wallet", "Couldn't create CancelOrderRequest bytes", e)
            ByteArray(0)
        }
    }

    fun sign(privateKey: ByteArray) {
        signature = Base58.encode(CryptoProvider.sign(privateKey, toSignBytes()))
    }
}
