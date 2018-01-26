package com.wavesplatform.wallet.payload;

import java.util.Comparator;

public class TransactionMostRecentDateComparator implements Comparator<Transaction> {

    public int compare(Transaction t1, Transaction t2) {

        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        int ret;

        if (t1.timestamp > t2.timestamp) {
            ret = BEFORE;
        } else if (t1.timestamp < t2.timestamp) {
            ret = AFTER;
        } else {
            ret = EQUAL;
        }

        return ret;
    }

}
