package com.wavesplatform.wallet.v1.ui.dex.details;

import android.databinding.BaseObservable;

import com.wavesplatform.wallet.v1.payload.WatchMarket;

public class DexDetailsModel extends BaseObservable {

    private WatchMarket mWatchMarket;

    public WatchMarket getWatchMarket() {
        return mWatchMarket;
    }

    public void setWatchMarket(WatchMarket watchMarket) {
        this.mWatchMarket = watchMarket;
    }
}
