package com.wavesplatform.wallet.v1.ui.auth;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.databinding.ActivityPinEntryBinding;
import com.wavesplatform.wallet.v1.ui.base.BaseAuthActivity;
import com.wavesplatform.wallet.v1.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.v1.util.AppUtil;

public class PinEntryActivity extends BaseAuthActivity {

    private static final int COOL_DOWN_MILLIS = 2 * 1000;
    private long backPressed;

    // Fragments
    private PinEntryFragment pinEntryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        ActivityPinEntryBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_pin_entry);
        pinEntryFragment = new PinEntryFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, pinEntryFragment)
                .commit();

        if (EnvironmentManager.get().shouldShowDebugMenu()) {
            ToastCustom.makeText(
                    this,
                    "Current environment: "
                            + EnvironmentManager.get().current().getName(),
                    ToastCustom.LENGTH_SHORT,
                    ToastCustom.TYPE_GENERAL);
        }
    }

    @Override
    public void onBackPressed() {
        if (pinEntryFragment.isValidatingPinForResult()) {
            finishWithResultCanceled();
        } else if (pinEntryFragment.allowExit()) {
            if (backPressed + COOL_DOWN_MILLIS > System.currentTimeMillis()) {
                return;
            } else {
                ToastCustom.makeText(this, getString(R.string.exit_confirm), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_GENERAL);
            }

            backPressed = System.currentTimeMillis();
        }
    }

    private void finishWithResultCanceled() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // Test for screen overlays before user enters PIN
        // consume event
        return new AppUtil(this).detectObscuredWindow(this, event) || super.dispatchTouchEvent(event);
    }

}