package com.wavesplatform.wallet.v1.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.v1.data.access.AccessState;
import com.wavesplatform.wallet.v1.data.auth.IncorrectPinException;
import com.wavesplatform.wallet.v1.injection.Injector;
import com.wavesplatform.wallet.v1.ui.base.BaseViewModel;
import com.wavesplatform.wallet.v1.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.v1.ui.fingerprint.FingerprintHelper;
import com.wavesplatform.wallet.v1.util.AppUtil;
import com.wavesplatform.wallet.v1.util.DialogButtonCallback;
import com.wavesplatform.wallet.v1.util.PrefsUtil;
import com.wavesplatform.wallet.v1.util.SSLVerifyUtil;
import com.wavesplatform.wallet.v1.util.StringUtils;
import com.wavesplatform.wallet.v1.util.ViewUtils;
import com.wavesplatform.wallet.v1.util.annotations.Thunk;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static com.wavesplatform.wallet.v1.ui.auth.PinEntryFragment.KEY_VALIDATING_PIN_FOR_RESULT;

@SuppressWarnings("WeakerAccess")
public class PinEntryViewModel extends BaseViewModel {

    public static final String KEY_INTENT_EMAIL = "intent_email";
    public static final String KEY_INTENT_PASSWORD = "intent_password";
    public static final String KEY_INTENT_SEED = "intent_seed";
    public static final String KEY_INTENT_CREATE_NEW_PIN = "intent_create_new_pin";

    private static final int PIN_LENGTH = 4;
    private static final int MAX_ATTEMPTS = 4;

    private DataListener mDataListener;
    @Inject protected AppUtil mAppUtil;
    @Inject protected PrefsUtil mPrefsUtil;
    @Inject protected StringUtils mStringUtils;
    @Inject protected SSLVerifyUtil mSSLVerifyUtil;
    @Inject protected FingerprintHelper mFingerprintHelper;
    @Inject protected AccessState mAccessState;

    private String mEmail;
    private String mPassword;
    private String mSeed;
    private String mWalletGuid;

    boolean mCanShowFingerprintDialog = true;
    boolean mValidatingPinForResult = false;
    boolean mCreatingNewPin = false;
    String mUserEnteredPin = "";
    String mUserEnteredConfirmationPin;
    boolean bAllowExit = true;

    public interface DataListener {

        Intent getPageIntent();

        ImageView[] getPinBoxArray();

        void showProgressDialog(@StringRes int messageId, @Nullable String suffix);

        void showToast(@StringRes int message, @ToastCustom.ToastType String toastType);

        void dismissProgressDialog();

        void showRequestPasswordDialog();

        void showCommonPinWarning(DialogButtonCallback callback);

        void setTitleString(@StringRes int title);

        void setTitleVisibility(@ViewUtils.Visibility int visibility);

        void clearPinBoxes();

        void finishWithResultOk(String password);

        void showFingerprintDialog(String pincode);

        void showKeyboard();

        void hideSoftKeyboard();

    }

    public PinEntryViewModel(DataListener listener) {
        Injector.getInstance().getDataManagerComponent().inject(this);
        mDataListener = listener;
    }


    @Override
    public void onViewReady() {
        mSSLVerifyUtil.validateSSL();
        mAppUtil.applyPRNGFixes();

        if (mDataListener.getPageIntent() != null) {
            restoreExtras();
        }

        checkPinFails();
        checkFingerprintStatus();

        if (mValidatingPinForResult) {
            mDataListener.setTitleString(R.string.pin_entry);
        }
    }

    private void restoreExtras() {
        Bundle extras = mDataListener.getPageIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(KEY_INTENT_EMAIL)) {
                mEmail = extras.getString(KEY_INTENT_EMAIL);
            }

            if (extras.containsKey(KEY_INTENT_PASSWORD)) {
                //noinspection ConstantConditions
                mPassword = extras.getString(KEY_INTENT_PASSWORD);
            }

            if (extras.containsKey(KEY_VALIDATING_PIN_FOR_RESULT)) {
                mValidatingPinForResult = extras.getBoolean(KEY_VALIDATING_PIN_FOR_RESULT);
            }

            if (extras.containsKey(KEY_INTENT_SEED)) {
                mSeed = extras.getString(KEY_INTENT_SEED);
            }

            if (extras.containsKey(KEY_INTENT_CREATE_NEW_PIN)) {
                mCreatingNewPin = extras.getBoolean(KEY_INTENT_CREATE_NEW_PIN);
            }

            if (!mValidatingPinForResult && mPassword != null && mPassword.length() > 0) {
                // Previous page was CreateWalletFragment
                bAllowExit = false;
                saveLoginAndPassword();
                mDataListener.showProgressDialog(R.string.create_wallet, "...");
                createWallet();
            }
        }
    }

    public void checkFingerprintStatus() {
        if (getIfShouldShowFingerprintLogin()) {
            mDataListener.showFingerprintDialog(
                    mFingerprintHelper.getEncryptedData(PrefsUtil.KEY_ENCRYPTED_PIN_CODE));
        } else {
            mDataListener.showKeyboard();
        }
    }

    public boolean canShowFingerprintDialog() {
        return mCanShowFingerprintDialog;
    }

    public boolean getIfShouldShowFingerprintLogin() {
        return !(mValidatingPinForResult || isCreatingNewPin())
                && mFingerprintHelper.getIfFingerprintUnlockEnabled()
                && mFingerprintHelper.getEncryptedData(PrefsUtil.KEY_ENCRYPTED_PIN_CODE) != null;
    }

    public void loginWithDecryptedPin(String pincode) {
        mCanShowFingerprintDialog = false;
        for (ImageView view : mDataListener.getPinBoxArray()) {
            view.setImageResource(R.drawable.rounded_view_dark_blue);
        }
        validatePin(pincode);
    }

    public void onDeleteClicked() {
        if (mUserEnteredPin.length() > 0) {
            // Remove last char from pin string
            mUserEnteredPin = mUserEnteredPin.substring(0, mUserEnteredPin.length() - 1);

            // Clear last box
            mDataListener.getPinBoxArray()[mUserEnteredPin.length()].setImageResource(R.drawable.rounded_view_blue_white_border);
        }
    }

    public void onPadClicked(String string) {
        if (mUserEnteredPin.length() == PIN_LENGTH) {
            return;
        }

        // Append tapped #
        mUserEnteredPin = mUserEnteredPin + string;
        mDataListener.getPinBoxArray()[mUserEnteredPin.length() - 1].setImageResource(R.drawable.rounded_view_dark_blue);

        // Perform appropriate action if PIN_LENGTH has been reached
        if (mUserEnteredPin.length() == PIN_LENGTH) {

            if (mUserEnteredPin.equals("0000")) {
                showErrorToast(R.string.zero_pin);
                clearPinViewAndReset();
                return;
            }

            // Only show warning on first entry and if user is creating a new PIN
            if (isCreatingNewPin() && isPinCommon(mUserEnteredPin) && mUserEnteredConfirmationPin == null) {
                mDataListener.showCommonPinWarning(new DialogButtonCallback() {
                    @Override
                    public void onPositiveClicked() {
                        clearPinViewAndReset();
                    }

                    @Override
                    public void onNegativeClicked() {
                        validateAndConfirmPin();
                    }
                });

            } else {
                validateAndConfirmPin();
            }
        }
    }

    @Thunk
    void validateAndConfirmPin() {
        if (!mCreatingNewPin) {
            validatePin(mUserEnteredPin);
        } else if (mUserEnteredConfirmationPin == null) {
            // End of Create -  Change to Confirm
            mUserEnteredConfirmationPin = mUserEnteredPin;
            mUserEnteredPin = "";
            mDataListener.setTitleString(R.string.confirm_pin);
            clearPinBoxes();
        } else if (mUserEnteredConfirmationPin.equals(mUserEnteredPin)) {
            // End of Confirm - Pin is confirmed
            createNewPin(mUserEnteredPin);
        } else {
            // End of Confirm - Pin Mismatch
            showErrorToast(R.string.pin_mismatch_error);
            mDataListener.setTitleString(R.string.create_pin);
            clearPinViewAndReset();
        }
    }

    /**
     * Resets the view without restarting the page
     */
    @Thunk
    void clearPinViewAndReset() {
        clearPinBoxes();
        mUserEnteredConfirmationPin = null;
        checkFingerprintStatus();
        if (mDataListener != null) {
            mDataListener.hideSoftKeyboard();
        }
    }

    public void clearPinBoxes() {
        mUserEnteredPin = "";
        mDataListener.clearPinBoxes();
    }

    public boolean isForValidatingPinForResult() {
        return mValidatingPinForResult;
    }

    public void validatePassword(String password) {
        mDataListener.showProgressDialog(R.string.validating_password, null);

        if (mAccessState.restoreWavesWallet(password)) {
            mPassword = password;
            mCreatingNewPin = true;
            mDataListener.showToast(R.string.pin_4_strikes_password_accepted, ToastCustom.TYPE_OK);
            mDataListener.dismissProgressDialog();
            mPrefsUtil.removeValue(PrefsUtil.KEY_PIN_FAILS);
            mDataListener.setTitleString(R.string.create_pin);
            clearPinViewAndReset();
        } else {
            showErrorToast(R.string.invalid_password);
            mDataListener.showRequestPasswordDialog();
        }
    }

    private void createNewPin(String pin) {
        mDataListener.showProgressDialog(R.string.creating_pin, null);
        compositeDisposable.add(
                mAccessState.createPin(getGuid(), mPassword, pin)
                        .subscribe(() -> {
                            mDataListener.dismissProgressDialog();
                                mFingerprintHelper.clearEncryptedData(PrefsUtil.KEY_ENCRYPTED_PIN_CODE);
                                mFingerprintHelper.setFingerprintUnlockEnabled(false);
                                mPrefsUtil.removeValue(PrefsUtil.KEY_PIN_FAILS);
                                if (mValidatingPinForResult) {
                                    mDataListener.finishWithResultOk(mPassword.toString());
                                } else {
                                    mAppUtil.restartAppWithVerifiedPin();
                                }
                        }, throwable -> {
                            showErrorToast(R.string.create_pin_failed);
                            mAppUtil.restartApp();
                        }));
    }

    private String getGuid() {
        return mWalletGuid != null ? mWalletGuid : mPrefsUtil.getGlobalValue(PrefsUtil.GLOBAL_LOGGED_IN_GUID, "");
    }

    private void validatePin(String pin) {
        mDataListener.showProgressDialog(R.string.validating_pin, null);

        mAccessState.validatePin(pin).subscribe(pwd -> {
            mDataListener.dismissProgressDialog();
            mPrefsUtil.removeValue(PrefsUtil.KEY_PIN_FAILS);
            mDataListener.finishWithResultOk(pwd);

        }, err -> {
            handleValidateFailure();
            if (!(err instanceof IncorrectPinException)) {
                Log.e(getClass().getSimpleName(), "Failed to validate pin", err);
            }
        });
    }

    private void handleValidateFailure() {
        incrementFailureCount();
        checkPinFails();
    }

    private void incrementFailureCount() {
        int fails = mPrefsUtil.getValue(PrefsUtil.KEY_PIN_FAILS, 0);
        mPrefsUtil.setValue(PrefsUtil.KEY_PIN_FAILS, ++fails);
        showErrorToast(R.string.invalid_pin);
        mUserEnteredPin = "";
        for (ImageView textView : mDataListener.getPinBoxArray()) {
            textView.setImageResource(R.drawable.rounded_view_blue_white_border);
        }
        mDataListener.setTitleVisibility(View.VISIBLE);
        mDataListener.setTitleString(R.string.pin_entry);
    }

    // Check user's password if PIN fails >= 4
    private void checkPinFails() {
        int fails = mPrefsUtil.getValue(PrefsUtil.KEY_PIN_FAILS, 0);
        if (fails >= MAX_ATTEMPTS) {
            showErrorToast(R.string.pin_4_strikes);
            mDataListener.showRequestPasswordDialog();
        }
    }

    private void saveLoginAndPassword() {
        if (mEmail == null || mEmail.isEmpty())
            mEmail = mStringUtils.getString(R.string.default_wallet_name);
    }

    private void createWallet() {
        mAppUtil.applyPRNGFixes();

        mWalletGuid = mAccessState.storeWavesWallet(mSeed, mPassword, mEmail);
        mDataListener.dismissProgressDialog();

        if (mWalletGuid == null) {
            showErrorToast(R.string.hd_error);
        }
    }

    private boolean isPinCommon(String pin) {
        List<String> commonPins = new ArrayList<String>() {{
            add("1234");
            add("1111");
            add("1212");
            add("7777");
            add("1004");
        }};
        return commonPins.contains(pin);
    }

    public void resetApp() {
        mAppUtil.clearCredentialsAndRestart();
    }

    public boolean allowExit() {
        return bAllowExit;
    }

    public boolean isCreatingNewPin() {
        return !mValidatingPinForResult;
    }

    @UiThread
    private void showErrorToast(@StringRes int message) {
        mDataListener.dismissProgressDialog();
        mDataListener.showToast(message, ToastCustom.TYPE_ERROR);
    }

    @UiThread
    private void showErrorToastAndRestartApp(@StringRes int message) {
        mDataListener.dismissProgressDialog();
        mDataListener.showToast(message, ToastCustom.TYPE_ERROR);
        resetApp();
    }

    @NonNull
    public AppUtil getAppUtil() {
        return mAppUtil;
    }
}
