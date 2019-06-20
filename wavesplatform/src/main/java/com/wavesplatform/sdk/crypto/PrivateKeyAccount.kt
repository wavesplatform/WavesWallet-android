/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.crypto

import com.google.common.primitives.Bytes
import com.google.common.primitives.Ints

class PrivateKeyAccount(seed: ByteArray) {

    val publicKey: ByteArray
    val privateKey: ByteArray

    val publicKeyStr: String
        get() = Base58.encode(publicKey)

    val privateKeyStr: String
        get() = Base58.encode(privateKey)

    init {
        val hashedSeed = WavesCrypto.sha256(WavesCrypto.keccak(Bytes.concat(Ints.toByteArray(0), seed)))
        privateKey = CryptoProvider.get().generatePrivateKey(hashedSeed)
        publicKey = CryptoProvider.get().generatePublicKey(privateKey)
    }
}
