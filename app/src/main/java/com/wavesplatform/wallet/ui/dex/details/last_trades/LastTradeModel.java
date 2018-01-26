package com.wavesplatform.wallet.ui.dex.details.last_trades;

import android.databinding.BaseObservable;

import com.wavesplatform.wallet.payload.WatchMarket;

public class LastTradeModel extends BaseObservable {

    private WatchMarket pairModel;

    public WatchMarket getPairModel() {
        return pairModel;
    }

    public void setPairModel(WatchMarket pairModel) {
        this.pairModel = pairModel;
    }
}
