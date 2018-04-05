package com.wavesplatform.wallet.v1.ui.dex.details.last_trades;

import android.databinding.BaseObservable;

import com.wavesplatform.wallet.v1.payload.WatchMarket;

public class LastTradeModel extends BaseObservable {

    private WatchMarket pairModel;

    public WatchMarket getPairModel() {
        return pairModel;
    }

    public void setPairModel(WatchMarket pairModel) {
        this.pairModel = pairModel;
    }
}
