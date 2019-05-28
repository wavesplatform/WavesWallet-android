/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.crypto;

import android.util.Log;

import com.google.common.base.Joiner;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class WalletManager {
    private static WalletManager instance;

    /**
     * @return Generated random secret seed-phrase, seed recovery phrase or backup seed phrase
     * and contains 15 different words from dictionary.
     */
    public static String createWalletSeed() {
        try {
            int nbWords = 15;
            int len = nbWords / 3 * 4;
            SecureRandom random = new SecureRandom();
            byte[] seed = new byte[len];
            random.nextBytes(seed);

            if (Words.INSTANCE.getList().size() != 2048) {
                throw new IllegalArgumentException("WalletManager: Words list did not contain 2048 words");
            }

            return Joiner.on(" ").join(toMnemonic(seed, Words.INSTANCE.getList()));
        } catch (Exception e) {
            Log.e(WalletManager.class.getSimpleName(), "createWalletSeed: ", e);
            return null;
        }
    }

    private static List<String> toMnemonic(byte[] entropy, List<String> allWords) throws IllegalAccessException {
        if (entropy.length % 4 > 0)
            throw new IllegalAccessException("Entropy length not multiple of 32 bits.");

        if (entropy.length == 0)
            throw new IllegalAccessException("Entropy is empty.");

        // We take initial entropy of ENT bits and compute its
        // checksum by taking first ENT / 32 bits of its SHA256 hash.

        byte[] hash = Sha256.Companion.hash(entropy);
        boolean[] hashBits = bytesToBits(hash);

        boolean[] entropyBits = bytesToBits(entropy);
        int checksumLengthBits = entropyBits.length / 32;

        // We append these bits to the end of the initial entropy.
        boolean[] concatBits = new boolean[entropyBits.length + checksumLengthBits];
        System.arraycopy(entropyBits, 0, concatBits, 0, entropyBits.length);
        System.arraycopy(hashBits, 0, concatBits, entropyBits.length, checksumLengthBits);

        // Next we take these concatenated bits and split them into
        // groups of 11 bits. Each group encodes number from 0-2047
        // which is a position in a wordlist.  We convert numbers into
        // words and use joined words as mnemonic sentence.

        ArrayList<String> words = new ArrayList<String>();
        int nwords = concatBits.length / 11;
        for (int i = 0; i < nwords; ++i) {
            int index = 0;
            for (int j = 0; j < 11; ++j) {
                index <<= 1;
                if (concatBits[(i * 11) + j])
                    index |= 0x1;
            }
            words.add(allWords.get(index));
        }

        return words;
    }

    private static boolean[] bytesToBits(byte[] data) {
        boolean[] bits = new boolean[data.length * 8];
        for (int i = 0; i < data.length; ++i)
            for (int j = 0; j < 8; ++j)
                bits[(i * 8) + j] = (data[i] & (1 << (7 - j))) != 0;
        return bits;
    }

    public static WalletManager get() {
        if (instance == null) {
            instance = new WalletManager();
        }
        return instance;
    }
}
