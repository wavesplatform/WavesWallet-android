package com.wavesplatform.wallet.v1.ui.backup;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.databinding.FragmentBackupWalletVerifyBinding;
import com.wavesplatform.wallet.v1.ui.customviews.MaterialProgressDialog;
import com.wavesplatform.wallet.v1.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.v1.util.PrefsUtil;
import com.wavesplatform.wallet.v1.util.annotations.Thunk;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BackupWalletVerifyFragment extends Fragment {

    @Thunk FragmentBackupWalletVerifyBinding binding;
    @Thunk MaterialProgressDialog mProgressDialog;

    public static BackupWalletVerifyFragment createFragment(String[] mnemonic) {
        BackupWalletVerifyFragment f = new BackupWalletVerifyFragment();
        f.mnemonic = mnemonic;
        return f;
    }

    private String[] mnemonic;

    public void onBackPressed() {
        mnemonic = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_backup_wallet_verify, container, false);

        ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && supportActionBar != null) {
            supportActionBar.setElevation(0F);
        }

        final List<Pair<Integer, String>> confirmSequence = getConfirmSequence();
        String[] mnemonicRequestHint = getResources().getStringArray(R.array.mnemonic_word_requests);

        binding.etFirstRequest.setHint(mnemonicRequestHint[confirmSequence.get(0).first]);
        binding.etSecondRequest.setHint(mnemonicRequestHint[confirmSequence.get(1).first]);
        binding.etThirdRequest.setHint(mnemonicRequestHint[confirmSequence.get(2).first]);

        binding.verifyAction.setOnClickListener(v -> {
            if (binding.etFirstRequest.getText().toString().trim().equals(confirmSequence.get(0).second)
                    && binding.etSecondRequest.getText().toString().trim().equalsIgnoreCase(confirmSequence.get(1).second)
                    && binding.etThirdRequest.getText().toString().trim().equalsIgnoreCase(confirmSequence.get(2).second)) {

                new PrefsUtil(getActivity()).setValue(PrefsUtil.KEY_BACKUP_DATE_KEY, (int)(System.currentTimeMillis() / 1000));
                ToastCustom.makeText(getActivity(), getString(R.string.backup_confirmed), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_OK);
                popAllAndStartFragment(BackupWalletCompletedFragment.newInstance(true), BackupWalletCompletedFragment.TAG);

            } else {
                ToastCustom.makeText(getActivity(), getString(R.string.backup_word_mismatch), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
            }
        });

        return binding.getRoot();
    }

    private List<Pair<Integer, String>> getConfirmSequence() {
        List<Pair<Integer, String>> toBeConfirmed = new ArrayList<>();
        SecureRandom random = new SecureRandom();
        List<Integer> seen = new ArrayList<>();

        int sel = 0;
        int i = 0;
        while (i < 3) {
            sel = random.nextInt(mnemonic.length);
            if (!seen.contains(sel)) {
                seen.add(sel);
                i++;
            }
        }

        Collections.sort(seen);

        for (int ii = 0; ii < 3; ii++) {
            toBeConfirmed.add(new Pair<>(seen.get(ii), mnemonic[seen.get(ii)]));
        }

        return toBeConfirmed;
    }

    @Thunk
    void popAllAndStartFragment(Fragment fragment, String tag) {
        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack(tag)
                .commit();
    }

    private void showProgressDialog() {
        mProgressDialog = new MaterialProgressDialog(getActivity());
        mProgressDialog.setMessage(getString(R.string.please_wait) + "…");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Thunk
    void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}