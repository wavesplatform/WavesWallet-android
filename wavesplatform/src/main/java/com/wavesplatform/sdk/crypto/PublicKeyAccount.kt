/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.crypto

import com.wavesplatform.sdk.utils.addressFromPublicKey

class PublicKeyAccount @Throws(InvalidPublicKey::class)
constructor(val publicKeyStr: String) {

    val publicKey: ByteArray
    val address: String

    class InvalidPublicKey : Exception()

    init {
        if (publicKeyStr.length > KeyStringLength) throw InvalidPublicKey()
        try {
            this.publicKey = Base58.decode(publicKeyStr)
        } catch (invalidBase58: Base58.InvalidBase58) {
            throw InvalidPublicKey()
        }

        this.address = addressFromPublicKey(publicKey)
    }

    companion object {
        var KeyStringLength = Base58.base58Length(32)
    }
}
