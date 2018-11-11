package com.wavesplatform.wallet.v1.util;

import android.content.Context;
import android.content.Intent;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.v1.ui.customviews.ToastCustom;

import java.io.File;
import java.security.Security;

import javax.inject.Inject;

public class AppUtil {


    private Context context;
    private String receiveQRFileName;

    @Inject
    public AppUtil(@com.wavesplatform.wallet.v2.injection.qualifier.ApplicationContext Context context) {
        this.context = context;
        this.receiveQRFileName = context.getExternalCacheDir() + File.separator + "qr.png";
    }


    public void restartApp() {
        Intent intent = new Intent(context, com.wavesplatform.wallet.v2.ui.splash.SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    public void applyPRNGFixes() {
        try {
            PRNGFixes.apply();
        } catch (Exception e0) {
            //
            // some Android 4.0 devices throw an exception when PRNGFixes is re-applied
            // removing provider before apply() is a workaround
            //
            Security.removeProvider("LinuxPRNG");
            try {
                PRNGFixes.apply();
            } catch (Exception e1) {
                ToastCustom.makeText(context, context.getString(R.string.cannot_launch_app), ToastCustom.LENGTH_LONG, ToastCustom.TYPE_ERROR);
            }
        }
    }
}
