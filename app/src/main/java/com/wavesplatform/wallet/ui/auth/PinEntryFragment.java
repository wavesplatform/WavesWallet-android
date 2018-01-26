package com.wavesplatform.wallet.ui.auth;


import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.data.connectivity.ConnectivityStatus;
import com.wavesplatform.wallet.databinding.FragmentPinEntryBinding;
import com.wavesplatform.wallet.ui.customviews.MaterialProgressDialog;
import com.wavesplatform.wallet.ui.customviews.PinEntryKeypad;
import com.wavesplatform.wallet.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.ui.fingerprint.FingerprintDialog;
import com.wavesplatform.wallet.util.DialogButtonCallback;
import com.wavesplatform.wallet.util.ViewUtils;
import com.wavesplatform.wallet.util.annotations.Thunk;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

@SuppressWarnings("WeakerAccess")
public class PinEntryFragment extends Fragment implements PinEntryViewModel.DataListener  {

    public static final String KEY_VALIDATING_PIN_FOR_RESULT = "validating_pin";
    public static final String KEY_VALIDATED_PIN = "validated_pin";
    public static final String KEY_VALIDATED_PASSWORD = "intent_validated_password";
    public static final int REQUEST_CODE_VALIDATE_PIN = 88;
    private static final int COOL_DOWN_MILLIS = 2 * 1000;
    private static final String KEY_SHOW_SWIPE_HINT = "show_swipe_hint";
    private static final int PIN_LENGTH = 4;
    private static final Handler HANDLER = new Handler();

    private ImageView[] pinBoxArray;
    private MaterialProgressDialog materialProgressDialog;
    private FragmentPinEntryBinding binding;
    private FingerprintDialog fingerprintDialog;
    private ViewGroup keyboardLayout;
    private boolean isPaused = false;
    @Thunk PinEntryViewModel viewModel;

    private long backPressed;

    public PinEntryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_pin_entry, container, false);

        viewModel = new PinEntryViewModel(this);

        keyboardLayout = (ViewGroup) binding.getRoot().findViewById(R.id.keyboard);

        // Set title state
        if (viewModel.isCreatingNewPin()) {
            binding.titleBox.setText(R.string.create_pin);
        } else {
            binding.titleBox.setText(R.string.pin_entry);
        }

        pinBoxArray = new ImageView[PIN_LENGTH];
        pinBoxArray[0] = binding.pinBox0;
        pinBoxArray[1] = binding.pinBox1;
        pinBoxArray[2] = binding.pinBox2;
        pinBoxArray[3] = binding.pinBox3;

        showConnectionDialogIfNeeded();

        viewModel.onViewReady();

        binding.keyboard.setPadClickedListener(new PinEntryKeypad.OnPinEntryPadClickedListener() {
            @Override
            public void onNumberClicked(String number) {
                viewModel.onPadClicked(number);
            }

            @Override
            public void onDeleteClicked() {
                viewModel.onDeleteClicked();
            }
        });

        return binding.getRoot();
    }

    @Override
    public void showFingerprintDialog(String pincode) {
        // Show icon for relaunching dialog
        binding.fingerprintLogo.setVisibility(View.VISIBLE);
        binding.fingerprintLogo.setOnClickListener(v -> viewModel.checkFingerprintStatus());
        // Show dialog itself if not already showing
        if (fingerprintDialog == null && viewModel.canShowFingerprintDialog()) {
            fingerprintDialog = FingerprintDialog.newInstance(pincode, FingerprintDialog.Stage.AUTHENTICATE);
            fingerprintDialog.setAuthCallback(new FingerprintDialog.FingerprintAuthCallback() {
                @Override
                public void onAuthenticated(String data) {
                    dismissFingerprintDialog();
                    viewModel.loginWithDecryptedPin(data);
                }

                @Override
                public void onCanceled() {
                    dismissFingerprintDialog();
                    showKeyboard();
                }
            });

            HANDLER.postDelayed(() -> {
                if (!getActivity().isFinishing() && !isPaused) {
                    fingerprintDialog.show(getActivity().getSupportFragmentManager(), FingerprintDialog.TAG);
                } else {
                    fingerprintDialog = null;
                }
            }, 200);

            hideKeyboard();
        }
    }

    @Override
    public void showKeyboard() {
        if (keyboardLayout.getVisibility() == View.INVISIBLE) {
            Animation bottomUp = AnimationUtils.loadAnimation(getContext(), R.anim.bottom_up);
            keyboardLayout.startAnimation(bottomUp);
            keyboardLayout.setVisibility(View.VISIBLE);
        }
    }

    private void hideKeyboard() {
        if (keyboardLayout.getVisibility() == View.VISIBLE) {
            Animation bottomUp = AnimationUtils.loadAnimation(getContext(), R.anim.top_down);
            keyboardLayout.startAnimation(bottomUp);
            keyboardLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void showConnectionDialogIfNeeded() {
        if (!ConnectivityStatus.hasConnectivity(getContext())) {
            new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle)
                    .setMessage(getString(R.string.check_connectivity_exit))
                    .setCancelable(false)
                    .setPositiveButton(R.string.dialog_continue, (dialog, id) -> viewModel.getAppUtil().restartApp())
                    .create()
                    .show();
        }
    }

    public void onBackPressed() {
        if (viewModel.isForValidatingPinForResult()) {
            finishWithResultCanceled();

        } else if (viewModel.allowExit()) {
            if (backPressed + COOL_DOWN_MILLIS > System.currentTimeMillis()) {
                return;
            } else {
                showToast(R.string.exit_confirm, ToastCustom.TYPE_GENERAL);
            }

            backPressed = System.currentTimeMillis();
        }
    }

    @Override
    public void clearPinBoxes() {
        HANDLER.postDelayed(new ClearPinNumberRunnable(), 200);
    }

    @Override
    public void setTitleString(@StringRes int title) {
        HANDLER.postDelayed(() -> binding.titleBox.setText(title), 200);
    }

    @Override
    public void setTitleVisibility(@ViewUtils.Visibility int visibility) {
        binding.titleBox.setVisibility(visibility);
    }

    public boolean allowExit() {
        return viewModel.allowExit();
    }

    public boolean isValidatingPinForResult() {
        return viewModel.isForValidatingPinForResult();
    }

    @Override
    public ImageView[] getPinBoxArray() {
        return pinBoxArray;
    }

    @Override
    public void showCommonPinWarning(DialogButtonCallback callback) {
        new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle)
                .setTitle(R.string.common_pin_dialog_title)
                .setMessage(R.string.common_pin_dialog_message)
                .setPositiveButton(R.string.common_pin_dialog_try_again, (dialogInterface, i) -> callback.onPositiveClicked())
                .setNegativeButton(R.string.common_pin_dialog_continue, (dialogInterface, i) -> callback.onNegativeClicked())
                .setCancelable(false)
                .create()
                .show();
    }

    @Override
    public void showRequestPasswordDialog() {
        final AppCompatEditText password = new AppCompatEditText(getContext());
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        password.setHint(getString(R.string.password_entry));

        new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle)
                .setTitle(R.string.app_name)
                .setMessage(getString(R.string.pin_4_strikes))
                .setView(ViewUtils.getAlertDialogEditTextLayout(getContext(), password))
                .setCancelable(false)
                .setNegativeButton(android.R.string.cancel, (dialog, whichButton) -> viewModel.getAppUtil().restartApp())
                .setPositiveButton(android.R.string.ok, (dialog, whichButton) -> {
                    final String pw = password.getText().toString();
                    if (pw.length() > 0) {
                        viewModel.validatePassword(pw);
                    } else {
                        password.setError(getString(R.string.invalid_password_too_short));
                    }

                }).show();
    }

    @Override
    public void showToast(@StringRes int message, @ToastCustom.ToastType String toastType) {
        ToastCustom.makeText(getContext(), getString(message), ToastCustom.LENGTH_SHORT, toastType);
    }

    @Override
    public void showProgressDialog(@StringRes int messageId, @Nullable String suffix) {
        dismissProgressDialog();
        materialProgressDialog = new MaterialProgressDialog(getContext());
        materialProgressDialog.setCancelable(false);
        if (suffix != null) {
            materialProgressDialog.setMessage(getString(messageId) + suffix);
        } else {
            materialProgressDialog.setMessage(getString(messageId));
        }

        if (!getActivity().isFinishing()) materialProgressDialog.show();
    }

    @Override
    public void dismissProgressDialog() {
        if (materialProgressDialog != null && materialProgressDialog.isShowing()) {
            materialProgressDialog.dismiss();
            materialProgressDialog = null;
        }
    }

    @Override
    public void hideSoftKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isPaused = false;
        viewModel.clearPinBoxes();
        viewModel.checkFingerprintStatus();
    }

    @Override
    public void finishWithResultOk(String password) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_VALIDATED_PASSWORD, password);
        Intent intent = new Intent();
        intent.putExtras(bundle);
        getActivity().setResult(RESULT_OK, intent);
        getActivity().finish();
    }

    private void finishWithResultCanceled() {
        Intent intent = new Intent();
        getActivity().setResult(RESULT_CANCELED, intent);
        getActivity().finish();
    }

    @Override
    public Intent getPageIntent() {
        return getActivity().getIntent();
    }

    @Override
    public void onPause() {
        super.onPause();
        isPaused = true;
        dismissFingerprintDialog();
    }

    @Override
    public void onDestroy() {
        dismissProgressDialog();
        viewModel.destroy();
        super.onDestroy();
    }

    @Thunk
    void dismissFingerprintDialog() {
        if (fingerprintDialog != null && fingerprintDialog.isVisible()) {
            fingerprintDialog.dismiss();
            fingerprintDialog = null;
        }

        // Hide if fingerprint unlock has become unavailable
        if (!viewModel.getIfShouldShowFingerprintLogin()) {
            binding.fingerprintLogo.setVisibility(View.GONE);
        }
    }

    private class ClearPinNumberRunnable implements Runnable {
        ClearPinNumberRunnable() {
            // Empty constructor
        }

        @Override
        public void run() {
            for (ImageView pinBox : getPinBoxArray()) {
                // Reset PIN buttons to blank
                pinBox.setImageResource(R.drawable.rounded_view_blue_white_border);
            }
        }
    }

}
