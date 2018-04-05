package com.wavesplatform.wallet.v1.util;

import android.util.Pair;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BackupWalletUtil {

    public BackupWalletUtil() {
        // Empty Constructor
    }

    /**
     * Return ordered list of integer, string pairs which can be used to confirm mnemonic.
     *
     * @return List<Pair<Integer,String>>
     */
    public List<Pair<Integer, String>> getConfirmSequence(String secondPassword) {

        List<Pair<Integer, String>> toBeConfirmed = new ArrayList<>();
        String[] s = getMnemonic(secondPassword);
        SecureRandom random = new SecureRandom();
        List<Integer> seen = new ArrayList<>();

        int sel = 0;
        int i = 0;
        while (i < 3) {
            sel = random.nextInt(s.length);
            if (!seen.contains(sel)) {
                seen.add(sel);
                i++;
            }
        }

        Collections.sort(seen);

        for (int ii = 0; ii < 3; ii++) {
            toBeConfirmed.add(new Pair<>(seen.get(ii), s[seen.get(ii)]));
        }

        return toBeConfirmed;
    }

    /**
     * Return mnemonic in the form of a string array.
     *
     * @return String[]
     */
    public String[] getMnemonic(String secondPassword) {

        return new String[]{"123"};// new String(NodeManager.get().getWallet().getSeed()).split(" ");
        /*try {
            if(PayloadManager.createInstance().getPayload().isDoubleEncrypted()) {
                return PayloadManager.createInstance().getMnemonic(secondPassword);
            } else {
                return PayloadManager.createInstance().getMnemonic();
            }
        } catch (Exception e) {
            Log.e(BackupWalletUtil.class.getSimpleName(), "getMnemonic: ", e);
            return null;
        }*/
    }
}
