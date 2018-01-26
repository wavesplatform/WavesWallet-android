package com.wavesplatform.wallet.ui.dex.details.my_orders;

import com.wavesplatform.wallet.payload.WatchMarket;

public class MyOrderModel {

    private WatchMarket pairModel;

    public WatchMarket getPairModel() {
        return pairModel;
    }

    public void setPairModel(WatchMarket pairModel) {
        this.pairModel = pairModel;
    }
}
