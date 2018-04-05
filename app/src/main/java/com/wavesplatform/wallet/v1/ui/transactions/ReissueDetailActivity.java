
package com.wavesplatform.wallet.v1.ui.transactions;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.databinding.ActivityReissueDetailsBinding;
import com.wavesplatform.wallet.v1.request.ReissueTransactionRequest;
import com.wavesplatform.wallet.v1.ui.base.BaseAuthActivity;

public class ReissueDetailActivity extends BaseAuthActivity implements ReissueDetailViewModel.DataListener {

    public static String KEY_INTENT_ACTIONS_ENABLED = "intent_actions_enabled";
    private final String TAG = getClass().getSimpleName();

    ActivityReissueDetailsBinding mBinding;
    private ReissueDetailViewModel mViewModel;

    ReissueTransactionRequest reissueRequest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_reissue_details);
        mViewModel = new ReissueDetailViewModel(this, this);
        mBinding.setViewModel(mViewModel);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_general);
        toolbar.setTitle(getResources().getString(R.string.reissue_detail_title));
        setSupportActionBar(toolbar);

        mViewModel.onViewReady();

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
