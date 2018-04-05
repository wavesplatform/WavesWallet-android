package com.wavesplatform.wallet.v1.crypto;

import fr.cryptohash.DigestEngine;
import fr.cryptohash.Keccak256;
import ove.crypto.digest.Blake2b;

public class Hash {
    public static int DigestSize = 32;

    public static byte[] hashChain(byte[] input, DigestEngine ... engines) {
        for (DigestEngine engine : engines) {
            input = engine.digest(input);
        }
        return input;
    }

    private static Blake2b.Digest blake = Blake2b.Digest.newInstance(DigestSize);
    private static Keccak256 keccak256 = new Keccak256();

    public static byte[] secureHash(byte[] input) {
        return keccak256.digest(blake.digest(input));
    }

    public static byte[] fastHash(byte[] input) {
        return blake.digest(input);
    }

}
