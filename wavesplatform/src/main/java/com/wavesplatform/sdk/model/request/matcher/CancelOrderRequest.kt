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

/**
 * Cancel Order Request in DEX-matcher, decentralized exchange of Waves.
 *
 * It collects orders from users who created CreateOrderRequest,
 * matches and sends it to blockchain it by Exchange transactions.
 */
class CancelOrderRequest(
        /**
         * Order Id of order to cancel
         */
        @SerializedName("orderId") var orderId: String = "",
        /**
         * Sender address or alias
         */
        @SerializedName("sender") var sender: String = "",
        /**
         * Order signature by account private key
         */
        @SerializedName("signature") var signature: String? = null
) {

    private fun toBytes(): ByteArray {
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
        signature = Base58.encode(CryptoProvider.sign(privateKey, toBytes()))
    }
}
