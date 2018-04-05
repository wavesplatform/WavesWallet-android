package com.wavesplatform.wallet.v1.crypto;

import com.wavesplatform.wallet.v1.util.AddressUtil;

public class PublicKeyAccount {
    public static class InvalidPublicKey extends Exception {}

    private final byte[] publicKey;
    private final String address;
    private final String publicKeyStr;

    public static int KeyStringLength = Base58.base58Length(32);

    public PublicKeyAccount(String s) throws InvalidPublicKey {
        this.publicKeyStr = s;
        if (s.length() > KeyStringLength) throw new InvalidPublicKey();
        try {
            this.publicKey = Base58.decode(s);
        } catch (Base58.InvalidBase58 invalidBase58) {
            throw new InvalidPublicKey();
        }
        this.address = AddressUtil.addressFromPublicKey(publicKey);
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public String getAddress() {
        return address;
    }

    public String getPublicKeyStr() {
        return publicKeyStr;
    }

}
