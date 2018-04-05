package com.wavesplatform.wallet.v1.ui.dex.details.chart;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet;
import com.github.mikephil.charting.listener.BarLineChartTouchListener;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.databinding.FragmentChartBinding;
import com.wavesplatform.wallet.v1.payload.TradesMarket;
import com.wavesplatform.wallet.v1.payload.WatchMarket;
import com.wavesplatform.wallet.v1.ui.customviews.MaterialProgressDialog;
import com.wavesplatform.wallet.v1.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.v1.ui.dex.details.DexDetailsActivity;
import com.wavesplatform.wallet.v1.util.annotations.Thunk;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.wavesplatform.wallet.v1.data.Keys.KEY_PAIR_MODEL;

public class ChartFragment extends Fragment implements ChartViewModel.DataListener,
        OnCandleGestureListener, PopupMenu.OnMenuItemClickListener {

    @Thunk
    FragmentChartBinding binding;
    @Thunk
    ChartViewModel viewModel;
    CustomCandleChart candleChart;
    CustomBarChart barChart;
    private MaterialProgressDialog materialProgressDialog;
    public Menu menu;
    private boolean isLoading = false;


    public static ChartFragment newInstance(WatchMarket pairModel) {
        Bundle args = new Bundle();
        args.putParcelable(KEY_PAIR_MODEL, pairModel);
        ChartFragment fragment = new ChartFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chart, container, false);
        viewModel = new ChartViewModel(getActivity(), this);

        initArgs(getArguments());
        setUpChart();
        viewModel.onViewReady();

        return binding.getRoot();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (menu != null) {
            changeTimeframe(item);
            viewModel.loadCandles(new Date().getTime(), true);
            viewModel.getTradesByPair();
            menu.findItem(R.id.action_timeframe).setTitle(item.getTitle());
        }
        return false;
    }

    private void changeTimeframe(MenuItem item) {
        String timeFrame = item.getTitle().toString();
        if (timeFrame.equals("M5")) setCurrentTimeFrame(5);
        if (timeFrame.equals("M15")) setCurrentTimeFrame(15);
        if (timeFrame.equals("M30")) setCurrentTimeFrame(30);
        if (timeFrame.equals("1H")) setCurrentTimeFrame(60);
        if (timeFrame.equals("4H")) setCurrentTimeFrame(240);
        if (timeFrame.equals("1D")) setCurrentTimeFrame(1440);
    }

    public void setCurrentTimeFrame(int currentTimeFrame){
        ((DexDetailsActivity) getActivity()).viewModel.dexDetailsModel.getWatchMarket().market.currentTimeFrame = currentTimeFrame;
        viewModel.setCurrentTimeFrame(currentTimeFrame);
    }

    private void initArgs(Bundle arguments) {
        if (arguments == null) return;
        if (arguments.containsKey(KEY_PAIR_MODEL))
            viewModel.chartModel.setPairModel(arguments.getParcelable(KEY_PAIR_MODEL));
    }

    private void setUpChart() {
        candleChart = binding.candleChart;

        candleChart.setOnTouchListener(
                new CandleTouchListener(candleChart, candleChart.getViewPortHandler().getMatrixTouch(), 3f)
        );

        YAxis rightAxis = binding.candleChart.getAxisRight();
        rightAxis.setDrawGridLines(true);
        rightAxis.setTextColor(Color.parseColor("#808080"));
        rightAxis.setTextSize(8f);
        rightAxis.setDrawAxisLine(false);
        rightAxis.setLabelCount(10, true);
        rightAxis.setMaxWidth(50);
        rightAxis.setMinWidth(50);
//        rightAxis.setAxisMinimum(0f);
//        rightAxis.setDrawZeroLine(true);

        YAxis leftAxis = binding.candleChart.getAxisLeft();
        leftAxis.setEnabled(false);
        /*leftAxis.setDrawGridLines(false);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawLabels(false);*/
//        leftAxis.setAxisMinimum(0f);
//        leftAxis.setDrawZeroLine(true);

        XAxis xAxis = binding.candleChart.getXAxis();
        xAxis.setTextSize(11);
        xAxis.setGranularityEnabled(true);
        //xAxis.setMultiLine(true);
        xAxis.setLabelCount(3);

        xAxis.setTextColor(Color.parseColor("#808080"));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularityEnabled(true);
        xAxis.setValueFormatter(viewModel.valueFormatter);

        binding.candleChart.setView(((DexDetailsActivity)getActivity()).binding.viewpager);
        binding.candleChart.setScaleXEnabled(true);
        binding.candleChart.setScaleYEnabled(false);
        binding.candleChart.setAutoScaleMinMaxEnabled(true);
        binding.candleChart.setVisibleXRange(10.0f, 100.0f);
        binding.candleChart.setDoubleTapToZoomEnabled(false);
        binding.candleChart.setPinchZoom(false);

        viewModel.chartModel.getData().setData(new CandleData());

        binding.candleChart.getXAxis().setValueFormatter(viewModel.valueFormatter);
        binding.candleChart.setOnChartGestureListener(this);
        binding.candleChart.getDescription().setEnabled(false);
        binding.candleChart.setDrawGridBackground(false);
        binding.candleChart.getLegend().setEnabled(false);
        binding.candleChart.setBackgroundColor(Color.parseColor("#fafafa"));
        binding.candleChart.setExtraBottomOffset(25f);
        binding.candleChart.setExtraLeftOffset(15f);

        barChart = binding.barChart;
        barChart.setView(((DexDetailsActivity)getActivity()).binding.viewpager);
        barChart.setOnChartGestureListener(new BarChartListener());
        barChart.setPinchZoom(false);
        barChart.setScaleXEnabled(false);
        barChart.setScaleYEnabled(false);
        barChart.setScaleEnabled(false);
        barChart.setDragEnabled(false);
        barChart.setVisibleXRange(10.0f, 100.0f);
        barChart.setAutoScaleMinMaxEnabled(true);
        //barChart.setMinOffset(0.f);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setExtraLeftOffset(15f);
        barChart.setDoubleTapToZoomEnabled(false);

        barChart.getAxisLeft().setEnabled(false);

        barChart.getAxisRight().setMaxWidth(50);
        barChart.getAxisRight().setMinWidth(50);
        barChart.getAxisRight().setLabelCount(4);
        barChart.getAxisRight().setDrawGridLines(true);
        barChart.getAxisRight().setTextColor(Color.parseColor("#808080"));
        barChart.getAxisRight().setTextSize(8f);
        barChart.getAxisRight().setDrawAxisLine(false);
        barChart.getAxisRight().setAxisMinimum(0);
        barChart.getAxisRight().setLabelCount(4);

        //barChart.getXAxis().setEnabled(false);
        //barChart.getXAxis().setDrawGridLines(true);
        //barChart.getXAxis().setDrawAxisLine(true);
        barChart.getXAxis().setDrawLabels(false);
        barChart.getXAxis().setDrawAxisLine(false);
        barChart.getXAxis().setGranularityEnabled(true);
        barChart.getXAxis().setLabelCount(3);

    }

    @Override
    public void onShowToast(@StringRes int message, @ToastCustom.ToastType String toastType) {
        getActivity().runOnUiThread(() -> ToastCustom.makeText(getActivity(), getString(message), ToastCustom.LENGTH_SHORT, toastType));
    }

    @Override
    public void onShowCandlesSuccess(List<CandleEntry> candles, List<BarEntry> barEntries, boolean firstRequest) {

        if (firstRequest) {
            BarData barData = new BarData();
            BarDataSet set1 = new BarDataSet(barEntries, "Bar 1");
            set1.setDrawValues(false);
            set1.setHighlightEnabled(false);
            set1.setColor(Color.parseColor("#cccccc"));//
            set1.setAxisDependency(YAxis.AxisDependency.RIGHT);
            barData.addDataSet(set1);

            CandleData candleData = new CandleData();
            CandleDataSet set = new CandleDataSet(candles, "Candle DataSet");
            set.setDecreasingColor(Color.parseColor("#E66C69"));
            set.setIncreasingColor(Color.parseColor("#5EAC69"));
            set.setNeutralColor(Color.parseColor("#4b7190"));
            set.setShadowColorSameAsCandle(true);
            set.setIncreasingPaintStyle(Paint.Style.FILL);
            set.setShadowColor(Color.DKGRAY);
            set.setHighlightEnabled(false);
            set.setDrawValues(false);
            set.setAxisDependency(YAxis.AxisDependency.RIGHT);
            candleData.addDataSet(set);

            CandleEntry last = candles.get(candles.size() - 1);
            binding.candleChart.setData(candleData);

            binding.barChart.setData(barData);

            barChart.setVisibleXRangeMinimum(5);
            barChart.setVisibleXRangeMaximum(100);

            candleChart.setVisibleXRangeMinimum(5);
            candleChart.setVisibleXRangeMaximum(100);

            binding.candleChart.moveViewToX(set.getEntryForIndex(set.getEntryCount() - 1).getX());
            binding.barChart.moveViewToX(set.getEntryForIndex(set.getEntryCount() - 1).getX());

            barChart.zoom(4f/barChart.getScaleX(), 1f, last.getX(), last.getY(), YAxis.AxisDependency.RIGHT);
            candleChart.zoom(4f/candleChart.getScaleX(), 1f, last.getX(), last.getY(), YAxis.AxisDependency.RIGHT);

            barChart.invalidate();
            candleChart.invalidate();

        } else {
            new Handler().postDelayed(() -> updateCandles(candles, barEntries), 100);
        }

    }


    @Override
    public void onRefreshCandles(List<CandleEntry> candles, List<BarEntry> barEntries) {
        if (candles.isEmpty() || barEntries.isEmpty())
            return;

        BarDataSet baseDataSet = (BarDataSet) binding.barChart.getBarData().getDataSetByIndex(0);

        for (BarEntry barEntry : barEntries) {
            List<BarEntry> entries = baseDataSet.getEntriesForXValue(barEntry.getX());
            if (entries.isEmpty()) {
                baseDataSet.addEntry(barEntry);
            } else {
                BarEntry bar = entries.get(0);
                bar.setY(barEntry.getY());
            }
        }
        baseDataSet.notifyDataSetChanged();
        BarData barData = new BarData();
        barData.addDataSet(baseDataSet);
        barChart.setData(barData);
        //barChart.moveViewToX(baseDataSet.getEntryForIndex(baseDataSet.getEntryCount() - 1).getX());
        barChart.notifyDataSetChanged();

        CandleDataSet candleDataSet = (CandleDataSet) candleChart.getCandleData().getDataSetByIndex(0);
        for (CandleEntry candleEntry : candles) {
            List<CandleEntry> entries = candleDataSet.getEntriesForXValue(candleEntry.getX());
            if (entries.isEmpty()) {
                candleDataSet.addEntry(candleEntry);
            } else {
                CandleEntry c = entries.get(0);
                c.setHigh(candleEntry.getHigh());
                c.setOpen(candleEntry.getOpen());
                c.setLow(candleEntry.getLow());
                c.setClose(candleEntry.getClose());
            }
        }
        CandleData candleData = new CandleData();
        candleData.addDataSet(candleDataSet);
        candleChart.setData(candleData);
        //candleChart.moveViewToX(candleDataSet.getEntryForIndex(candleDataSet.getEntryCount() - 1).getX());
        candleChart.notifyDataSetChanged();
    }

    private void updateCandles(List<CandleEntry> candles, List<BarEntry> barEntries) {
        BarData barData = new BarData();
        BarDataSet baseDataSet = (BarDataSet) binding.barChart.getBarData().getDataSetByIndex(0);

        for (BarEntry barEntry : barEntries) {
            baseDataSet.addEntry(barEntry);
        }
        Collections.sort(baseDataSet.getValues(), new EntryXComparator());
        barData.addDataSet(baseDataSet);

        CandleDataSet candleDataSet = (CandleDataSet) binding.candleChart.getCandleData().getDataSetByIndex(0);
        int prevCnt = candleDataSet.getEntryCount();
        final CandleEntry lastPoint = candleDataSet.getEntryForIndex(0);
        for (CandleEntry candleEntry : candles) {
            candleDataSet.addEntry(candleEntry);
        }
        Collections.sort(candleDataSet.getValues(), new EntryXComparator());
        CandleData candleData = new CandleData();
        candleData.addDataSet(candleDataSet);

        binding.candleChart.setData(candleData);
        binding.barChart.setData(barData);

        binding.candleChart.moveViewToX(lastPoint.getX());
        binding.barChart.moveViewToX(lastPoint.getX());

        final float zoom = prevCnt > 0 ? (float) candleDataSet.getEntryCount() / prevCnt : 0.0f;

        binding.candleChart.zoomToCenter(zoom, 0.0f);
        binding.candleChart.setVisibleXRangeMinimum(5);
        binding.candleChart.setVisibleXRangeMaximum(100);
        binding.candleChart.notifyDataSetChanged();

        binding.barChart.zoomToCenter(zoom, 0.0f);
        binding.barChart.setVisibleXRangeMinimum(5);
        binding.barChart.setVisibleXRangeMaximum(100);
        binding.barChart.notifyDataSetChanged();

        isLoading = false;
    }

    @Override
    public void successGetTrades(List<TradesMarket> tradesMarket) {
        if (tradesMarket.size() < 1) return;

        LimitLine limitLine = new LimitLine(Float.valueOf(tradesMarket.get(0).price), "");
        limitLine.setLineColor(Color.parseColor("#F6AD12"));
        limitLine.setLineWidth(1f);
        limitLine.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        binding.candleChart.getAxisRight().removeAllLimitLines();
        binding.candleChart.getAxisRight().addLimitLine(limitLine);
        binding.candleChart.getAxisRight().setDrawLimitLinesBehindData(false);
        binding.candleChart.invalidate();
    }

    @Override
    public void showProgressDialog(@StringRes int messageId, @Nullable String suffix) {
        dismissProgressDialog();
        materialProgressDialog = new MaterialProgressDialog(getActivity());
        materialProgressDialog.setCancelable(false);
        if (suffix != null) {
            materialProgressDialog.setMessage(getString(messageId) + suffix);
        } else {
            materialProgressDialog.setMessage(getString(messageId));
        }

        if (!getActivity().isFinishing()) materialProgressDialog.show();
    }

    @Override
    public void dismissProgressDialog() {
        if (materialProgressDialog != null && materialProgressDialog.isShowing()) {
            materialProgressDialog.dismiss();
            materialProgressDialog = null;
        }
    }


    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        prevScaleX = 0;
        checkInterval();
    }

    @Override
    public void onChartUp(MotionEvent me) {
        if (!isLoading && !candleChart.isDragEnabled()) {
            binding.candleChart.setScaleXEnabled(true);
            candleChart.setDragEnabled(true);
        }
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        //checkInterval();
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

    }
    private float prevScaleX = 0;

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        float xPos = (me.getX(0) + me.getX(1))/2f;
        float yPos = (me.getY(0) + me.getY(1))/2f;
        float z1 = prevScaleX > 0 ? scaleX/prevScaleX : scaleX;
        barChart.zoom(z1, 1.f, xPos, yPos);
        prevScaleX = scaleX;

        //barChart.zoom(scaleX, scaleY, xPos, yPos, YAxis.AxisDependency.RIGHT);
        //barChart.moveViewToX(candleChart.getLowestVisibleX());
        //System.out.println("p.x" +p.x + " X: "+ scaleX + "z1: " + z1 + " C: " + candleChart.getScaleX() + " B: " + barChart.getScaleX());
    }


    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        checkInterval();
        binding.barChart.moveViewToX(binding.candleChart.getLowestVisibleX());
    }

    private void checkInterval() {
        ICandleDataSet dataSetByIndex = binding.candleChart.getCandleData().getDataSetByIndex(0);
        float start = Math.round(dataSetByIndex.getEntryForIndex(0).getX());
        float lastOffsetX = Math.round(binding.candleChart.getLowestVisibleX());
        if (start == lastOffsetX && !isLoading) {
            isLoading = true;

            binding.candleChart.cancelPendingInputEvents();
            ((BarLineChartTouchListener) binding.candleChart.getOnTouchListener()).stopDeceleration();
            binding.candleChart.disableScroll();
            binding.candleChart.setScaleXEnabled(false);
            binding.candleChart.setScaleYEnabled(false);
            binding.candleChart.setDragEnabled(false);

            binding.barChart.cancelPendingInputEvents();
            ((BarLineChartTouchListener) binding.barChart.getOnTouchListener()).stopDeceleration();
            binding.barChart.disableScroll();
            binding.barChart.setScaleXEnabled(false);
            binding.barChart.setScaleYEnabled(false);
            binding.barChart.setDragEnabled(false);

            Date lastDate = new Date(((long) lastOffsetX) * 1000 * 60 * viewModel.currentTimeFrame);
            viewModel.loadCandles(lastDate.getTime(), false);
            viewModel.getTradesByPair();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        viewModel.pause();
    }

    @Override
    public void onResume(){
        super.onResume();
        viewModel.resume();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.destroy();
    }

    public class BarChartListener implements OnChartGestureListener {

        private float prevScaleX = 0;

        @Override
        public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

        }

        @Override
        public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
            prevScaleX = 0;
        }

        @Override
        public void onChartLongPressed(MotionEvent me) {

        }

        @Override
        public void onChartDoubleTapped(MotionEvent me) {

        }

        @Override
        public void onChartSingleTapped(MotionEvent me) {

        }

        @Override
        public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

        }

        @Override
        public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
            float xPos = (me.getX(0) + me.getX(1))/2f;
            float yPos = (me.getY(0) + me.getY(1))/2f;
            float z1 = prevScaleX > 0 ? scaleX/prevScaleX : scaleX;
            candleChart.zoom(z1, 1.f, xPos, yPos);
            prevScaleX = scaleX;
        }

        @Override
        public void onChartTranslate(MotionEvent me, float dX, float dY) {
            //binding.candleChart.moveViewToX(binding.barChart.getLowestVisibleX());
            //checkInterval();
        }
    }
}
