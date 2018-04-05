package com.wavesplatform.wallet.v1.ui.dex.markets.add;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.databinding.ActivityAddMarketBinding;
import com.wavesplatform.wallet.v1.payload.Market;
import com.wavesplatform.wallet.v1.ui.base.BaseAuthActivity;
import com.wavesplatform.wallet.v1.ui.customviews.MaterialProgressDialog;
import com.wavesplatform.wallet.v1.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.v1.util.annotations.Thunk;

public class AddMarketActivity extends BaseAuthActivity implements AddMarketViewModel.DataListener {

    private AddMarketViewModel mViewModel;
    private ActivityAddMarketBinding binding;
    private MaterialProgressDialog materialProgressDialog;


    public static Intent getStartIntent(Context context) {
        return new Intent(context, AddMarketActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_market);

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> {
            finish();
        });

        getSupportActionBar().setTitle(getString(R.string.dex_add_market_toolbar_title));

        mViewModel = new AddMarketViewModel(this);
        mViewModel.onViewReady();

        binding.buttonSubmit.setOnClickListener(v -> {
            if (mViewModel.validateFields(binding.editAmountAsset.getText().toString(),binding.editPriceAsset.getText().toString())){
                mViewModel.getOrderBook(binding.editAmountAsset.getText().toString(),binding.editPriceAsset.getText().toString());
            }
        });
    }

    @Thunk
    void onViewReady() {
        mViewModel.onViewReady();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewModel.destroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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
    public void onShowToast(String message, @ToastCustom.ToastType String toastType) {
        ToastCustom.makeText(this, message, ToastCustom.LENGTH_SHORT, toastType);
    }

    @Override
    public void afterSuccessfullyOrderBook(Market market) {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onShowToast(@StringRes int message, @ToastCustom.ToastType String toastType) {
        ToastCustom.makeText(this, getString(message), ToastCustom.LENGTH_SHORT, toastType);
    }
}
