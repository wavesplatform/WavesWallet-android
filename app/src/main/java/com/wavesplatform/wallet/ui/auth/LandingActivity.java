package com.wavesplatform.wallet.ui.auth;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.MotionEvent;
import android.view.View;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.data.connectivity.ConnectivityStatus;
import com.wavesplatform.wallet.databinding.ActivityLandingBinding;
import com.wavesplatform.wallet.ui.base.BaseAuthActivity;
import com.wavesplatform.wallet.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.ui.pairing.ImportOrCreateWalletActivity;
import com.wavesplatform.wallet.util.AppUtil;


public class LandingActivity extends BaseAuthActivity {

    public static final String KEY_INTENT_IMPORT_WALLET = "import_wallet";

    private ActivityLandingBinding binding;
    private LandingViewModel mViewModel;

    private int numLogoClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_landing);
        setTitle(R.string.app_name);

        mViewModel = new LandingViewModel();
        mViewModel.onViewReady();

        binding.create.setOnClickListener(view -> startCreateWalletActivity());

        if (mViewModel.isNoStoredKeys()) {
            binding.login.setVisibility(View.GONE);
        } else {
            binding.login.setOnClickListener(view -> startLoginActivity());
        }

        binding.recoverFunds.setOnClickListener(view -> startImportActivity());

        if (EnvironmentManager.get().shouldShowDebugMenu()) {
            ToastCustom.makeText(
                    this,
                    "Current environment: "
                            + EnvironmentManager.get().current().getName(),
                    ToastCustom.LENGTH_SHORT,
                    ToastCustom.TYPE_GENERAL);
        }

        binding.buttonSettings.setOnClickListener(view ->
                new EnvironmentSwitcher(this).showEnvironmentSelectionDialog());

        if (!ConnectivityStatus.hasConnectivity(this)) {
            new AlertDialog.Builder(this, R.style.AlertDialogStyle)
                    .setMessage(getString(R.string.check_connectivity_exit))
                    .setCancelable(false)
                    .setPositiveButton(R.string.dialog_continue, (d, id) -> {
                        Intent intent = new Intent(LandingActivity.this, LandingActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    })
                    .create()
                    .show();
        }

        binding.logo.setOnClickListener(v -> {
            numLogoClicked++;
            if (numLogoClicked >= 5)  {
                binding.buttonSettings.setVisibility(View.VISIBLE);
            }
        });
    }

    private void startCreateWalletActivity() {
        Intent intent = new Intent(this, ImportOrCreateWalletActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void startImportActivity() {
        Intent intent = new Intent(this, ImportOrCreateWalletActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_INTENT_IMPORT_WALLET, true);
        startActivity(intent);
    }

    public void startLoginActivity() {
        Intent intent = new Intent(this, ChooseWalletActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // Test for screen overlays before user creates a new wallet or enters confidential information
        // consume event
        return new AppUtil(this).detectObscuredWindow(this, event) || super.dispatchTouchEvent(event);
    }
}
