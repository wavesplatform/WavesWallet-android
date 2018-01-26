package com.wavesplatform.wallet.payload;

import android.os.Parcel;
import android.os.Parcelable;

public class Candle implements Parcelable{

    private Long timestamp;
    private String open;
    private String high;
    private String low;
    private String close;
    private String volume;
    private String priceVolume;
    private boolean confirmed;

    public Candle() {
    }

    public Candle(Long timestamp, String open, String high, String low, String close, String volume, String priceVolume, boolean confirmed) {
        this.timestamp = timestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.priceVolume = priceVolume;
        this.confirmed = confirmed;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getOpen() {
        return open;
    }

    public void setOpen(String open) {
        this.open = open;
    }

    public String getHigh() {
        return high;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public String getLow() {
        return low;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public String getClose() {
        return close;
    }

    public void setClose(String close) {
        this.close = close;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getPriceVolume() {
        return priceVolume;
    }

    public void setPriceVolume(String priceVolume) {
        this.priceVolume = priceVolume;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.timestamp);
        dest.writeString(this.open);
        dest.writeString(this.high);
        dest.writeString(this.low);
        dest.writeString(this.close);
        dest.writeString(this.volume);
        dest.writeString(this.priceVolume);
        dest.writeByte(this.confirmed ? (byte) 1 : (byte) 0);
    }

    protected Candle(Parcel in) {
        this.timestamp = in.readLong();
        this.open = in.readString();
        this.high = in.readString();
        this.low = in.readString();
        this.close = in.readString();
        this.volume = in.readString();
        this.priceVolume = in.readString();
        this.confirmed = in.readByte() != 0;
    }

    public static final Creator<Candle> CREATOR = new Creator<Candle>() {
        @Override
        public Candle createFromParcel(Parcel source) {
            return new Candle(source);
        }

        @Override
        public Candle[] newArray(int size) {
            return new Candle[size];
        }
    };
}
