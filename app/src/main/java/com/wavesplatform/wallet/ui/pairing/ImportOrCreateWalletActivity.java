package com.wavesplatform.wallet.ui.pairing;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.ui.auth.CreateWalletFragment;
import com.wavesplatform.wallet.ui.auth.SeedWalletActivity;
import com.wavesplatform.wallet.ui.base.BaseAuthActivity;
import com.wavesplatform.wallet.ui.customviews.MaterialProgressDialog;
import com.wavesplatform.wallet.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.ui.zxing.CaptureActivity;
import com.wavesplatform.wallet.util.AppUtil;

import static com.wavesplatform.wallet.ui.auth.LandingActivity.KEY_INTENT_IMPORT_WALLET;

public class ImportOrCreateWalletActivity extends BaseAuthActivity implements PairingViewModel.DataListener {

    public static final int PAIRING_QR = 2005;
    private PairingViewModel viewModel;
    private MaterialProgressDialog materialProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair);

        viewModel = new PairingViewModel(this);
        viewModel.onViewReady();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_general);
        setSupportActionBar(toolbar);

        Fragment fragment;

        if (getIntent().getBooleanExtra(KEY_INTENT_IMPORT_WALLET, false)) {
            fragment = new PairWalletFragment();
        } else {
            fragment = new CreateWalletFragment();
        }

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        new AppUtil(this).restartApp();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == PAIRING_QR) {
            if (data != null && data.getStringExtra(CaptureActivity.SCAN_RESULT) != null) {
                viewModel.pairWithQR(data.getStringExtra(CaptureActivity.SCAN_RESULT));
            }
        }
    }

    @Override
    public void showToast(@StringRes int message, @ToastCustom.ToastType String toastType) {
        ToastCustom.makeText(this, getString(message), ToastCustom.LENGTH_LONG, toastType);
    }

    @Override
    public void startSeedWalletActivity(String seed) {
        Intent intent = new Intent(this, SeedWalletActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        if (seed != null) {
            intent.putExtra(SeedWalletActivity.KEY_INTENT_SEED, seed);
        }
        startActivity(intent);
    }

    @Override
    public void showProgressDialog(@StringRes int message) {
        dismissProgressDialog();
        materialProgressDialog = new MaterialProgressDialog(this);
        materialProgressDialog.setCancelable(false);
        materialProgressDialog.setMessage(getString(message));

        if (!isFinishing()) materialProgressDialog.show();
    }

    @Override
    public void dismissProgressDialog() {
        if (materialProgressDialog != null && materialProgressDialog.isShowing()) {
            materialProgressDialog.dismiss();
            materialProgressDialog = null;
        }
    }
}
