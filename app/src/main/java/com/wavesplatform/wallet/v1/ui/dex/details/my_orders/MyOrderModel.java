package com.wavesplatform.wallet.v1.ui.dex.details.my_orders;

import com.wavesplatform.wallet.v1.payload.WatchMarket;

public class MyOrderModel {

    private WatchMarket pairModel;

    public WatchMarket getPairModel() {
        return pairModel;
    }

    public void setPairModel(WatchMarket pairModel) {
        this.pairModel = pairModel;
    }
}
