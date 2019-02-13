package com.wavesplatform.wallet.v2.util;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.wavesplatform.wallet.R;

import java.security.Security;

import javax.inject.Inject;

public class AppUtil {


    private Context context;

    @Inject
    public AppUtil(@com.wavesplatform.wallet.v2.injection.qualifier.ApplicationContext Context context) {
        this.context = context;
    }


    public void restartApp() {
        Intent intent = new Intent(context, com.wavesplatform.wallet.v2.ui.splash.SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}