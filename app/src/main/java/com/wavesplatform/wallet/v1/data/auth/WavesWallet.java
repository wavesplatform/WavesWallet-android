/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v1.data.auth;

import com.wavesplatform.wallet.v1.crypto.AESUtil;
import com.wavesplatform.wallet.v1.crypto.Base58;
import com.wavesplatform.wallet.v1.crypto.PrivateKeyAccount;
import com.wavesplatform.wallet.v2.util.AddressUtil;

import org.apache.commons.io.Charsets;

public class WavesWallet {
    public static final int DEFAULT_PBKDF2_ITERATIONS_V2 = 5000;
    private final PrivateKeyAccount account;
    private final String address;
    private final byte[] seed;

    public WavesWallet(byte[] seed) {
        this.seed = seed;
        account = new PrivateKeyAccount(seed);
        address = AddressUtil.addressFromPublicKey(account.getPublicKey());
    }

    public WavesWallet(String walletData, String password) throws Exception {
        this(Base58.decode(AESUtil.decrypt(walletData, password, DEFAULT_PBKDF2_ITERATIONS_V2)));
    }

    public String getAddress() {
        return address;
    }

    public String getPublicKeyStr() {
        return account.getPublicKeyStr();
    }

    public byte[] getPrivateKey() {
        return account.getPrivateKey();
    }

    public String getPrivateKeyStr() {
        return account.getPrivateKeyStr();
    }

    public String getEncryptedData(String password) throws Exception {
        return AESUtil.encrypt(Base58.encode(seed), password, DEFAULT_PBKDF2_ITERATIONS_V2);
    }

    public byte[] getSeed() {
        return seed;
    }

    public String getSeedStr() {
        return new String(seed, Charsets.UTF_8);
    }
}
