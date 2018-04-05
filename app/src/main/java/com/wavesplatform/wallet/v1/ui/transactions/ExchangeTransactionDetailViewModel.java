package com.wavesplatform.wallet.v1.ui.transactions;

import android.content.Context;
import android.content.Intent;
import android.databinding.Bindable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.ContextCompat;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.v1.data.datamanagers.TransactionListDataManager;
import com.wavesplatform.wallet.v1.injection.Injector;
import com.wavesplatform.wallet.v1.payload.ExchangeTransaction;
import com.wavesplatform.wallet.v1.payload.Transaction;
import com.wavesplatform.wallet.v1.ui.base.BaseViewModel;
import com.wavesplatform.wallet.v1.util.MoneyUtil;
import com.wavesplatform.wallet.v1.util.PrefsUtil;
import com.wavesplatform.wallet.v1.util.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import static com.wavesplatform.wallet.v1.ui.balance.TransactionsFragment.KEY_TRANSACTION_LIST_POSITION;

@SuppressWarnings("WeakerAccess")
public class ExchangeTransactionDetailViewModel extends BaseViewModel {

    private DataListener mDataListener;
    @Inject PrefsUtil mPrefsUtil;
    @Inject StringUtils mStringUtils;
    @Inject TransactionListDataManager mTransactionListDataManager;

    private Context context;

    @VisibleForTesting
    ExchangeTransaction mTransaction;

    private String walletName;

    public interface DataListener {

        Intent getPageIntent();

        void pageFinish();

    }

    public ExchangeTransactionDetailViewModel(Context context, DataListener listener) {
        this.context = context;
        Injector.getInstance().getDataManagerComponent().inject(this);
        mDataListener = listener;
    }

    @Override
    public void onViewReady() {
        if (mDataListener.getPageIntent() != null
                && mDataListener.getPageIntent().hasExtra(KEY_TRANSACTION_LIST_POSITION)) {
            int position = mDataListener.getPageIntent().getIntExtra(KEY_TRANSACTION_LIST_POSITION, -1);
            if (position == -1) {
                mDataListener.pageFinish();
            } else {
                mTransaction = (ExchangeTransaction) mTransactionListDataManager.getTransactionList().get(position);
            }
            walletName = mPrefsUtil.getValue(PrefsUtil.KEY_WALLET_NAME, "");
        } else {
            mDataListener.pageFinish();
        }
        updateUiFromTransaction();
    }


    @Bindable
    public String getTransactionType() {
        switch (mTransaction.getDirection()) {
            case Transaction.RECEIVED:
                return mStringUtils.getString(R.string.Buy);
            case Transaction.SENT:
                return mStringUtils.getString(R.string.Sell);
        }
        return null;
    }

    private void updateUiFromTransaction() {
    }

    @Bindable
    public String getTransactionAmount() {
        return MoneyUtil.getTextStripZeros(mTransaction.amount, mTransaction.getDecimals()) + " " + mTransaction.getAmountAssetName();
    }


    @Bindable
    public int getTransactionColor() {
        switch (mTransaction.getDirection()) {
            case Transaction.RECEIVED:
                return ContextCompat.getColor(context, R.color.blockchain_receive_green);
            case Transaction.SENT:
                return ContextCompat.getColor(context, R.color.blockchain_send_red);
        }
        return R.color.blockchain_transfer_blue;
    }

    @Bindable
    public String getTransactionFee() {
        return MoneyUtil.getWavesStripZeros(mTransaction.getTransactionFee()) + " WAVES";
    }


    @Bindable
    public String getPrice() {
        return mTransaction.getDisplayPrice();
    }

    @Bindable
    public String getExchangeResult() {

        return mStringUtils.getString(R.string.exchange_transaction_details_exchange_result) + " " +
                MoneyUtil.getTextStripZeros(mTransaction.getDisplayPriceAmount()) + " " + mTransaction.getPriceAssetName();
    }

    @Bindable
    public String getConfirmationStatus() {
        if (mTransaction.isPending) return mStringUtils.getString(R.string.transaction_detail_pending);
        else return mStringUtils.getString(R.string.transaction_detail_confirmed);
    }

    @Bindable
    public String getAmountAssetName() {
        return mTransaction.getAmountAssetName();
    }

    @Bindable
    public String getPriceAssetName() {
        return mTransaction.getPriceAssetName();
    }

    @Bindable
    public String getAmountAssetId() {
        return mTransaction.getAmountAssetId();//mTransaction.assetId;
    }

    @Bindable
    public String getPriceAssetId() {
        return mTransaction.getPriceAssetId();//mTransaction.assetId;
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
    public boolean isExchangeTransaction() {
        return mTransaction instanceof ExchangeTransaction;
    }
}
