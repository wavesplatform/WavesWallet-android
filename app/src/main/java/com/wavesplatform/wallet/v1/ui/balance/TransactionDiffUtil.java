package com.wavesplatform.wallet.v1.ui.balance;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.wavesplatform.wallet.v1.payload.Transaction;

import java.util.List;

class TransactionDiffUtil extends DiffUtil.Callback {

    private List<Transaction> oldTransactions;
    private List<Transaction> newTransactions;

    TransactionDiffUtil(List<Transaction> oldTransactions, List<Transaction> newTransactions) {
        this.oldTransactions = oldTransactions;
        this.newTransactions = newTransactions;
    }

    @Override
    public int getOldListSize() {
        return oldTransactions != null ? oldTransactions.size() : 0;
    }

    @Override
    public int getNewListSize() {
        return newTransactions != null ? newTransactions.size() : 0;
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldTransactions.get(oldItemPosition).id.equals(
                newTransactions.get(newItemPosition).id);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Transaction oldTransaction = oldTransactions.get(oldItemPosition);
        Transaction newTransaction = newTransactions.get(newItemPosition);

        return oldTransaction.id.equals(newTransaction.id)
                && oldTransaction.isPending == newTransaction.isPending;
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
