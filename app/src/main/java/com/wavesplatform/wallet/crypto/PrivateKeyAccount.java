package com.wavesplatform.wallet.crypto;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

public class PrivateKeyAccount {

    private byte[] publicKey;
    private byte[] privateKey;

    public PrivateKeyAccount(byte[] seed) {
        byte[] hashedSeed = Sha256.hash(Hash.secureHash(Bytes.concat(Ints.toByteArray(0), seed)));

        privateKey = CryptoProvider.get().generatePrivateKey(hashedSeed);
        publicKey = CryptoProvider.get().generatePublicKey(privateKey);
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public String getPublicKeyStr() {
        return Base58.encode(getPublicKey());
    }
}
