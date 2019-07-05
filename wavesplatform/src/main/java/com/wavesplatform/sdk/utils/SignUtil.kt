/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.utils

import com.google.common.primitives.Bytes
import com.google.common.primitives.Shorts
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.crypto.WavesCrypto
import java.nio.charset.Charset

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


    @Throws(Base58.InvalidBase58::class)
    fun attachmentBytes(attachment: String): ByteArray {
        return if (attachment.isEmpty()) {
            byteArrayOf(0, 0)
        } else {
            WavesCrypto.base58decode(attachment).arrayWithSize()
        }
    }

    @Throws(Base58.InvalidBase58::class)
    fun recipientBytes(recipient: String, version: Byte, chainId: Byte): ByteArray {
        return if (recipient.length <= 30) {
            Bytes.concat(
                byteArrayOf(version),
                byteArrayOf(chainId),
                recipient.parseAlias().toByteArray(Charset.forName("UTF-8")).arrayWithSize()
            )
        } else {
            WavesCrypto.base58decode(recipient)
        }
    }
}
