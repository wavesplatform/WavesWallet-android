package com.wavesplatform.wallet.ui.assets;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.api.NodeManager;
import com.wavesplatform.wallet.data.connectivity.ConnectivityStatus;
import com.wavesplatform.wallet.databinding.ActivityIssueAssetBinding;
import com.wavesplatform.wallet.databinding.FragmentIssueConfirmBinding;
import com.wavesplatform.wallet.databinding.FragmentSendSuccessBinding;
import com.wavesplatform.wallet.payload.AssetBalance;
import com.wavesplatform.wallet.request.IssueTransactionRequest;
import com.wavesplatform.wallet.ui.auth.PinEntryActivity;
import com.wavesplatform.wallet.ui.balance.TransactionsFragment;
import com.wavesplatform.wallet.ui.base.BaseAuthActivity;
import com.wavesplatform.wallet.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.ui.transactions.SubmitTransactionsUtils;
import com.wavesplatform.wallet.util.MoneyUtil;
import com.wavesplatform.wallet.util.annotations.Thunk;

import static android.databinding.DataBindingUtil.inflate;
import static com.wavesplatform.wallet.ui.auth.PinEntryFragment.KEY_VALIDATED_PASSWORD;
import static com.wavesplatform.wallet.ui.auth.PinEntryFragment.KEY_VALIDATING_PIN_FOR_RESULT;
import static com.wavesplatform.wallet.ui.auth.PinEntryFragment.REQUEST_CODE_VALIDATE_PIN;


public class IssueAssetsActivity extends BaseAuthActivity implements IssueViewModel.DataListener {

    private static final int COOL_DOWN_MILLIS = 2 * 1000;

    @Thunk
    ActivityIssueAssetBinding binding;
    @Thunk IssueViewModel viewModel;
    private long backPressed;

    private AlertDialog confirmDialog;
    private AlertDialog successDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_issue_asset);

        viewModel = new IssueViewModel(this, this, binding);
        binding.setViewModel(viewModel);

        setupToolbar();

        setupViews();

    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbarContainer.toolbarGeneral);
        getSupportActionBar().setTitle(getString(R.string.issue_asset));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.issue_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_send:
                //customKeypad.setNumpadVisibility(View.GONE);

                if (ConnectivityStatus.hasConnectivity(this)) {
                    viewModel.sendClicked();
                } else {
                    ToastCustom.makeText(this, getString(R.string.check_connectivity_exit), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_VALIDATE_PIN && resultCode == RESULT_OK && data != null
            && data.getStringExtra(KEY_VALIDATED_PASSWORD) != null) {
            if (viewModel.signTransaction()) {
                viewModel.submitIssue();
            }
        }
    }

    private void setupViews() {

    }

    private AlertDialog createSendSuccessDialog(IssueTransactionRequest signed) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        FragmentSendSuccessBinding dialogBinding = DataBindingUtil.inflate(LayoutInflater.from(this),
                R.layout.fragment_send_success, null, false);
        dialogBuilder.setView(dialogBinding.getRoot());

        successDialog = dialogBuilder.create();
        successDialog.setCanceledOnTouchOutside(false);
        dialogBinding.btnDone.setOnClickListener(v -> finishPage());
        dialogBinding.ivCheck.setOnClickListener(v -> finishPage());

        dialogBinding.btnFavorite.setVisibility(View.GONE);

        return successDialog;
    }

    @Override
    public void onShowTransactionSuccess(IssueTransactionRequest signed) {
        runOnUiThread(() -> {
            confirmDialog.cancel();
            SubmitTransactionsUtils.playAudio(this);

            AssetBalance pendingAsset = signed.createDisplayAsset();
            NodeManager.get().addPendingTransaction(pendingAsset.issueTransaction);
            NodeManager.get().addPendingAsset(pendingAsset);
            LocalBroadcastManager.getInstance(this).sendBroadcastSync(new Intent(TransactionsFragment.ACTION_INTENT));
            LocalBroadcastManager.getInstance(this).sendBroadcastSync(new Intent(AssetsActivity.ACTION_INTENT));

            final AlertDialog successDialog = createSendSuccessDialog(signed);
            successDialog.show();
        });
    }

    @Override
    public void finishPage() {
        if (confirmDialog != null && confirmDialog.isShowing()) {
            confirmDialog.dismiss();
        }
        if (successDialog != null && successDialog.isShowing()) {
            successDialog.dismiss();
        }
        finish();
    }


    @Override
    public void onShowTransactionDetails(IssueTransactionRequest request) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        FragmentIssueConfirmBinding dialogBinding = inflate(LayoutInflater.from(this),
                R.layout.fragment_issue_confirm, null, false);
        dialogBuilder.setView(dialogBinding.getRoot());

        String nonReissuable = request.reissuable ? "" : "NON ";
        dialogBinding.issueDetails.setText(Html.fromHtml(
                getString(R.string.issue_details, nonReissuable, request.name,
                        MoneyUtil.getScaledText(request.quantity, request.decimals))));

        if (!request.reissuable) {
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

        if (viewModel.signTransaction()) {
            viewModel.submitIssue();
        } else {
            requestPinDialog();
        }
    }

    private void requestPinDialog() {
        Intent intent = new Intent(this, PinEntryActivity.class);
        intent.putExtra(KEY_VALIDATING_PIN_FOR_RESULT, true);
        startActivityForResult(intent, REQUEST_CODE_VALIDATE_PIN);
    }


    @Override
    public void onShowToast(@StringRes int message, @ToastCustom.ToastType String toastType) {
        runOnUiThread(() -> ToastCustom.makeText(this, getString(message), ToastCustom.LENGTH_SHORT, toastType));
    }

    @Override
    public void hideKeyboard() {
        View view =getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.destroy();
    }

}
