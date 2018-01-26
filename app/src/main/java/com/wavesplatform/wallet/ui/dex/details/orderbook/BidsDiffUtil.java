package com.wavesplatform.wallet.ui.dex.details.orderbook;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.wavesplatform.wallet.payload.OrderBook;

import java.util.List;

class BidsDiffUtil extends DiffUtil.Callback {

    private List<OrderBook.Bids> oldBis;
    private List<OrderBook.Bids> newBids;

    BidsDiffUtil(List<OrderBook.Bids> oldBis, List<OrderBook.Bids> newBids) {
        this.oldBis = oldBis;
        this.newBids = newBids;
    }

    @Override
    public int getOldListSize() {
        return oldBis != null ? oldBis.size() : 0;
    }

    @Override
    public int getNewListSize() {
        return newBids != null ? newBids.size() : 0;
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldBis.get(oldItemPosition).amount.equals(
                newBids.get(newItemPosition).amount);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldBis.get(oldItemPosition).equals(newBids.get(newItemPosition));
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
