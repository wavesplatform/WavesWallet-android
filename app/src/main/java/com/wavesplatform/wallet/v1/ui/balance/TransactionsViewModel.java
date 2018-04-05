package com.wavesplatform.wallet.v1.ui.balance;

import android.content.Context;
import android.content.Intent;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.VisibleForTesting;
import android.text.Spannable;
import android.text.style.RelativeSizeSpan;

import com.google.common.collect.HashBiMap;
import com.wavesplatform.wallet.BR;
import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.v1.api.NodeManager;
import com.wavesplatform.wallet.v1.data.datamanagers.TransactionListDataManager;
import com.wavesplatform.wallet.v1.injection.Injector;
import com.wavesplatform.wallet.v1.payload.AssetBalance;
import com.wavesplatform.wallet.v1.payload.Transaction;
import com.wavesplatform.wallet.v1.ui.assets.AssetsHelper;
import com.wavesplatform.wallet.v1.ui.assets.ItemAccount;
import com.wavesplatform.wallet.v1.ui.base.ViewModel;
import com.wavesplatform.wallet.v1.util.PrefsUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

@SuppressWarnings("WeakerAccess")
public class TransactionsViewModel extends BaseObservable implements ViewModel {

    private static final long ONE_DAY = 24 * 60 * 60 * 1000L;

    private Context context;
    private DataListener dataListener;
    private String balance;

    private List<ItemAccount> activeAccountAndAddressList;
    private HashBiMap<AssetBalance, Integer> activeAccountAndAddressBiMap;
    private List<Transaction> transactionList;
    @Inject protected PrefsUtil prefsUtil;
    @Inject protected TransactionListDataManager transactionListDataManager;
    @Inject protected AssetsHelper assetsHelper;
    @VisibleForTesting CompositeDisposable compositeDisposable;

    @Bindable
    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
        notifyPropertyChanged(BR.balance);
    }

    public PrefsUtil getPrefsUtil() {
        return prefsUtil;
    }

    public interface DataListener {
        void onRefreshAccounts();

        void onAccountSizeChange();

        void onRefreshBalanceAndTransactions();

        void showBackupPromptDialog(boolean showNeverAgain);

        void setTopBalance(Spannable spannable);
    }

    public TransactionsViewModel(Context context, DataListener dataListener) {
        Injector.getInstance().getDataManagerComponent().inject(this);
        this.context = context;
        this.dataListener = dataListener;

        activeAccountAndAddressList = new ArrayList<>();
        activeAccountAndAddressBiMap = HashBiMap.create();
        transactionList = new ArrayList<>();
        compositeDisposable = new CompositeDisposable();
    }

    private void showBackupPromtIfNeede() {
        if (!prefsUtil.getValue(PrefsUtil.KEY_SECURITY_BACKUP_NEVER, false)
                && prefsUtil.getValue(PrefsUtil.KEY_BACKUP_DATE_KEY, 0) == 0
                && System.currentTimeMillis() - prefsUtil.getValue(PrefsUtil.KEY_LAST_BACKUP_PROMPT, 0L) >= ONE_DAY) {
            dataListener.showBackupPromptDialog(prefsUtil.getValue(PrefsUtil.KEY_LAST_BACKUP_PROMPT, 0L) != 0);
            prefsUtil.setValue(PrefsUtil.KEY_LAST_BACKUP_PROMPT, System.currentTimeMillis());
        }
    }

    public void onViewReady() {
        showBackupPromtIfNeede();
    }

    public void neverPromptBackup() {
        prefsUtil.setValue(PrefsUtil.KEY_SECURITY_BACKUP_NEVER, true);
    }

    @Override
    public void destroy() {
        context = null;
        dataListener = null;
        compositeDisposable.clear();
    }

    public List<ItemAccount> getActiveAccountAndAddressList() {
        return activeAccountAndAddressList;
    }

    public void updateAccountList() {

        //activeAccountAndAddressList is linked to Adapter - do not reconstruct or loose reference otherwise notifyDataSetChanged won't work
        activeAccountAndAddressList.clear();
        activeAccountAndAddressBiMap.clear();

        int spinnerIndex = 0;

        //Only V3 will display "All"
        String allLabel = context.getResources().getString(R.string.all_accounts);
        AssetBalance wavesAsset = NodeManager.get().wavesAsset;
        ItemAccount all = new ItemAccount(
                allLabel,
                "",
                0L,
                null);
        activeAccountAndAddressList.add(all);

        if (assetsHelper != null) {
            List<ItemAccount> assets = assetsHelper.getAccountItems();

            for (ItemAccount itemAccount : assets) {
                spinnerIndex++;
                activeAccountAndAddressList.add(itemAccount);
                activeAccountAndAddressBiMap.put((AssetBalance) itemAccount.accountObject, spinnerIndex);
            }
        }

        //If we have multiple accounts/addresses we will show dropdown in toolbar, otherwise we will only display a static text
        if (dataListener != null) dataListener.onRefreshAccounts();
    }

    public List<Transaction> getTransactionList() {
        return transactionList;
    }

    public void updateBalanceAndTransactionList(Intent intent, int accountSpinnerPosition) {

        AssetBalance ab = activeAccountAndAddressBiMap.inverse().get(accountSpinnerPosition);
        if (ab == null && dataListener !=null) {
            dataListener.onAccountSizeChange();
            ab = activeAccountAndAddressBiMap.inverse().get(accountSpinnerPosition);
        }

        transactionListDataManager.clearTransactionList();
        transactionListDataManager.generateTransactionList(ab);
        transactionList = transactionListDataManager.getTransactionList();

        if (ab != null) {
            setBalance(ab.getDisplayBalance());
        } else {
            Spannable spannable = Spannable.Factory.getInstance().newSpannable(NodeManager.get().wavesAsset.getDisplayBalanceWithUnit());
            spannable.setSpan(new RelativeSizeSpan(0.5f),
                    spannable.length() - NodeManager.get().wavesAsset.getName().length(),
                    spannable.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            setBalance(spannable.toString());
            if (dataListener != null) dataListener.setTopBalance(spannable);
        }
        if (dataListener != null) dataListener.onRefreshBalanceAndTransactions();
    }
}
