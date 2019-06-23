package com.wavesplatform.sdk.model.request.node

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.WavesPlatform
import com.wavesplatform.sdk.crypto.Base58

/**
 * Set asset script transaction (set script to asset)
 * An asset script is a script that is attached to an asset with a set asset script transaction.
 * An asset with the attached script is called a smart asset.
 * You can attach a script to an asset only during the creation of the asset.
 * Script can be developed with [Waves Ride IDE]({https://ide.wavesplatform.com/)
 *
 * Smart assets are unique virtual currency tokens that may represent a tangible real-world asset,
 * or a non-tangible ownership that can be purchased, sold, or exchanged as defined
 * by the rules of a script on the Waves blockchain network.
 */
class SetAssetScriptTransaction(
        /**
         * Selected for script asset Id
         */
        @SerializedName("assetId") var assetId: String,
        /**
         * Base64 binary string with Waves Ride script, starts with "base64:"
         */
        @SerializedName("script") var script: String)
    : BaseTransaction(ASSET_SCRIPT) {

    override fun toBytes(): ByteArray {
        return try {
            Bytes.concat(byteArrayOf(type.toByte()),
                    byteArrayOf(version.toByte()),
                    byteArrayOf(WavesPlatform.getEnvironment().scheme),
                    Base58.decode(senderPublicKey),
                    Base58.decode(assetId),
                    Longs.toByteArray(fee),
                    Longs.toByteArray(timestamp),
                    Base58.decode(script))
            // todo check https://github.com/wavesplatform/marshall/blob/master/src/schemas.ts : 492
        } catch (e: Exception) {
            Log.e("Sign", "Can't create bytes for sign in Data Transaction", e)
            ByteArray(0)
        }
    }
}