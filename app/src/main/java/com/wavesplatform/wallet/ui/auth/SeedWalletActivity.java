package com.wavesplatform.wallet.ui.auth;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.databinding.ActivitySeedWalletBinding;
import com.wavesplatform.wallet.ui.base.BaseAuthActivity;
import com.wavesplatform.wallet.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.util.ViewUtils;
import com.wavesplatform.wallet.util.annotations.Thunk;

public class SeedWalletActivity extends BaseAuthActivity {

    public static String KEY_INTENT_SEED = "intent_seed";

    @Thunk
    ActivitySeedWalletBinding binding;
    private String extraSeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_seed_wallet);

        binding.toolbarContainer.toolbarGeneral.setTitle(R.string.wallet_seed);
        ViewUtils.setElevation(binding.toolbarContainer.toolbarGeneral, 0F);
        setSupportActionBar(binding.toolbarContainer.toolbarGeneral);

        extraSeed = getIntent().getStringExtra(KEY_INTENT_SEED);

        if (extraSeed == null) {
            binding.tvWalletSeed.setVisibility(View.GONE);
            binding.tilWalletSeed.setVisibility(View.VISIBLE);
        } else {
            binding.tvWalletSeed.setText(extraSeed);
        }

        binding.commandNext.setOnClickListener(v -> onNext());

    }

    private void onNext() {
        if (extraSeed != null) {
            goToNextActivity(extraSeed);
        } else {
            String enteredSeed = binding.walletSeed.getText().toString().trim();
            if (enteredSeed.length() < 60) {
                ToastCustom.makeText(this, getString(R.string.invalid_seed_too_short), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
            } else {
                goToNextActivity(enteredSeed);
            }
        }
    }

    private Intent goToNextActivity(String seed) {
        Intent intent = new Intent(this, PasswordWalletActivity.class);
        intent.putExtra(KEY_INTENT_SEED, seed);
        startActivity(intent);
        return intent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
