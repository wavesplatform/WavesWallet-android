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


    public void applyPRNGFixes() {
        try {
            PRNGFixes.apply();
        } catch (Exception e0) {
            // todo is it need?
            // some Android 4.0 devices throw an exception when PRNGFixes is re-applied
            // removing provider before apply() is a workaround
            //
            Security.removeProvider("LinuxPRNG");
            try {
                PRNGFixes.apply();
            } catch (Exception e1) {
                Toast.makeText(context, R.string.cannot_launch_app, Toast.LENGTH_LONG).show();
            }
        }
    }
}