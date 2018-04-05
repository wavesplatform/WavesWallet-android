package com.wavesplatform.wallet.v1.ui.transactions;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.v1.api.NodeManager;
import com.wavesplatform.wallet.v1.data.access.AccessState;
import com.wavesplatform.wallet.v1.data.connectivity.ConnectivityStatus;
import com.wavesplatform.wallet.v1.data.rxjava.RxUtil;
import com.wavesplatform.wallet.databinding.ActivityIssueDetailsBinding;
import com.wavesplatform.wallet.databinding.FragmentIssueConfirmBinding;
import com.wavesplatform.wallet.databinding.FragmentReissueAssetBinding;
import com.wavesplatform.wallet.databinding.FragmentSendSuccessBinding;
import com.wavesplatform.wallet.v1.request.ReissueTransactionRequest;
import com.wavesplatform.wallet.v1.ui.auth.PinEntryActivity;
import com.wavesplatform.wallet.v1.ui.balance.TransactionsFragment;
import com.wavesplatform.wallet.v1.ui.base.BaseAuthActivity;
import com.wavesplatform.wallet.v1.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.v1.util.MoneyUtil;

import java.util.Collections;

import static android.databinding.DataBindingUtil.inflate;
import static com.wavesplatform.wallet.v1.ui.auth.PinEntryFragment.KEY_VALIDATED_PASSWORD;
import static com.wavesplatform.wallet.v1.ui.auth.PinEntryFragment.KEY_VALIDATING_PIN_FOR_RESULT;
import static com.wavesplatform.wallet.v1.ui.auth.PinEntryFragment.REQUEST_CODE_VALIDATE_PIN;

public class IssueDetailActivity extends BaseAuthActivity implements IssueDetailViewModel.IssueDataListener {

    public static String KEY_INTENT_ACTIONS_ENABLED = "intent_actions_enabled";
    private final String TAG = getClass().getSimpleName();

    ActivityIssueDetailsBinding mBinding;
    private IssueDetailViewModel mViewModel;

    private AlertDialog reissueDialog;
    private AlertDialog confirmDialog;
    private AlertDialog successDialog;


    ReissueTransactionRequest reissueRequest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_issue_details);
        mViewModel = new IssueDetailViewModel(this, this);
        mBinding.setViewModel(mViewModel);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_general);
        toolbar.setTitle(getResources().getString(R.string.issue_detail_title));
        setSupportActionBar(toolbar);

        mViewModel.onViewReady();

        setupBottomToolbar();
    }

    private void setupBottomToolbar() {
        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.reissue_button, R.drawable.ic_add_circle_outline_black_24dp, R.color.blockchain_pearl_white);

        // Add items
        mBinding.bottomNavigation.addItems(Collections.singletonList(item1));

        // Styling
        mBinding.bottomNavigation.setAccentColor(ContextCompat.getColor(this, R.color.blockchain_send_red));
        mBinding.bottomNavigation.setInactiveColor(ContextCompat.getColor(this, R.color.blockchain_send_red));
        mBinding.bottomNavigation.setForceTint(true);
        mBinding.bottomNavigation.setUseElevation(true);

        mBinding.bottomNavigation.setCurrentItem(0);

        mBinding.bottomNavigation.setOnTabSelectedListener((position, wasSelected) -> {
            if (position == 0) {
                showReissueDialog();
            }
            return true;
        });
    }

    public void showReissueDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        FragmentReissueAssetBinding reissueBinding = DataBindingUtil.inflate(LayoutInflater.from(this),
                R.layout.fragment_reissue_asset, null, false);
        reissueBinding.reissuable.setChecked(true);
        dialogBuilder.setView(reissueBinding.getRoot());

        dialogBuilder.setTitle(R.string.reissue_asset);
        dialogBuilder.setPositiveButton(R.string.reissue_button, (dialog, whichButton) -> {
            long addQuantity = MoneyUtil.getUnscaledValue(reissueBinding.quantity.getText().toString(), mViewModel.getDecimals());
            if (addQuantity <= 0 || (addQuantity + mViewModel.getTotalQuantity()) <= 0) {
                reissueBinding.quantity.setError(getString(R.string.invalid_quantity));
                ToastCustom.makeText(this, getString(R.string.correct_errors), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
            } else {
                reissueRequest = new ReissueTransactionRequest(mViewModel.getIdentifier(),
                        NodeManager.get().getPublicKeyStr(), addQuantity,
                        reissueBinding.reissuable.isChecked(), System.currentTimeMillis());
                showTransactionDetails();
            }
        }).setNegativeButton(android.R.string.cancel, null);

        reissueDialog = dialogBuilder.create();
        reissueDialog.setCanceledOnTouchOutside(false);
        reissueDialog.show();
    }

    public void showTransactionDetails() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        FragmentIssueConfirmBinding dialogBinding = inflate(LayoutInflater.from(this),
                R.layout.fragment_issue_confirm, null, false);
        dialogBuilder.setView(dialogBinding.getRoot());

        dialogBinding.confirmTitle.setText(getString(R.string.confirm_reissue));
        String nonReissuable = reissueRequest.reissuable ? "" : "NON ";
        dialogBinding.issueDetails.setText(Html.fromHtml(
                getString(R.string.reissue_details, nonReissuable, mViewModel.getAssetName(),
                        MoneyUtil.getScaledText(reissueRequest.quantity, mViewModel.getDecimals()))));

        if (!reissueRequest.reissuable) {
            dialogBinding.nonReissuableNotice.setText(Html.fromHtml(getString(R.string.non_reissuable_notice)));
        } else {
            dialogBinding.nonReissuableNotice.setVisibility(View.GONE);
        }

        confirmDialog= dialogBuilder.create();
        confirmDialog.setCanceledOnTouchOutside(false);


        dialogBinding.confirmCancel.setOnClickListener(v -> {
            if (confirmDialog.isShowing()) {
                confirmDialog.cancel();
            }
        });

        dialogBinding.confirmSend.setOnClickListener(v -> {
            if (ConnectivityStatus.hasConnectivity(this)) {
                confirmSend(dialogBinding);
            } else {
                ToastCustom.makeText(this, getString(R.string.check_connectivity_exit), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
                // Queue tx here
            }
        });

        if (!isFinishing()) {
            confirmDialog.show();
        }

        // To prevent the dialog from appearing too large on Android N
        if (confirmDialog.getWindow() != null) {
            confirmDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }

    }

    private void confirmSend(FragmentIssueConfirmBinding dialogBinding) {
        dialogBinding.confirmSend.setClickable(false);

        if (signTransaction()) {
            submitIssue();
        } else {
            requestPinDialog();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_VALIDATE_PIN && resultCode == RESULT_OK && data != null
                && data.getStringExtra(KEY_VALIDATED_PASSWORD) != null) {
            if (signTransaction()) {
                submitIssue();
            }
        }
    }

    public boolean signTransaction() {
        byte[] pk = AccessState.getInstance().getPrivateKey();
        if (pk != null) {
            reissueRequest.sign(pk);
            return true;
        } else {
            return false;
        }
    }

    private void requestPinDialog() {
        Intent intent = new Intent(this, PinEntryActivity.class);
        intent.putExtra(KEY_VALIDATING_PIN_FOR_RESULT, true);
        startActivityForResult(intent, REQUEST_CODE_VALIDATE_PIN);
    }

    public void onShowTransactionSuccess(ReissueTransactionRequest signed) {
        runOnUiThread(() -> {
            confirmDialog.cancel();
            SubmitTransactionsUtils.playAudio(this);

            NodeManager.get().addPendingTransaction(signed.createDisplayTransaction());
            LocalBroadcastManager.getInstance(this).sendBroadcastSync(new Intent(TransactionsFragment.ACTION_INTENT));

            final AlertDialog successDialog = createSendSuccessDialog();
            successDialog.show();
        });
    }

    private AlertDialog createSendSuccessDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        FragmentSendSuccessBinding dialogBinding = DataBindingUtil.inflate(LayoutInflater.from(this),
                R.layout.fragment_send_success, null, false);
        dialogBuilder.setView(dialogBinding.getRoot());

        successDialog = dialogBuilder.create();
        successDialog.setCanceledOnTouchOutside(false);
        dialogBinding.btnDone.setOnClickListener(v -> finishSubmit());
        dialogBinding.ivCheck.setOnClickListener(v -> finishSubmit());

        dialogBinding.btnFavorite.setVisibility(View.GONE);

        return successDialog;
    }

    private void finishSubmit() {
        if (confirmDialog != null && confirmDialog.isShowing()) {
            confirmDialog.dismiss();
        }
        if (successDialog != null && successDialog.isShowing()) {
            successDialog.dismiss();
        }
        if (reissueDialog != null && reissueDialog.isShowing()) {
            reissueDialog.dismiss();
        }
    }

    public void submitIssue() {
        NodeManager.get().broadcastReissue(reissueRequest)
                .compose(RxUtil.applySchedulersToObservable()).subscribe(tx ->
                onShowTransactionSuccess(reissueRequest), err -> {
            Log.e(TAG, "submitIssue: ", err);
            ToastCustom.makeText(this, getString(R.string.transaction_failed), ToastCustom.LENGTH_LONG, ToastCustom.TYPE_ERROR);
        });

    }

    @Override
    public void pageFinish() {
        finish();
    }

    @Override
    public Intent getPageIntent() {
        return getIntent();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewModel.destroy();
    }
}
