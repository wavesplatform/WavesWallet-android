package com.wavesplatform.wallet.v1.ui.dex.details.chart;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.v1.api.datafeed.DataFeedManager;
import com.wavesplatform.wallet.v1.data.rxjava.RxUtil;
import com.wavesplatform.wallet.v1.db.DBHelper;
import com.wavesplatform.wallet.v1.injection.Injector;
import com.wavesplatform.wallet.v1.payload.Candle;
import com.wavesplatform.wallet.v1.payload.Market;
import com.wavesplatform.wallet.v1.payload.TradesMarket;
import com.wavesplatform.wallet.v1.ui.base.BaseViewModel;
import com.wavesplatform.wallet.v1.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.v1.util.RxEventBus;
import com.wavesplatform.wallet.v1.util.SSLVerifyUtil;
import com.wavesplatform.wallet.v1.util.annotations.Thunk;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import io.reactivex.Observable;

@SuppressWarnings("WeakerAccess")
public class ChartViewModel extends BaseViewModel {

    @Inject
    SSLVerifyUtil sslVerifyUtil;
    @Inject
    RxEventBus mRxEventBus;
    @Thunk
    DataListener dataListener;
    private Context context;

    public ChartModel chartModel;
    private List<CandleEntry> entries = new ArrayList<>();
    private List<BarEntry> barEntries = new ArrayList<>();
    public int currentTimeFrame = 30;
    public long prevToDate = 0;
    //public long fromTimeFrame = 0;
    private Timer timer;

    ChartViewModel(Context context, DataListener dataListener) {

        Injector.getInstance().getDataManagerComponent().inject(this);
        this.context = context;
        this.dataListener = dataListener;

        chartModel = new ChartModel();

        sslVerifyUtil.validateSSL();
    }

    @Override
    public void onViewReady() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 2);
        chartModel.setLastLoadDate(calendar.getTime());

        if (chartModel.getPairModel().market.currentTimeFrame != null) currentTimeFrame = chartModel.getPairModel().market.currentTimeFrame;
        else currentTimeFrame = 30;

        loadCandles(new Date().getTime(), true);
        getTradesByPair();

        startTimer();
    }

    @Override
    public void destroy() {
        super.destroy();
        context = null;
        dataListener = null;
        timer.cancel();
    }

    public void pause() {
        timer.cancel();
    }

    public void resume() {
        startTimer();
    }

    public interface DataListener {

        void onShowToast(@StringRes int message, @ToastCustom.ToastType String toastType);

        void onShowCandlesSuccess(List<CandleEntry> candles, List<BarEntry> barEntries, boolean firstRequest);

        void onRefreshCandles(List<CandleEntry> candles, List<BarEntry> barEntries);

        void showProgressDialog(@StringRes int messageId, @Nullable String suffix);

        void dismissProgressDialog();

        void successGetTrades(List<TradesMarket> tradesMarket);
    }

    public void setCurrentTimeFrame(int currentTimeFrame) {
        this.currentTimeFrame = currentTimeFrame;
        DBHelper.getInstance().getRealm().executeTransaction(realm -> {
            Market market = findMarket(chartModel.getPairModel().market);
            market.currentTimeFrame = currentTimeFrame;
            realm.copyToRealmOrUpdate(market);
        });
    }

    public Market findMarket(Market market) {
        return DBHelper.getInstance().getRealm().where(Market.class)
                .equalTo("id", market.id)
                .findFirst();
    }

    private void startTimer() {
        if (timer != null) timer.cancel();

        if (timer == null) {
            timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    refreshCandles();
                    getTradesByPair();
                }
            };
            timer.scheduleAtFixedRate(timerTask, 1000 * 60, 1000 * 60);
        }
    }


    public void loadCandles(Long to, boolean firstRequest) {
        entries = new ArrayList<>();
        barEntries = new ArrayList<>();
        Long fromTimestamp = to - 100L*currentTimeFrame*1000*60;

        compositeDisposable.add(DataFeedManager.get().loadCandlesOnInterval(chartModel.getPairModel().market.amountAsset, chartModel.getPairModel().market.priceAsset, currentTimeFrame, fromTimestamp, to)
                .flatMap(candles -> {
                    chartModel.setCandleList(candles);
                    return Observable.fromIterable(candles);
                })
                .filter(candle -> Double.valueOf(candle.getVolume()) > 0)
                .map(candle -> {
                    CandleEntry e = new CandleEntry(candle.getTimestamp() / (1000 * 60 * currentTimeFrame), Float.valueOf(candle.getHigh()), Float.valueOf(candle.getLow()), Float.valueOf(candle.getOpen()), Float.valueOf(candle.getClose()));
                    entries.add(e);

                    barEntries.add(new BarEntry(candle.getTimestamp() / (1000 * 60 * currentTimeFrame), Float.valueOf(candle.getVolume())));

                    return candle;
                })
                .toList().toObservable()
                .compose(RxUtil.applySchedulersToObservable()).subscribe(tx -> {
                    if (firstRequest)
                        prevToDate = to;
                    if (dataListener != null)
                        dataListener.onShowCandlesSuccess(entries, barEntries, firstRequest);
                }, err -> {
                    if (dataListener != null)
                        dataListener.onShowToast(R.string.load_candles_failed, ToastCustom.TYPE_ERROR);
                }));
    }

    public void refreshCandles() {
        final long to = new Date().getTime();
        compositeDisposable.add(DataFeedManager.get().loadCandlesOnInterval(
            chartModel.getPairModel().market.amountAsset,
            chartModel.getPairModel().market.priceAsset,
            currentTimeFrame, prevToDate, to)
            .compose(RxUtil.applySchedulersToObservable()).subscribe(candles -> {
                List<CandleEntry> ces = new ArrayList<>();
                List<BarEntry> bes = new ArrayList<>();
                for (Candle candle : candles) {
                    if (Float.valueOf(candle.getVolume()) > 0) {
                        ces.add(new CandleEntry(candle.getTimestamp() / (1000 * 60 * currentTimeFrame), Float.valueOf(candle.getHigh()), Float.valueOf(candle.getLow()), Float.valueOf(candle.getOpen()), Float.valueOf(candle.getClose())));
                        bes.add(new BarEntry(candle.getTimestamp() / (1000 * 60 * currentTimeFrame), Float.valueOf(candle.getVolume())));
                    }
                }
                prevToDate = to;
                if (dataListener != null)
                    dataListener.onRefreshCandles(ces, bes);
            }, err -> {
                if (dataListener != null)
                    dataListener.onShowToast(R.string.load_candles_failed, ToastCustom.TYPE_ERROR);
            }));
    }

    public void getTradesByPair() {
        compositeDisposable.add(DataFeedManager.get().getTradesByPairWithoutInterval(chartModel.getPairModel().market.amountAsset, chartModel.getPairModel().market.priceAsset,"1")
                .compose(RxUtil.applySchedulersToObservable())
                .subscribe(tradesMarket -> {
                    if (dataListener != null)
                        dataListener.successGetTrades(tradesMarket);
                }, Throwable::printStackTrace));

    }

    IAxisValueFormatter valueFormatter = (value, axis) -> {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM HH:mm", Locale.getDefault());

        Date date = new Date((long) value * 1000 * 60 * currentTimeFrame);
        return simpleDateFormat.format(date);
    };

}
