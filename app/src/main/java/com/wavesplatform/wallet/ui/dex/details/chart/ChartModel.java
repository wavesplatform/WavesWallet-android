package com.wavesplatform.wallet.ui.dex.details.chart;

import android.databinding.BaseObservable;

import com.github.mikephil.charting.data.CombinedData;
import com.wavesplatform.wallet.payload.Candle;
import com.wavesplatform.wallet.payload.WatchMarket;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChartModel extends BaseObservable {


    private List<Candle> candleList;
    private CombinedData data = new CombinedData();
    private Date lastLoadDate;
    private WatchMarket pairModel;

    public ChartModel() {
        candleList = new ArrayList<>();
    }

    public List<Candle> getCandleList() {
        return candleList;
    }

    public void setCandleList(List<Candle> candleList) {
        this.candleList = candleList;
    }

    public CombinedData getData() {
        return data;
    }

    public void setData(CombinedData data) {
        this.data = data;
    }

    public Date getLastLoadDate() {
        return lastLoadDate;
    }

    public void setLastLoadDate(Date lastLoadDate) {
        this.lastLoadDate = lastLoadDate;
    }

    public WatchMarket getPairModel() {
        return pairModel;
    }

    public void setPairModel(WatchMarket pairModel) {
        this.pairModel = pairModel;
    }
}
