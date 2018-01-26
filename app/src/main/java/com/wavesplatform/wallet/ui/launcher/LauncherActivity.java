package com.wavesplatform.wallet.ui.launcher;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.ui.auth.AuthUtil;
import com.wavesplatform.wallet.ui.auth.LandingActivity;
import com.wavesplatform.wallet.ui.auth.PinEntryActivity;
import com.wavesplatform.wallet.util.annotations.Thunk;

public class LauncherActivity extends AppCompatActivity implements LauncherViewModel.DataListener {

    private LauncherViewModel mViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_launcher);

        mViewModel = new LauncherViewModel(this);
        mViewModel.onViewReady();

        finish();
        //Handler handler = new Handler();
        //handler.postDelayed(new DelayStartRunnable(this), 500);
    }

    @Thunk
    void onViewReady() {
        mViewModel.onViewReady();
    }

    @Override
    public Intent getPageIntent() {
        return getIntent();
    }

    @Override
    public void onNotLoggedIn() {
        startSingleActivity(LandingActivity.class);
    }

    @Override
    public void onRequestPin() {
        startSingleActivity(PinEntryActivity.class);
    }

    @Override
    public void onCorruptPayload() {
        new AlertDialog.Builder(this, R.style.AlertDialogStyle)
                .setTitle(R.string.app_name)
                .setMessage(getString(R.string.not_sane_error))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, (dialog, whichButton) -> {
                    mViewModel.getAppUtil().clearCredentialsAndRestart();
                    mViewModel.getAppUtil().restartApp();
                })
                .show();
    }

    @Override
    public boolean onStartMainActivity(String publicKey) {
        return AuthUtil.startMainActivity(this, publicKey);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewModel.destroy();
    }

    private void startSingleActivity(Class clazz) {
        Intent intent = new Intent(this, clazz);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private static class DelayStartRunnable implements Runnable {

        private LauncherActivity activity;

        DelayStartRunnable(LauncherActivity activity) {
            this.activity = activity;
        }

        @Override
        public void run() {
            activity.onViewReady();
        }
    }

}
