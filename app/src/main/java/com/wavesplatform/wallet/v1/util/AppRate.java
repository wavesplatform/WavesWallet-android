package com.wavesplatform.wallet.v1.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.text.format.DateUtils;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.v1.ui.customviews.ToastCustom;

public class AppRate implements android.content.DialogInterface.OnClickListener, OnCancelListener {

    private static final String SHARED_PREFS_NAME = "apprate_prefs";
    private static final String PREF_DATE_REMIND_START = "date_remind_start";
    private static final String PREF_TRANSACTION_COUNT = "transaction_count";
    private static final String PREF_DONT_SHOW_AGAIN = "dont_show_again";
    private static final String PREF_DAYS_UNTIL_PROMPT = "days_until_prompt";

    private Activity hostActivity;
    private OnClickListener clickListener;
    private SharedPreferences preferences;

    private long minTransactionsUntilPrompt = 0;

    public AppRate(Activity hostActivity) {
        this.hostActivity = hostActivity;
        preferences = hostActivity.getSharedPreferences(AppRate.SHARED_PREFS_NAME, 0);
    }

    public AppRate setMinTransactionsUntilPrompt(long minTransactionsUntilPrompt) {
        this.minTransactionsUntilPrompt = minTransactionsUntilPrompt;
        return this;
    }

    public AppRate setMinDaysUntilPrompt(long minDaysUntilPrompt) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(AppRate.PREF_DAYS_UNTIL_PROMPT, minDaysUntilPrompt);
        editor.apply();
        return this;
    }

    /**
     * Reset all the data collected about number of transactions and days until first launch.
     *
     * @param context A context.
     */
    public static void reset(Context context) {
        context.getSharedPreferences(AppRate.SHARED_PREFS_NAME, 0).edit().clear().apply();
    }

    /**
     * Returns true if the Rating dialog should be shown
     */
    public boolean shouldShowDialog() {

        SharedPreferences.Editor editor = preferences.edit();
        long transactionCount = preferences.getLong(AppRate.PREF_TRANSACTION_COUNT, 0);
        long minDaysUntilPrompt = preferences.getLong(AppRate.PREF_DAYS_UNTIL_PROMPT, 0);
        boolean dontShowAgain = preferences.getBoolean(AppRate.PREF_DONT_SHOW_AGAIN, false);

        if (!dontShowAgain) {
            // Get date of 'remind me later'.
            Long dateRemindStart = preferences.getLong(AppRate.PREF_DATE_REMIND_START, 0);
            if (dateRemindStart == 0) {
                dateRemindStart = System.currentTimeMillis();
                editor.putLong(AppRate.PREF_DATE_REMIND_START, dateRemindStart);
            }

            // Show the rate dialog if needed.
            if (transactionCount >= minTransactionsUntilPrompt) {
                if (System.currentTimeMillis() >= dateRemindStart + (minDaysUntilPrompt * DateUtils.DAY_IN_MILLIS)) {
                    editor.apply();
                    return true;
                }
            }
        }

        editor.apply();
        return false;
    }

    public AppRate incrementTransactionCount() {

        SharedPreferences.Editor editor = preferences.edit();
        long transactionCount = preferences.getLong(AppRate.PREF_TRANSACTION_COUNT, 0) + 1;
        editor.putLong(AppRate.PREF_TRANSACTION_COUNT, transactionCount);
        editor.apply();
        return this;
    }

    /**
     * Returns the Rating dialog.
     *
     * @return An {@link AlertDialog}
     */
    public AlertDialog getRateDialog() {

        String title = hostActivity.getString(R.string.rate_title);
        String message = hostActivity.getString(R.string.rate_message);
        String rate = hostActivity.getString(R.string.rate_yes);
        String remindLater = hostActivity.getString(R.string.rate_later);
        String dismiss = hostActivity.getString(R.string.rate_no);

        return new AlertDialog.Builder(hostActivity, R.style.StackedAlertDialogStyle)
                .setIcon(R.drawable.ic_launcher)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(rate, this)
                .setNegativeButton(dismiss, this)
                .setNeutralButton(remindLater, this)
                .setOnCancelListener(this)
                .create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {

        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(AppRate.PREF_DATE_REMIND_START, System.currentTimeMillis());
        editor.putLong(AppRate.PREF_TRANSACTION_COUNT, 0);
        editor.apply();
    }

    /**
     * @param onClickListener A listener to be called back on.
     * @return This {@link AppRate} object to allow chaining.
     */
    public AppRate setOnClickListener(OnClickListener onClickListener) {
        clickListener = onClickListener;
        return this;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

        SharedPreferences.Editor editor = preferences.edit();

        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                try {
                    hostActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + hostActivity.getPackageName())));
                } catch (ActivityNotFoundException e) {
                    ToastCustom.makeText(hostActivity, "No Play Store installed on device", ToastCustom.LENGTH_LONG, ToastCustom.TYPE_ERROR);
                }
                editor.putBoolean(AppRate.PREF_DONT_SHOW_AGAIN, true);
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                editor.putBoolean(AppRate.PREF_DONT_SHOW_AGAIN, true);
                break;

            case DialogInterface.BUTTON_NEUTRAL:

                setMinDaysUntilPrompt(7);
                editor.putLong(AppRate.PREF_DATE_REMIND_START, System.currentTimeMillis());

                break;

            default:
                break;
        }

        editor.apply();
        dialog.dismiss();

        if (clickListener != null) {
            clickListener.onClick(dialog, which);
        }
    }
}