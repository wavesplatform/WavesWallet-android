package com.wavesplatform.wallet.ui.transactions;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.databinding.ActivityTransferTransactionDetailsBinding;
import com.wavesplatform.wallet.ui.base.BaseAuthActivity;
import com.wavesplatform.wallet.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.util.annotations.Thunk;

public class TransactionDetailActivity extends BaseAuthActivity implements TransactionDetailViewModel.DataListener {

    public static final String KEY_TRANSACTION_URL = "key_transaction_url";
    @Thunk
    ActivityTransferTransactionDetailsBinding mBinding;
    private TransactionDetailViewModel mViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_transfer_transaction_details);
        mViewModel = new TransactionDetailViewModel(this, this);
        mBinding.setViewModel(mViewModel);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_general);
        setSupportActionBar(toolbar);

        mViewModel.onViewReady();

        getSupportActionBar().setTitle(getString(mViewModel.isPaymentTransaction() ? R.string.payment_detail_title : R.string.transaction_detail_title));

        setupViews();
    }

    private void setupViews() {
        mBinding.fromAddress.setOnClickListener(v -> copyToClipboard(mBinding.fromAddress.getText().toString()));
        mBinding.toAddress.setOnClickListener(v -> copyToClipboard(mBinding.toAddress.getText().toString()));
    }

    public void copyToClipboard(String address) {
        ClipboardManager clipboard = (android.content.ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = android.content.ClipData.newPlainText("Send address", address);
        ToastCustom.makeText(this, getString(R.string.copied_to_clipboard), ToastCustom.LENGTH_LONG, ToastCustom.TYPE_GENERAL);
        clipboard.setPrimaryClip(clip);
    }

    @Override
    public void pageFinish() {
        finish();
    }

    @Override
    public Intent getPageIntent() {
        return getIntent();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewModel.destroy();
    }
}
