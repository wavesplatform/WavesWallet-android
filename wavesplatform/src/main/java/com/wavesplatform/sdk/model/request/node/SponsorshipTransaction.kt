package com.wavesplatform.sdk.model.request.node

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.crypto.Base58

class SponsorshipTransaction(@SerializedName("assetId")
                             var assetId: String,
                             @SerializedName("minSponsoredAssetFee")
                             var minSponsoredAssetFee: Long)
    : BaseTransaction(SPONSORSHIP) {

    override fun toBytes(): ByteArray {
        return try {
            Bytes.concat(byteArrayOf(type.toByte()),
                    byteArrayOf(version.toByte()),
                    Base58.decode(senderPublicKey),
                    Base58.decode(assetId),
                    Longs.toByteArray(minSponsoredAssetFee),
                    Longs.toByteArray(fee),
                    Longs.toByteArray(timestamp))
        } catch (e: Exception) {
            Log.e("Sign", "Can't create bytes for sign in Sponsorship Transaction", e)
            ByteArray(0)
        }
    }
}