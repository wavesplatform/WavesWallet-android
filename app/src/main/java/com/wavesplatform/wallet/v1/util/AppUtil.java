package com.wavesplatform.wallet.v1.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.v7.app.AlertDialog;
import android.view.MotionEvent;

import com.wavesplatform.wallet.App;
import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.v1.injection.Injector;
import com.wavesplatform.wallet.v1.ui.customviews.ToastCustom;

import java.io.File;
import java.security.Security;

import javax.inject.Inject;

public class AppUtil {

    private static final String REGEX_UUID = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    @Inject protected PrefsUtil prefs;
    private Context context;
    private AlertDialog alertDialog;
    private String receiveQRFileName;

    @Inject
    public AppUtil(@com.wavesplatform.wallet.v2.injection.qualifier.ApplicationContext Context context) {
        Injector.getInstance().getAppComponent().inject(this);
        this.context = context;
        this.receiveQRFileName = context.getExternalCacheDir() + File.separator + "qr.png";
    }

    public void clearCredentialsAndRestart() {
        App.getAccessManager().setLastLoggedInGuid("");
        restartApp();
    }

    public void restartApp() {
        Intent intent = new Intent(context, com.wavesplatform.wallet.v2.ui.splash.SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void restartAppWithVerifiedPin() {
        Intent intent = new Intent(context, com.wavesplatform.wallet.v2.ui.splash.SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("verified", true);
        context.startActivity(intent);
        prefs.logIn();
    }

    public String getReceiveQRFilename() {
        return receiveQRFileName;
    }

    public boolean isNewlyCreated() {
        return prefs.getValue(PrefsUtil.KEY_NEWLY_CREATED_WALLET, false);
    }

    public void setNewlyCreated(boolean newlyCreated) {
        prefs.setValue(PrefsUtil.KEY_NEWLY_CREATED_WALLET, newlyCreated);
    }

    public boolean isCameraOpen() {
        Camera camera = null;

        try {
            camera = Camera.open();
        } catch (RuntimeException e) {
            return true;
        } finally {
            if (camera != null) {
                camera.release();
            }
        }

        return false;
    }

    public String getSharedKey() {
        return prefs.getValue(PrefsUtil.KEY_SHARED_KEY, "");
    }

    public void setSharedKey(String sharedKey) {
        prefs.setValue(PrefsUtil.KEY_SHARED_KEY, sharedKey);
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

    public boolean detectObscuredWindow(Context context, MotionEvent event) {
        //Detect if touch events are being obscured by hidden overlays - These could be used for tapjacking
        if ((!prefs.getValue("OVERLAY_TRUSTED", false)) && (event.getFlags() & MotionEvent.FLAG_WINDOW_IS_OBSCURED) != 0) {

            //Prevent multiple popups
            if (alertDialog != null)
                alertDialog.dismiss();

            alertDialog = new AlertDialog.Builder(context, R.style.AlertDialogStyle)
                    .setTitle(R.string.screen_overlay_warning)
                    .setMessage(R.string.screen_overlay_note)
                    .setCancelable(false)
                    .setPositiveButton(R.string.dialog_continue, (dialog, whichButton) -> {
                        prefs.setValue("OVERLAY_TRUSTED", true);
                        dialog.dismiss();
                    }).setNegativeButton(R.string.exit, (dialog, whichButton) -> {
                        dialog.dismiss();
                        ((Activity) context).finish();
                    }).show();
            return true;//consume event
        } else {
            return false;
        }
    }

    public String getPackageName() {
        return context.getPackageName();
    }

    public PackageManager getPackageManager() {
        return context.getPackageManager();
    }
}
