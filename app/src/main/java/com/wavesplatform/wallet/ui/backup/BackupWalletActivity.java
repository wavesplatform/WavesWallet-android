package com.wavesplatform.wallet.ui.backup;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.ui.base.BaseAuthActivity;
import com.wavesplatform.wallet.util.PrefsUtil;

public class BackupWalletActivity extends BaseAuthActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_wallet);

        Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar_general);
        toolbar.setTitle(getResources().getString(R.string.backup_wallet));
        setSupportActionBar(toolbar);

        if (isBackedUp()) {
            startFragment(com.wavesplatform.wallet.ui.backup.BackupWalletCompletedFragment.newInstance(false), com.wavesplatform.wallet.ui.backup.BackupWalletCompletedFragment.TAG);
        } else {
            startFragment(new com.wavesplatform.wallet.ui.backup.BackupWalletStartingFragment(), BackupWalletStartingFragment.TAG);
        }
    }

    private void startFragment(Fragment fragment, String tag) {
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack(tag)
                .commit();
    }

    private Fragment getCurrentFragment() {
        return getFragmentManager().findFragmentById(R.id.content_frame);
    }

    @Override
    public void onBackPressed() {
        if (getCurrentFragment() instanceof BackupWalletVerifyFragment) {
            ((BackupWalletVerifyFragment) getCurrentFragment()).onBackPressed();
        } else if (getCurrentFragment() instanceof BackupWalletWordListFragment) {
            ((BackupWalletWordListFragment) getCurrentFragment()).onBackPressed();
        }

        if (getFragmentManager().getBackStackEntryCount() <= 1) {
            finish();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private boolean isBackedUp() {
        return new PrefsUtil(this).getValue(PrefsUtil.KEY_BACKUP_DATE_KEY, 0) != 0;
    }
}