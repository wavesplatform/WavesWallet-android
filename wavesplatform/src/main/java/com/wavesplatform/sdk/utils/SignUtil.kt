package com.wavesplatform.sdk.utils

import com.google.common.primitives.Bytes
import com.google.common.primitives.Shorts
import com.wavesplatform.sdk.crypto.Base58

object SignUtil {

    @Throws(Base58.InvalidBase58::class)
    fun arrayWithSize(s: String?): ByteArray {
        return if (s != null && s.isNotEmpty()) {
            val b = Base58.decode(s)
            Bytes.concat(Shorts.toByteArray(b.size.toShort()), b)
        } else {
            Shorts.toByteArray(0.toShort())
        }
    }

    @Throws(Base58.InvalidBase58::class)
    fun arrayOption(o: String): ByteArray {
        return if (org.apache.commons.lang3.StringUtils.isEmpty(o))
            byteArrayOf(0)
        else
            Bytes.concat(byteArrayOf(1), Base58.decode(o))
    }
}
