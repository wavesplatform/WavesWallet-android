package com.wavesplatform.wallet.ui.transactions;

import android.content.Context;
import android.content.Intent;
import android.databinding.Bindable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.ContextCompat;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.data.datamanagers.TransactionListDataManager;
import com.wavesplatform.wallet.injection.Injector;
import com.wavesplatform.wallet.payload.Transaction;
import com.wavesplatform.wallet.ui.base.BaseViewModel;
import com.wavesplatform.wallet.util.MoneyUtil;
import com.wavesplatform.wallet.util.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import static com.wavesplatform.wallet.ui.balance.TransactionsFragment.KEY_TRANSACTION_LIST_POSITION;

@SuppressWarnings("WeakerAccess")
public class UnknownDetailViewModel extends BaseViewModel {

    private DataListener mDataListener;
    @Inject StringUtils mStringUtils;
    @Inject TransactionListDataManager mTransactionListDataManager;

    private Context context;

    @VisibleForTesting
    Transaction mTransaction;

    public interface DataListener {

        Intent getPageIntent();

        void pageFinish();

    }

    public UnknownDetailViewModel(Context context, DataListener listener) {
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
                mTransaction = mTransactionListDataManager.getTransactionList().get(position);
            }
        } else {
            mDataListener.pageFinish();
        }
        updateUiFromTransaction();
    }


    @Bindable
    public String getTransactionType() {
        switch (mTransaction.getDirection()) {
            case Transaction.RECEIVED:
                return mStringUtils.getString(R.string.RECEIVED);
            case Transaction.SENT:
                return mStringUtils.getString(R.string.SENT);
        }
        return null;
    }

    private void updateUiFromTransaction() {
    }

    @Bindable
    public String getTransactionAmount() {
        return MoneyUtil.getTextStripZeros(mTransaction.amount, mTransaction.getDecimals()) + " " + mTransaction.getAssetName();
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
        return mStringUtils.getString(R.string.transaction_detail_fee) +
                MoneyUtil.getWavesStripZeros(mTransaction.fee) + " WAVES";
    }

    @Bindable
    public String getConfirmationStatus() {
        if (mTransaction.isPending) return mStringUtils.getString(R.string.transaction_detail_pending);
        else return mStringUtils.getString(R.string.transaction_detail_confirmed);
    }

    @Bindable
    public String getAssetName() {
        return mTransaction.getAssetName();
    }

    @Bindable
    public String getFromAddressLabel() {
        return null;
    }

    @Bindable
    public String getFromAddress() {
        return mTransaction.sender;
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

}
