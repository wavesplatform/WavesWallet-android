package com.wavesplatform.wallet.ui.dex.details;

import android.databinding.BaseObservable;

import com.wavesplatform.wallet.payload.WatchMarket;

public class DexDetailsModel extends BaseObservable {

    private WatchMarket mWatchMarket;

    public WatchMarket getWatchMarket() {
        return mWatchMarket;
    }

    public void setWatchMarket(WatchMarket watchMarket) {
        this.mWatchMarket = watchMarket;
    }
}
