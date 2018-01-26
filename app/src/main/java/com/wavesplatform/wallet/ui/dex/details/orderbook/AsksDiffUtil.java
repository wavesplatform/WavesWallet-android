package com.wavesplatform.wallet.ui.dex.details.orderbook;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.wavesplatform.wallet.payload.OrderBook;

import java.util.List;

class AsksDiffUtil extends DiffUtil.Callback {

    private List<OrderBook.Asks> oldAsks;
    private List<OrderBook.Asks> newAsks;

    AsksDiffUtil(List<OrderBook.Asks> oldAsks, List<OrderBook.Asks> newAsks) {
        this.oldAsks = oldAsks;
        this.newAsks = newAsks;
    }

    @Override
    public int getOldListSize() {
        return oldAsks != null ? oldAsks.size() : 0;
    }

    @Override
    public int getNewListSize() {
        return newAsks != null ? newAsks.size() : 0;
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldAsks.get(oldItemPosition).amount.equals(
                newAsks.get(newItemPosition).amount);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldAsks.get(oldItemPosition).equals(newAsks.get(newItemPosition));
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
