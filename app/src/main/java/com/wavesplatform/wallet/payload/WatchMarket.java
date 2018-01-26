package com.wavesplatform.wallet.payload;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by anonymous on 27.06.17.
 */

public class WatchMarket implements Parcelable{

    public Market market;
    public TickerMarket tickerMarket;
    public TradesMarket tradesMarket;

    public WatchMarket(Market market, TickerMarket tickerMarket, TradesMarket tradesMarket) {
        this.market = market;
        this.tickerMarket = tickerMarket;
        this.tradesMarket = tradesMarket;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.market, flags);
        dest.writeParcelable(this.tickerMarket, flags);
        dest.writeParcelable(this.tradesMarket, flags);
    }

    protected WatchMarket(Parcel in) {
        this.market = in.readParcelable(Market.class.getClassLoader());
        this.tickerMarket = in.readParcelable(TickerMarket.class.getClassLoader());
        this.tradesMarket = in.readParcelable(TradesMarket.class.getClassLoader());
    }

    public static final Creator<WatchMarket> CREATOR = new Creator<WatchMarket>() {
        @Override
        public WatchMarket createFromParcel(Parcel source) {
            return new WatchMarket(source);
        }

        @Override
        public WatchMarket[] newArray(int size) {
            return new WatchMarket[size];
        }
    };
}
