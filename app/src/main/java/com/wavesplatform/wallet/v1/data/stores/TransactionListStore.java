package com.wavesplatform.wallet.v1.data.stores;

import com.wavesplatform.wallet.v1.payload.Transaction;
import com.wavesplatform.wallet.v1.payload.TransactionMostRecentDateComparator;

import java.util.List;

public class TransactionListStore extends ListStore<Transaction> {

    public TransactionListStore() {
        // Empty constructor
    }

    public void insertTransactionIntoListAndSort(Transaction transaction) {
        insertObjectIntoList(transaction);
        sort(new TransactionMostRecentDateComparator());
    }

    public void insertTransactions(List<Transaction> transactions) {
        insertBulk(transactions);
    }
}
