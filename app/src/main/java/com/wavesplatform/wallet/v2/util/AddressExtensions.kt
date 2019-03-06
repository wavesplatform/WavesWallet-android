package com.wavesplatform.wallet.v2.util

import com.wavesplatform.wallet.v1.crypto.Base58
import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager
import com.wavesplatform.wallet.v2.util.AddressUtil.calcCheckSum
import java.util.*

var AddressVersion: Byte = 1
var ChecksumLength = 4
var HashLength = 20
var AddressLength = 1 + 1 + ChecksumLength + HashLength

fun String?.isValidAddress(): Boolean {
    if (this.isNullOrEmpty()) return false
    return try {
        val bytes = Base58.decode(this)
        if (bytes.size == AddressLength &&
                bytes[0] == AddressVersion &&
                bytes[1] == EnvironmentManager.netCode) {
            val checkSum = Arrays.copyOfRange(bytes, bytes.size - ChecksumLength, bytes.size)
            val checkSumGenerated = calcCheckSum(Arrays.copyOf(bytes, bytes.size - ChecksumLength))
            Arrays.equals(checkSum, checkSumGenerated)
        } else {
            false
        }
    } catch (e: Exception) {
        false
    }
}

fun String.makeAsAlias(): String {
    return "alias:${EnvironmentManager.netCode.toChar()}:$this"
}

fun String.clearAlias(): String {
    return this.substringAfterLast(":")
}