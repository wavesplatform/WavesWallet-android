package com.wavesplatform.wallet.v1.ui.auth;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SingleSelector;
import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.databinding.ActivityChooseWalletBinding;
import com.wavesplatform.wallet.v1.ui.base.BaseAuthActivity;
import com.wavesplatform.wallet.v1.util.AddressUtil;
import com.wavesplatform.wallet.v1.util.AppUtil;
import com.wavesplatform.wallet.v1.util.PrefsUtil;
import com.wavesplatform.wallet.v1.util.annotations.Thunk;

import java.util.ArrayList;
import java.util.List;

public class ChooseWalletActivity extends BaseAuthActivity implements WalletAdapter.WalletCardListener {

    private PrefsUtil prefsUtil;
    private ArrayList<WalletItem> wallets;
    private WalletAdapter walletAdapter;

    @Thunk
    ActivityChooseWalletBinding binding;

    private MultiSelector mMultiSelector = new SingleSelector();
    private ModalMultiSelectorCallback mDeleteMode = new ModalMultiSelectorCallback(mMultiSelector) {

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            super.onCreateActionMode(actionMode, menu);
            getMenuInflater().inflate(R.menu.wallet_item_context, menu);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if (menuItem.getItemId()==  R.id.menu_item_delete_wallet){
                showFirstDeleteWalletWarning(actionMode);
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            super.onDestroyActionMode(actionMode);
            mMultiSelector.clearSelections();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefsUtil = new PrefsUtil(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_choose_wallet);
        setSupportActionBar(binding.toolbarContainer.toolbarGeneral);

        setupWalletsList();
    }

    private void setupWalletsList() {
        String env = EnvironmentManager.get().current().getName();
        String[] guids = prefsUtil.getGlobalValueList(env + PrefsUtil.LIST_WALLET_GUIDS);
        wallets = new ArrayList<>();

        for (int i = 0; i < guids.length; ++i) {
            String pubKey = prefsUtil.getGlobalValue(guids[i] + PrefsUtil.KEY_PUB_KEY, "");
            String name = prefsUtil.getGlobalValue(guids[i] + PrefsUtil.KEY_WALLET_NAME, "");
            String address = AddressUtil.addressFromPublicKey(pubKey);
            wallets.add(new WalletItem(guids[i], name, address, pubKey));
        }

        walletAdapter = new WalletAdapter(wallets, mMultiSelector);
        walletAdapter.setWalletCardListener(this);

        binding.walletsList.setAdapter(walletAdapter);

        binding.walletsList.setLayoutManager(new LinearLayoutManager(this));
        binding.walletsList.setHasFixedSize(true);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public void onBackPressed() {
        new AppUtil(this).restartApp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCardClicked(int position) {
        WalletItem selItem = wallets.get(position);
        prefsUtil.setGlobalValue(PrefsUtil.GLOBAL_LOGGED_IN_GUID, selItem.guid);
        AuthUtil.startMainActivity(this, selItem.publicKey);
    }

    private void doRemoveWallet(int position) {
        WalletItem item = wallets.get(position);
        String env = prefsUtil.getEnvironment();
        prefsUtil.removeGlobalListValue(env + PrefsUtil.LIST_WALLET_GUIDS, position);
        prefsUtil.removeAllGuid(item.guid);

        if (position < wallets.size()) {
            walletAdapter.removeWallet(position);
        }
    }

    public void showFirstDeleteWalletWarning(ActionMode actionMode) {
        new AlertDialog.Builder(this, R.style.AlertDialogStyle)
                .setTitle("Delete Wallet")
                .setMessage("Are you sure you want to delete this wallet?")
                .setPositiveButton(R.string.delete_wallet, (dialog, which) -> showSecondDeleteWalletWarning(actionMode))
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> actionMode.finish())
                .show();
    }

    public void showSecondDeleteWalletWarning(ActionMode actionMode) {
        new AlertDialog.Builder(this, R.style.AlertDialogStyle)
                .setTitle(R.string.warning)
                .setMessage(R.string.forget_wallet_warning)
                .setNegativeButton(R.string.delete_wallet, (dialogInterface, i) -> {
                    List<Integer> pos = mMultiSelector.getSelectedPositions();
                    if (pos.size() == 1) {
                        doRemoveWallet(pos.get(0));
                    }
                    actionMode.finish();
                })
                .setPositiveButton(android.R.string.cancel, (dialogInterface, i) -> actionMode.finish())
                .create()
                .show();
    }

    @Override
    public void onCardLongClicked(int position) {
        startSupportActionMode(mDeleteMode);
    }
}
