package com.wavesplatform.wallet.ui.transactions;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.databinding.ActivityExchangeTransactionBinding;
import com.wavesplatform.wallet.ui.base.BaseAuthActivity;
import com.wavesplatform.wallet.util.annotations.Thunk;

public class ExchangeTransactionActivity extends BaseAuthActivity implements ExchangeTransactionDetailViewModel.DataListener {

    @Thunk
    ActivityExchangeTransactionBinding mBinding;
    private ExchangeTransactionDetailViewModel mViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_exchange_transaction);
        mViewModel = new ExchangeTransactionDetailViewModel(this, this);
        mBinding.setViewModel(mViewModel);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_general);
        setSupportActionBar(toolbar);

        mViewModel.onViewReady();

        getSupportActionBar().setTitle(getString(R.string.exchange_detail_tilte));
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
