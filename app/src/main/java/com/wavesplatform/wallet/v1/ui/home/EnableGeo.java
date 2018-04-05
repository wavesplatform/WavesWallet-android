package com.wavesplatform.wallet.v1.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

import com.wavesplatform.wallet.R;

public class EnableGeo {

    public static void displayGPSPrompt(final Activity activity) {

        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;

        new AlertDialog.Builder(activity, R.style.AlertDialogStyle)
                .setMessage(activity.getString(R.string.enable_geo))
                .setPositiveButton(
                        android.R.string.ok, (d, id) -> activity.startActivity(new Intent(action)))
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
    }
}
