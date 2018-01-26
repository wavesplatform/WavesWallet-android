package com.wavesplatform.wallet.ui.transactions;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.databinding.ActivityUnknownDetailsBinding;
import com.wavesplatform.wallet.ui.base.BaseAuthActivity;

public class UnknownDetailActivity extends BaseAuthActivity implements UnknownDetailViewModel.DataListener {

    ActivityUnknownDetailsBinding mBinding;
    private UnknownDetailViewModel mViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_unknown_details);
        mViewModel = new UnknownDetailViewModel(this, this);
        mBinding.setViewModel(mViewModel);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_general);
        setSupportActionBar(toolbar);

        mViewModel.onViewReady();

        getSupportActionBar().setTitle(getString(R.string.unknown_detail_title));
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
