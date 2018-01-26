package com.wavesplatform.wallet.ui.backup;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.databinding.FragmentBackupStartBinding;
import com.wavesplatform.wallet.util.ViewUtils;
import com.wavesplatform.wallet.util.annotations.Thunk;

import static android.app.Activity.RESULT_OK;
import static com.wavesplatform.wallet.ui.auth.PinEntryFragment.REQUEST_CODE_VALIDATE_PIN;

public class BackupWalletStartingFragment extends Fragment {

    public static final String TAG = BackupWalletStartingFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentBackupStartBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_backup_start, container, false);

        ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && supportActionBar != null) {
            supportActionBar.setElevation(ViewUtils.convertDpToPixel(5F, getActivity()));
        }

        binding.backupWalletAction.setOnClickListener(v -> {

            Fragment fragment = new BackupWalletWordListFragment();
            Bundle args = new Bundle();
            args.putString("second_password", "a123456A");
            fragment.setArguments(args);
            loadFragment(fragment);
        });

        return binding.getRoot();
    }

    @Thunk
    void loadFragment(Fragment fragment) {
        getFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_NONE)
                .replace(R.id.content_frame, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_VALIDATE_PIN && resultCode == RESULT_OK) {
            loadFragment(new BackupWalletWordListFragment());
        }
    }
}