package com.wavesplatform.wallet.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.databinding.ActivityPasswordWalletBinding;
import com.wavesplatform.wallet.ui.base.BaseAuthActivity;
import com.wavesplatform.wallet.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.util.PasswordUtil;
import com.wavesplatform.wallet.util.annotations.Thunk;

import static com.wavesplatform.wallet.ui.auth.PinEntryViewModel.KEY_INTENT_CREATE_NEW_PIN;

public class PasswordWalletActivity extends BaseAuthActivity {

    public static final String KEY_INTENT_EMAIL = "intent_email";
    public static final String KEY_INTENT_PASSWORD = "intent_password";
    public static final String KEY_INTENT_SEED = "intent_seed";

    @Thunk int pwStrength;
    @Thunk final int[] strengthVerdicts = {R.string.strength_weak, R.string.strength_medium, R.string.strength_normal, R.string.strength_strong};
    @Thunk final int[] strengthColors = {R.drawable.progress_red, R.drawable.progress_orange, R.drawable.progress_blue, R.drawable.progress_green};

    @Thunk
    ActivityPasswordWalletBinding binding;
    private boolean mIsImportWallet = false;

    private String mSeed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_password_wallet);

        mSeed = getIntent().getStringExtra(KEY_INTENT_SEED);
        mIsImportWallet = getIntent().getBooleanExtra(LandingActivity.KEY_INTENT_IMPORT_WALLET, false);

        if (mIsImportWallet) {
            setTitle(getString(R.string.import_wallet));
            binding.commandNext.setText(getString(R.string.dialog_continue));
        } else {
            setTitle(getString(R.string.new_wallet));
            binding.commandNext.setText(getString(R.string.create_wallet));
        }

        binding.commandNext.setClickable(false);
        binding.entropyContainer.passStrengthBar.setMax(100);

        binding.emailAddress.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !isFinishing()) {
                setEntropyMeterVisible(View.GONE);
            }
        });

        binding.walletPassConfrirm.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !isFinishing()) {
                setEntropyMeterVisible(View.GONE);
            }
        });

        binding.walletPass.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No-op
            }

            @Override
            public void afterTextChanged(final Editable editable) {
                binding.emailAddress.postDelayed(() -> {
                    if (!isFinishing()) {
                        setEntropyMeterVisible(View.VISIBLE);

                        final String pw = editable.toString();

                        if (pw.equals(binding.emailAddress.getText().toString())) {
                            // Email and password can't be the same
                            pwStrength = 0;
                        } else {
                            pwStrength = (int) Math.round(PasswordUtil.getInstance().getStrength(pw));
                        }

                        int pwStrengthLevel = 0;//red
                        if (pwStrength >= 75) pwStrengthLevel = 3;//green
                        else if (pwStrength >= 50) pwStrengthLevel = 2;//green
                        else if (pwStrength >= 25) pwStrengthLevel = 1;//orange

                        setProgress(pwStrengthLevel, pwStrength);
                    }

                }, 200);
            }

            private void setProgress(final int pwStrengthLevel, final int scorePerc) {
                binding.entropyContainer.passStrengthBar.setProgress(scorePerc);
                binding.entropyContainer.passStrengthBar.setProgressDrawable(
                        ContextCompat.getDrawable(getBaseContext(), strengthColors[pwStrengthLevel]));
                binding.entropyContainer.passStrengthVerdict.setText(getResources().getString(strengthVerdicts[pwStrengthLevel]));
            }
        });

        binding.commandNext.setOnClickListener(v -> {

            final String em = binding.emailAddress.getText().toString().trim();
            final String pw1 = binding.walletPass.getText().toString();
            final String pw2 = binding.walletPassConfrirm.getText().toString();

            if (!com.wavesplatform.wallet.util.StringUtils.isValidName(em)) {
                ToastCustom.makeText(this, getString(R.string.invalid_wallet_name), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
            } else
            if (pw1.length() < 8) {
                ToastCustom.makeText(this, getString(R.string.invalid_password_too_short), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
            } else if (pw1.length() > 255) {
                ToastCustom.makeText(this, getString(R.string.invalid_password), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
            } else if (!pw1.equals(pw2)) {
                ToastCustom.makeText(this, getString(R.string.password_mismatch_error), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
            } else if (pwStrength < 50) {

                new AlertDialog.Builder(this, R.style.AlertDialogStyle)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.weak_password)
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, (dialog, whichButton) -> {
                            binding.walletPass.setText("");
                            binding.walletPassConfrirm.setText("");
                            binding.walletPass.requestFocus();
                        }).setNegativeButton(R.string.no, (dialog, whichButton) -> {
                    hideKeyboard();
                    startActivity(getNextActivityIntent(em, pw1));
                }).show();
            } else {
                hideKeyboard();
                startActivity(getNextActivityIntent(em, pw1));
            }
        });

    }

    private Intent getNextActivityIntent(String email, String password) {
        Intent intent = new Intent(this, PinEntryActivity.class);
        intent.putExtra(KEY_INTENT_EMAIL, email);
        intent.putExtra(KEY_INTENT_PASSWORD, password);
        intent.putExtra(KEY_INTENT_SEED, mSeed);
        intent.putExtra(KEY_INTENT_CREATE_NEW_PIN, true);
        return intent;
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Thunk
    void setEntropyMeterVisible(final int visible) {
        binding.entropyContainer.entropyMeter.setVisibility(visible);
    }
}
