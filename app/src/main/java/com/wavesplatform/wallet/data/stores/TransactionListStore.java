package com.wavesplatform.wallet.data.stores;

import com.wavesplatform.wallet.payload.Transaction;
import com.wavesplatform.wallet.payload.TransactionMostRecentDateComparator;

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
