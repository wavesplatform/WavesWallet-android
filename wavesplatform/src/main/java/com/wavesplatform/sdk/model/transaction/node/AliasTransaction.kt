/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model.transaction.node

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.WavesPlatform
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.utils.arrayWithSize
import java.nio.charset.Charset

class AliasTransaction(@SerializedName("aliasBytes") var alias: String = "")
    : BaseTransaction(CREATE_ALIAS) {

    override fun toBytes(): ByteArray {
        return try {
            Bytes.concat(
                    byteArrayOf(type.toByte()),
                    byteArrayOf(WavesPlatform.getEnvironment().scheme),
                    Base58.decode(senderPublicKey),
                    Bytes.concat(
                            byteArrayOf(version.toByte()),
                            byteArrayOf(WavesPlatform.getEnvironment().scheme),
                            alias.toByteArray(
                                    Charset.forName("UTF-8")).arrayWithSize()
                    ).arrayWithSize(),
                    Longs.toByteArray(fee),
                    Longs.toByteArray(timestamp))
        } catch (e: Exception) {
            Log.e("Sign", "Can't create bytes for sign in Alias Transaction", e)
            ByteArray(0)
        }
    }

    class Params {

    }
}