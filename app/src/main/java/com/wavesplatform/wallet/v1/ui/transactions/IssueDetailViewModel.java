package com.wavesplatform.wallet.v1.ui.transactions;

import android.content.Context;
import android.content.Intent;
import android.databinding.Bindable;
import android.support.annotation.VisibleForTesting;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.v1.api.NodeManager;
import com.wavesplatform.wallet.v1.data.datamanagers.TransactionListDataManager;
import com.wavesplatform.wallet.v1.injection.Injector;
import com.wavesplatform.wallet.v1.payload.AssetBalance;
import com.wavesplatform.wallet.v1.payload.IssueTransaction;
import com.wavesplatform.wallet.v1.ui.base.BaseViewModel;
import com.wavesplatform.wallet.v1.util.MoneyUtil;
import com.wavesplatform.wallet.v1.util.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import static com.wavesplatform.wallet.v1.ui.assets.AssetsActivity.KEY_MY_ASSETS_LIST_POSITION;
import static com.wavesplatform.wallet.v1.ui.balance.TransactionsFragment.KEY_TRANSACTION_LIST_POSITION;
import static com.wavesplatform.wallet.v1.ui.transactions.IssueDetailActivity.KEY_INTENT_ACTIONS_ENABLED;

@SuppressWarnings("WeakerAccess")
public class IssueDetailViewModel extends BaseViewModel {

    private IssueDataListener mDataListener;
    @Inject StringUtils mStringUtils;
    @Inject TransactionListDataManager mTransactionListDataManager;

    private Context context;

    @VisibleForTesting
    IssueTransaction mTransaction;
    AssetBalance mAssetBalance;
    boolean actionsEnabled;

    public interface IssueDataListener {

        Intent getPageIntent();

        void pageFinish();

    }

    public IssueDetailViewModel(Context context, IssueDataListener listener) {
        this.context = context;
        Injector.getInstance().getDataManagerComponent().inject(this);
        mDataListener = listener;
    }

    @Override
    public void onViewReady() {
        if (mDataListener.getPageIntent() != null) {
            if (mDataListener.getPageIntent().hasExtra(KEY_TRANSACTION_LIST_POSITION)) {
                mTransaction = (IssueTransaction) mTransactionListDataManager.getTransactionList().get(
                        mDataListener.getPageIntent().getIntExtra(KEY_TRANSACTION_LIST_POSITION, 0));
            } else if (mDataListener.getPageIntent().hasExtra(KEY_MY_ASSETS_LIST_POSITION)) {
                mAssetBalance = NodeManager.get().getAllAssets().get(
                        mDataListener.getPageIntent().getIntExtra(KEY_MY_ASSETS_LIST_POSITION, 0));
                mTransaction = mAssetBalance.issueTransaction;
            }
            actionsEnabled = mDataListener.getPageIntent().getBooleanExtra(KEY_INTENT_ACTIONS_ENABLED, false);
        } else {
            mDataListener.pageFinish();
        }
    }

    @Bindable
    public String getAssetName() {
        return mTransaction.getAssetName();
    }

    @Bindable
    public String getQuantity() {
        return MoneyUtil.getScaledText(mAssetBalance != null ? mAssetBalance.quantity : mTransaction.quantity,
                mTransaction.getDecimals());
    }

    @Bindable
    public String getIdentifier() {
        return mTransaction.id;
    }

    public boolean isAssetReissuable() {
        return mAssetBalance != null ? mAssetBalance.reissuable : mTransaction.reissuable;
    }

    @Bindable
    public String getReissuable() {
        return  isAssetReissuable() ? "Yes" : "No";
    }

    @Bindable
    public String getTransactionFee() {
        return mStringUtils.getString(R.string.transaction_detail_fee) +
                MoneyUtil.getWavesStripZeros(mTransaction.fee) + " WAVES";
    }

    @Bindable
    public String getConfirmationStatus() {
        if (mTransaction.isPending) return mStringUtils.getString(R.string.transaction_detail_pending);
        else return mStringUtils.getString(R.string.transaction_detail_confirmed);
    }

    @Bindable
    public String getTransactionDate() {
        Date date = new Date(mTransaction.timestamp);
        DateFormat dateFormat = SimpleDateFormat.getDateInstance(DateFormat.LONG);
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());
        String dateText = dateFormat.format(date);
        String timeText = timeFormat.format(date);

        return dateText + " @ " + timeText;
    }

    @Bindable
    public String getIssuer() {
        return  mTransaction.sender;
    }

    @Bindable
    public String getIssuerLabel() {
        return null;
    }

    @Bindable
    public String getDescription() {
        return mTransaction.description;
    }

    public boolean isActionsEnabled() {
        return actionsEnabled && isAssetReissuable() ;
    }

    public int getDecimals() {
        return mTransaction.decimals;
    }

    public long getTotalQuantity() {
        return mAssetBalance != null ? mAssetBalance.quantity : -1;
    }
}
