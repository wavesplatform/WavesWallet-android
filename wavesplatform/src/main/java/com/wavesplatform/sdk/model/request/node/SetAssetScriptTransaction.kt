package com.wavesplatform.sdk.model.request.node

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.WavesPlatform
import com.wavesplatform.sdk.crypto.Base58

class SetAssetScriptTransaction(
        @SerializedName("assetId") var assetId: String,
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