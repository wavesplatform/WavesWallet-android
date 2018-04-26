package com.wavesplatform.wallet.ui.dex.details.order;


import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.appsflyer.AppsFlyerLib;
import com.google.gson.internal.LinkedTreeMap;
import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.databinding.ActivityPlaceOrderBinding;
import com.wavesplatform.wallet.payload.AmountAssetInfo;
import com.wavesplatform.wallet.payload.Price;
import com.wavesplatform.wallet.payload.PriceAssetInfo;
import com.wavesplatform.wallet.payload.WatchMarket;
import com.wavesplatform.wallet.request.OrderRequest;
import com.wavesplatform.wallet.request.OrderType;
import com.wavesplatform.wallet.ui.auth.PinEntryActivity;
import com.wavesplatform.wallet.ui.base.BaseAuthActivity;
import com.wavesplatform.wallet.ui.customviews.MaterialProgressDialog;
import com.wavesplatform.wallet.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.util.MoneyUtil;
import com.wavesplatform.wallet.util.PrefsUtil;
import com.wavesplatform.wallet.util.annotations.Thunk;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.wavesplatform.wallet.data.Keys.KEY_PAIR_MODEL;
import static com.wavesplatform.wallet.ui.auth.PinEntryFragment.KEY_VALIDATED_PASSWORD;
import static com.wavesplatform.wallet.ui.auth.PinEntryFragment.KEY_VALIDATING_PIN_FOR_RESULT;
import static com.wavesplatform.wallet.ui.auth.PinEntryFragment.REQUEST_CODE_VALIDATE_PIN;

public class PlaceOrderActivity extends BaseAuthActivity implements PlaceOrderViewModel.DataListener, TextWatcher,
        View.OnTouchListener, View.OnClickListener {

    public static final long CHANGE_RANG_TIME = 3000L;
    private static final int MSG_INC = 0;
    private static final int MSG_DEC = 1;

    private ActivityPlaceOrderBinding binding;
    @Thunk
    PlaceOrderViewModel placeOrderViewModel;
    private MaterialProgressDialog materialProgressDialog;
    private Price price;
    private int rangOffset = 0;

    private ScheduledExecutorService mUpdater;
    private EditText currentView;

    private Handler mUpdateHandler;
    private Handler mIncrementRangHandler = new Handler();


    public static Intent newInstance(Context context, WatchMarket watchMarket, Price price) {
        Intent intent = new Intent(context, PlaceOrderActivity.class);
        intent.putExtra(KEY_PAIR_MODEL, watchMarket);
        intent.putExtra("price", price);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_place_order);
        placeOrderViewModel = new PlaceOrderViewModel(getIntent().getParcelableExtra(KEY_PAIR_MODEL), this);
        initUI();

        price = getIntent().getParcelableExtra("price");
        if (price != null) {
            long priceValue = price.asks != null ? price.asks.price : price.bids.price;
            long amountValue = price.asks != null ? Long.valueOf(price.asks.amount) : Long.valueOf(price.bids.amount);

            binding.editPriceValue.setText(MoneyUtil.getTextStripZeros(priceValue, placeOrderViewModel.placeOrderModel.getPriceValueDecimals()).replaceAll(",", ""));
            binding.editAmount.setText(MoneyUtil.getTextStripZeros(amountValue, placeOrderViewModel.placeOrderModel.getAmountDecimals()).replaceAll(",", ""));
        }

        placeOrderViewModel.onViewReady();
    }

    private void initUI() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_general);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.dex_bg_toolbar));
        toolbar.setTitle(getString(R.string.place_order_toolbar_title) + " " + placeOrderViewModel.placeOrderModel.getWatchMarket().market.amountAssetName + "/" +
                placeOrderViewModel.placeOrderModel.getWatchMarket().market.priceAssetName);
        setSupportActionBar(toolbar);

        binding.inputAmount.setHint(getString(R.string.place_order_amount) + " in " + placeOrderViewModel.placeOrderModel.getWatchMarket().market.amountAssetName);
        binding.inputPrice.setHint(getString(R.string.place_order_price) + " in " + placeOrderViewModel.placeOrderModel.getWatchMarket().market.priceAssetName);

        binding.textPriceAsset.setText(placeOrderViewModel.placeOrderModel.getWatchMarket().market.priceAssetName);
        binding.totalAssetName.setText(placeOrderViewModel.placeOrderModel.getWatchMarket().market.priceAssetName);
        binding.textAmountAsset.setText(placeOrderViewModel.placeOrderModel.getWatchMarket().market.amountAssetName);

        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_INC:
                        doOperation(true);
                        return;
                    case MSG_DEC:
                        doOperation(false);
                        return;
                }
                super.handleMessage(msg);
            }
        };

        binding.editAmount.addTextChangedListener(this);
        binding.editPriceValue.addTextChangedListener(this);

        binding.imagePriceIncrement.setOnTouchListener(this);
        binding.imagePriceIncrement.setOnClickListener(this);

        binding.imagePriceDecrement.setOnTouchListener(this);
        binding.imagePriceDecrement.setOnClickListener(this);

        binding.imageAmountDecrement.setOnTouchListener(this);
        binding.imageAmountDecrement.setOnClickListener(this);

        binding.imageAmountIncrement.setOnTouchListener(this);
        binding.imageAmountIncrement.setOnClickListener(this);

        binding.textAmountAssetValue.setOnClickListener(v -> binding.editAmount.setText(binding.textAmountAssetValue.getText().toString().replaceAll(",", "")));
        binding.textPriceAssetValue.setOnClickListener(v -> {
            if (binding.editPriceValue.getText().toString().trim().isEmpty()) return;
            Double d = Double.valueOf(binding.textPriceAssetValue.getText().toString().replaceAll(",", "")) /
                    Double.valueOf(binding.editPriceValue.getText().toString());
            if (Double.isNaN(d) || Double.isInfinite(d)) binding.editAmount.setText("0.0");
            else binding.editAmount.setText(MoneyUtil.getTextStripZeros(String.valueOf(d)));
        });

        binding.buttonBuy.setOnClickListener(v -> {
            placeOrderViewModel.getOrderRequest().orderType = OrderType.buy;
            checkSendWithoutAskOrShowAlert();
        });
        binding.buttonSell.setOnClickListener(v -> {
            placeOrderViewModel.getOrderRequest().orderType = OrderType.sell;
            checkSendWithoutAskOrShowAlert();
        });
    }

    private class UpdateCounterTask implements Runnable {
        private boolean mInc;

        public UpdateCounterTask(boolean inc) {
            mInc = inc;
        }

        public void run() {
            if (mInc) {
                mUpdateHandler.sendEmptyMessage(MSG_INC);
            } else {
                mUpdateHandler.sendEmptyMessage(MSG_DEC);
            }
        }
    }

    private void doOperation(boolean increment) {
        String val = currentView.getText().toString();

        if (val.trim().isEmpty()) return;

        int rang;
        int doteIndex = val.indexOf(".");
        if (val.contains(".")) rang = val.length() - 1;
        else rang = val.length();

        rang = rang - rangOffset;

        StringBuilder incrementValueWithRang = new StringBuilder();
        for (int i = 0; i < rang; i++) {
            if (i == doteIndex)
                incrementValueWithRang.append(".");
            else
                incrementValueWithRang.append("0");
        }

        incrementValueWithRang.append("1");

        BigDecimal t = new BigDecimal(val);

        if (increment)
            currentView.setText(String.valueOf(t.add(new BigDecimal(incrementValueWithRang.toString()))));
        else
            currentView.setText(String.valueOf(t.subtract(new BigDecimal(incrementValueWithRang.toString()))));
    }

    private void startUpdating(boolean inc) {
        mIncrementRangHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                rangOffset++;
                mIncrementRangHandler.postDelayed(this, CHANGE_RANG_TIME);
            }
        }, CHANGE_RANG_TIME);
        if (mUpdater != null) {
            Log.e(getClass().getSimpleName(), "Another executor is still active");
            return;
        }
        mUpdater = Executors.newSingleThreadScheduledExecutor();
        mUpdater.scheduleAtFixedRate(new UpdateCounterTask(inc), 200, 200,
                TimeUnit.MILLISECONDS);
    }

    private void stopUpdating() {
        rangOffset = 0;
        mIncrementRangHandler.removeCallbacksAndMessages(null);
        mUpdater.shutdownNow();
        mUpdater = null;
    }

    public void onClick(View v) {
        setCurrentOperationEditText(v);
        if (mUpdater == null) {
            if (v == binding.imagePriceIncrement || v == binding.imageAmountIncrement) {
                doOperation(true);
            } else {
                doOperation(false);
            }
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        setCurrentOperationEditText(v);
        boolean isReleased = event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL;
        boolean isPressed = event.getAction() == MotionEvent.ACTION_DOWN;

        if (isReleased) {
            stopUpdating();
        } else if (isPressed) {
            startUpdating(v == binding.imagePriceIncrement || v == binding.imageAmountIncrement);
        }
        return false;
    }

    private void checkSendWithoutAskOrShowAlert() {
        if (placeOrderViewModel.getPrefsUtil().getValue(PrefsUtil.KEY_DONT_ASK_AGAIN_ORDER, false)) {
            sendRequest();
        } else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.PlaceOrderDialogStyle);
            alertDialogBuilder.setTitle(R.string.dex_place_order_dialog_title)
                    .setView(getView())
                    .setPositiveButton(R.string.dex_place_order_dialog_confirm, (dialog, which) -> {
                        placeOrderViewModel.getPrefsUtil().setValue(PrefsUtil.KEY_DONT_ASK_AGAIN_ORDER, placeOrderViewModel.placeOrderModel.isDontAskAgain());
                        sendRequest();
                    })
                    .setNegativeButton(R.string.dex_place_order_dialog_cancel, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        }
    }

    public void setCurrentOperationEditText(View view) {
        switch (view.getId()) {
            case R.id.image_amount_increment:
                currentView = binding.editAmount;
                break;
            case R.id.image_amount_decrement:
                currentView = binding.editAmount;
                break;
            case R.id.image_price_increment:
                currentView = binding.editPriceValue;
                break;
            case R.id.image_price_decrement:
                currentView = binding.editPriceValue;
                break;
        }
    }

    private View getView() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_dont_ask_again, null);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            placeOrderViewModel.placeOrderModel.setDontAskAgain(isChecked);
        });
        return view;
    }


    private void sendRequest() {
        if (placeOrderViewModel.validateFields(binding.editAmount.getText().toString(), binding.editPriceValue.getText().toString())) {
            if (placeOrderViewModel.signTransaction(binding.editAmount.getText().toString(), binding.editPriceValue.getText().toString())) {
                placeOrderViewModel.placeOrder();
            } else {
                requestPinDialog();
            }
        }
    }

    private void requestPinDialog() {
        Intent intent = new Intent(this, PinEntryActivity.class);
        intent.putExtra(KEY_VALIDATING_PIN_FOR_RESULT, true);
        startActivityForResult(intent, REQUEST_CODE_VALIDATE_PIN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_VALIDATE_PIN && resultCode == RESULT_OK && data != null
                && data.getStringExtra(KEY_VALIDATED_PASSWORD) != null) {
            if (placeOrderViewModel.validateFields(binding.editAmount.getText().toString(), binding.editPriceValue.getText().toString())) {
                if (placeOrderViewModel.signTransaction(binding.editAmount.getText().toString(), binding.editPriceValue.getText().toString())) {
                    placeOrderViewModel.placeOrder();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    @Override
    public void showProgressDialog(@StringRes int messageId, @Nullable String suffix) {
        dismissProgressDialog();
        materialProgressDialog = new MaterialProgressDialog(this);
        materialProgressDialog.setCancelable(false);
        if (suffix != null) {
            materialProgressDialog.setMessage(getString(messageId) + suffix);
        } else {
            materialProgressDialog.setMessage(getString(messageId));
        }

        if (!this.isFinishing()) materialProgressDialog.show();
    }

    @Override
    public void dismissProgressDialog() {
        if (materialProgressDialog != null && materialProgressDialog.isShowing()) {
            materialProgressDialog.dismiss();
            materialProgressDialog = null;
        }
    }

    @Override
    public void onShowToast(@StringRes int message, @ToastCustom.ToastType String toastType) {
        ToastCustom.makeText(this, getString(message), ToastCustom.LENGTH_SHORT, toastType);
    }

    @Override
    public void onShowToast(String message, @ToastCustom.ToastType String toastType) {
        ToastCustom.makeText(this, message, ToastCustom.LENGTH_SHORT, toastType);
    }

    @Override
    public void trackPlaceOrder(OrderRequest order) {
        Map<String, Object> eventValue = new HashMap<String, Object>();
        eventValue.put("af_order_pair", order.assetPair.getKey());
        eventValue.put("af_order_type", order.orderType.toString());
        eventValue.put("af_order_price", order.price);
        eventValue.put("af_order_amount", order.amount);
        AppsFlyerLib.getInstance().trackEvent(getApplicationContext(), "af_place_order", eventValue);
    }

    @Override
    public void showBalanceFromPair(LinkedTreeMap<String, Long> balances) {
        AmountAssetInfo amountAssetInfo = placeOrderViewModel.placeOrderModel.getWatchMarket().market.getAmountAssetInfo();
        String amountAsset = placeOrderViewModel.placeOrderModel.getWatchMarket().market.amountAsset;
        PriceAssetInfo priceAssetInfo = placeOrderViewModel.placeOrderModel.getWatchMarket().market.getPriceAssetInfo();
        String priceAsset = placeOrderViewModel.placeOrderModel.getWatchMarket().market.priceAsset;
        binding.textAmountAssetValue.setText(MoneyUtil.getTextStripZeros(MoneyUtil.getScaledText(balances.get(amountAsset), amountAssetInfo.decimals)));
        binding.textPriceAssetValue.setText(MoneyUtil.getTextStripZeros(MoneyUtil.getScaledText(balances.get(priceAsset), priceAssetInfo.decimals)));
    }

    @Override
    public void afterSuccessfullyPlaceOrder() {
        finish();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (binding.editAmount.getText().toString().isEmpty() || binding.editPriceValue.getText().toString().isEmpty()) {
            binding.textTotalValue.setText("0.0");
        } else {
            if (binding.editAmount.getText().toString().startsWith(".") || binding.editPriceValue.getText().toString().startsWith("."))
                binding.textTotalValue.setText("0.0");
            else
                binding.textTotalValue.setText(MoneyUtil.getFormattedTotal(Double.valueOf(binding.editAmount.getText().toString()) * Double.valueOf(binding.editPriceValue.getText().toString()), placeOrderViewModel.placeOrderModel.getWatchMarket().market.getPriceAssetInfo().decimals));
        }

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}