package com.wavesplatform.wallet.ui.assets;

import com.wavesplatform.wallet.api.NodeManager;
import com.wavesplatform.wallet.payload.AssetBalance;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.databinding .ActivityAccountsBinding;
import com.wavesplatform.wallet.ui.balance.TransactionsFragment;
import com.wavesplatform.wallet.ui.base.BaseAuthActivity;
import com.wavesplatform.wallet.ui.customviews.MaterialProgressDialog;
import com.wavesplatform.wallet.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.ui.transactions.IssueDetailActivity;
import com.wavesplatform.wallet.util.MoneyUtil;
import com.wavesplatform.wallet.util.annotations.Thunk;

import static com.wavesplatform.wallet.ui.transactions.IssueDetailActivity.KEY_INTENT_ACTIONS_ENABLED;

public class AssetsActivity extends BaseAuthActivity {

    public final static String KEY_MY_ASSETS_LIST_POSITION = "intent_my_assets_position";
    public static final String ACTION_INTENT = "com.wavesplatform.wallet.ui.account.AssetsActivity.REFRESH";

    private static final int ISSUE_REQUEST_CODE = 2001;
    private static final int EDIT_ACTIVITY_REQUEST_CODE = 2007;
    private static final int ADDRESS_LABEL_MAX_LENGTH = 17;

    public static final String IMPORT_ADDRESS = "import_account";
    public static final String CREATE_NEW = "create_wallet";

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            if (AssetsActivity.ACTION_INTENT.equals(intent.getAction())) {
                onUpdateAssetsList();
            }
        }
    };

    private ArrayList<AssetItem> myAssetsList;
    private AssetsAdapter accountsAdapter;
    @Thunk MaterialProgressDialog progress;
    @Thunk ActivityAccountsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_accounts);

        setSupportActionBar(binding.toolbarContainer.toolbarGeneral);
        getSupportActionBar().setTitle("");

        binding.accountsList.setLayoutManager(new LinearLayoutManager(this));
        binding.accountsList.setHasFixedSize(true);
        myAssetsList = new ArrayList<>();

        onUpdateAssetsList();
    }

    @Thunk
    void onRowClick(int position) {
        Intent intent = new Intent(this, IssueDetailActivity.class);
        intent.putExtra(KEY_MY_ASSETS_LIST_POSITION, position);
        intent.putExtra(KEY_INTENT_ACTIONS_ENABLED, true);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Thunk
    void createNewAccount() {
        Intent intent = new Intent(this, IssueAssetsActivity.class);
        startActivityForResult(intent, ISSUE_REQUEST_CODE);
    }

    public void onUpdateAssetsList() {
        myAssetsList.clear();

        // Create New button position
        myAssetsList.add(new AssetItem(-1, CREATE_NEW, null, null, false));

        List<AssetBalance> allAssets = NodeManager.get().getAllAssets();
        for (int i = 0; i < allAssets.size(); ++i) {
            AssetBalance ab = allAssets.get(i);
            if (ab.assetId != null && ab.issueTransaction.sender.equals(NodeManager.get().getAddress())) {
                myAssetsList.add(new AssetItem(i,
                        ab.getName(),
                        ab.assetId,
                        MoneyUtil.getScaledText(ab.quantity, ab.issueTransaction.decimals),
                        ab.isPending));
            }
        }

        if (accountsAdapter == null) {
            accountsAdapter = new AssetsAdapter(myAssetsList);
            accountsAdapter.setAccountHeaderListener(new AssetsAdapter.AccountHeadersListener() {
                @Override
                public void onCreateNewClicked() {
                    createNewAccount();
                }

                @Override
                public void onCardClicked(int correctedPosition) {
                    onRowClick(correctedPosition);
                }
            });

            binding.accountsList.setAdapter(accountsAdapter);
        } else {
            // Notify adapter of items changes
            accountsAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(TransactionsFragment.ACTION_INTENT);
        LocalBroadcastManager.getInstance(AssetsActivity.this).registerReceiver(receiver, filter);
        onUpdateAssetsList();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(AssetsActivity.this).unregisterReceiver(receiver);
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == EDIT_ACTIVITY_REQUEST_CODE) {
            onUpdateAssetsList();
        }
    }

    public void showToast(@StringRes int message, @ToastCustom.ToastType String toastType) {
        ToastCustom.makeText(this, getString(message), ToastCustom.LENGTH_SHORT, toastType);
    }

    public void broadcastIntent(Intent intent) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void showProgressDialog(@StringRes int message) {
        dismissProgressDialog();
        if (!isFinishing()) {
            progress = new MaterialProgressDialog(this);
            progress.setMessage(message);
            progress.setCancelable(false);
            progress.show();
        }
    }

    public void dismissProgressDialog() {
        if (progress != null && progress.isShowing()) {
            progress.dismiss();
            progress = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();
    }
}
