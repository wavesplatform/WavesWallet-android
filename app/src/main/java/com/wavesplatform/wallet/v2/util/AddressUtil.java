package com.wavesplatform.wallet.v2.util;

import com.google.common.primitives.Bytes;
import com.wavesplatform.wallet.v1.crypto.Base58;
import com.wavesplatform.wallet.v1.crypto.Hash;
import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager;

import java.util.Arrays;

public class AddressUtil {
    public static byte AddressVersion = 1;
    public static int ChecksumLength = 4;
    public static int HashLength = 20;
    public static String WAVES_PREFIX = "waves://";

    public static byte[] calcCheckSum(byte[] bytes) {
        return Arrays.copyOfRange(Hash.secureHash(bytes), 0, ChecksumLength);
    }


    public static String addressFromPublicKey(byte[] publicKey) {
        byte[] publicKeyHash = Arrays.copyOf(Hash.secureHash(publicKey), HashLength);
        byte[] withoutChecksum = Bytes.concat(new byte[] {AddressVersion,
                EnvironmentManager.Companion.getNetCode()}, publicKeyHash);
        return Base58.encode(Bytes.concat(withoutChecksum, calcCheckSum(withoutChecksum)));
    }

    public static String addressFromPublicKey(String publicKey) {
        try {
            byte[] bytes = Base58.decode(publicKey);
            byte[] publicKeyHash = Arrays.copyOf(Hash.secureHash(bytes), HashLength);
            byte[] withoutChecksum = Bytes.concat(new byte[] {AddressVersion,
                    EnvironmentManager.Companion.getNetCode()}, publicKeyHash);
            return Base58.encode(Bytes.concat(withoutChecksum, calcCheckSum(withoutChecksum)));
        } catch (Exception e) {
            return "Unknown address";
        }
    }
}
