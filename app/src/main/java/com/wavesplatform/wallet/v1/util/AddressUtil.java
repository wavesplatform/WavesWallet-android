package com.wavesplatform.wallet.v1.util;

import android.text.TextUtils;

import com.google.common.primitives.Bytes;
import com.wavesplatform.wallet.v1.api.NodeManager;
import com.wavesplatform.wallet.v1.crypto.Base58;
import com.wavesplatform.wallet.v1.crypto.Hash;
import com.wavesplatform.wallet.v1.payload.AssetBalance;
import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddressUtil {
    public static byte AddressVersion = 1;
    public static int ChecksumLength = 4;
    public static int HashLength = 20;
    public static int AddressLength = 1 + 1 + ChecksumLength + HashLength;

    public static byte getAddressScheme() {
        return (byte) EnvironmentManager.get().current().getAddressScheme();
    }

    public static boolean isValidAddress(String address) {
        if (address == null) return false;
        try {
            byte[] bytes = Base58.decode(address);
            if (bytes.length == AddressLength
                    && bytes[0] == AddressVersion
                    && bytes[1] == getAddressScheme()) {
                byte[] checkSum = Arrays.copyOfRange(bytes, bytes.length - ChecksumLength, bytes.length);
                byte[] checkSumGenerated = calcCheckSum(Arrays.copyOf(bytes, bytes.length - ChecksumLength));
                return Arrays.equals(checkSum, checkSumGenerated);
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static byte[] calcCheckSum(byte[] bytes) {
        return Arrays.copyOfRange(Hash.secureHash(bytes), 0, ChecksumLength);
    }


    public static String addressFromPublicKey(byte[] publicKey) {
        byte[] publicKeyHash = Arrays.copyOf(Hash.secureHash(publicKey), HashLength);
        byte[] withoutChecksum = Bytes.concat(new byte[] {AddressVersion, getAddressScheme()}, publicKeyHash);
        return Base58.encode(Bytes.concat(withoutChecksum, calcCheckSum(withoutChecksum)));
    }

    public static String addressFromPublicKey(String publicKey) {
        try {
            byte[] bytes = Base58.decode(publicKey);
            byte[] publicKeyHash = Arrays.copyOf(Hash.secureHash(bytes), HashLength);
            byte[] withoutChecksum = Bytes.concat(new byte[] {AddressVersion, getAddressScheme()}, publicKeyHash);
            return Base58.encode(Bytes.concat(withoutChecksum, calcCheckSum(withoutChecksum)));
        } catch (Exception e) {
            return "Unknown address";
        }
    }

    public static boolean isWavesUri(String uri) {
        return uri.startsWith("waves://");
    }

    public static String generateReceiveUri(long amount, AssetBalance ab, String attachment) {
        List<String> params = new ArrayList<>();
        if (!ab.isWaves()) params.add("asset=" + ab.assetId);
        if (amount > 0) params.add("amount=" + amount);
        if (attachment != null && !attachment.isEmpty()) params.add("attachment=" + attachment);

        String paramsText = TextUtils.join("&", params);
        return "waves://" + NodeManager.get().getAddress() + (paramsText.isEmpty() ? "" : "?" + paramsText);
    }

}
