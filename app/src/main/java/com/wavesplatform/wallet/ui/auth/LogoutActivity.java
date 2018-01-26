package com.wavesplatform.wallet.ui.auth;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.wavesplatform.wallet.data.access.AccessState;
import com.wavesplatform.wallet.util.AndroidUtils;

public class LogoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null && getIntent().getAction() != null) {
            if (AndroidUtils.is16orHigher()) {
                finishAffinity();
            } else {
                finish();
                // Shouldn't call System.exit(0) if it can be avoided
                System.exit(0);
            }
        }
    }
}
