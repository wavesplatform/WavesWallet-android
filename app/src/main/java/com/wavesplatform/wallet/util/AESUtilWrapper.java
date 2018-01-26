package com.wavesplatform.wallet.util;

import com.wavesplatform.wallet.crypto.AESUtil;

import org.spongycastle.crypto.InvalidCipherTextException;

import java.io.UnsupportedEncodingException;

public class AESUtilWrapper {

    public String decrypt(String ciphertext, String password, int iterations)
            throws UnsupportedEncodingException, InvalidCipherTextException, AESUtil.DecryptionException {
        return AESUtil.decrypt(ciphertext, password, iterations);
    }

    public String encrypt(String plaintext, String password, int iterations) throws Exception {
        return AESUtil.encrypt(plaintext, password, iterations);
    }

}
