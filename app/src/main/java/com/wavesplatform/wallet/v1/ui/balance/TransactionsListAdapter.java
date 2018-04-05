package com.wavesplatform.wallet.v1.ui.balance;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.v1.data.datamanagers.AddressBookManager;
import com.wavesplatform.wallet.v1.payload.Transaction;
import com.wavesplatform.wallet.v1.ui.customviews.AutoResizeTextView;
import com.wavesplatform.wallet.v1.util.DateUtil;

import java.util.List;
import java.util.Map;

class TransactionsListAdapter extends RecyclerView.Adapter<TransactionsListAdapter.ViewHolder> implements
    AddressBookManager.AddressBookListener {

    private List<Transaction> mTransactions;
    private DateUtil mDateUtil;
    private TxListClickListener mListClickListener;
    private Map<String, String> mAddressBook;

    TransactionsListAdapter(List<Transaction> transactions,
                            DateUtil dateUtil,
                            Map<String, String> addressBook) {

        mTransactions = transactions;
        mDateUtil = dateUtil;
        mAddressBook = addressBook;
    }

    @Override
    public TransactionsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.txs_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        onBindViewHolder(holder, position);
    }


    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        togglePendingAnimation(holder);
    }

    private void togglePendingAnimation(ViewHolder holder) {
        if (holder.isPendnding) {
            addPendingAnimation(holder.result);
        } else {
            removePendingAnimation(holder.result);
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Context context = holder.touchView.getContext();

        if (mTransactions != null) {
            final Transaction tx = mTransactions.get(position);

            holder.result.setTextColor(Color.WHITE);
            holder.timeSince.setText(mDateUtil.formatted(tx.timestamp));

            String sign = "";
            if (tx.getDirection() == Transaction.RECEIVED) {
                //holder.direction.setColorFilter(ContextCompat.getColor(holder.direction.getContext(), R.color.blockchain_receive_green));
                ///holder.direction.setImageResource(R.drawable.vector_receive);
                holder.result.setBackgroundResource(R.drawable.rounded_view_green);
            } else {
                sign = "\u2013";
                //holder.direction.setColorFilter(ContextCompat.getColor(holder.direction.getContext(), R.color.blockchain_send_red));
                //holder.direction.setImageResource(R.drawable.vector_send);
                holder.result.setBackgroundResource(R.drawable.rounded_view_red);
            }

            Spannable spannable;
            spannable = Spannable.Factory.getInstance().newSpannable(sign + tx.getDisplayAmount());
            spannable.setSpan(
                    new RelativeSizeSpan(0.67f), spannable.length() - tx.getDecimals(), spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            holder.units.setText(tx.getAssetName());
            holder.result.setText(spannable);

            if (tx.getConterParty().isPresent()) {
                holder.counterParty.setVisibility(View.VISIBLE);
                holder.favorite.setVisibility(View.VISIBLE);
                String counterParty = tx.getConterParty().get();
                if (mAddressBook.containsKey(counterParty)) {
                    holder.counterParty.setText(mAddressBook.get(counterParty));
                    holder.favorite.setImageResource(R.drawable.ic_star);
                    holder.favorite.setColorFilter(ContextCompat.getColor(context, R.color.favorite_yellow));
                } else {
                    holder.counterParty.setText(counterParty);
                    holder.counterParty.setMinTextSize(12);
                    holder.favorite.setColorFilter(ContextCompat.getColor(context, R.color.light_grey_text));
                    holder.favorite.setImageResource(R.drawable.ic_not_star);
                }

                holder.favorite.setOnClickListener(v -> {
                    onAddressBookUpdated(v.getContext(), tx);
                });
            } else {
                holder.counterParty.setVisibility(View.INVISIBLE);
                holder.favorite.setVisibility(View.GONE);
            }

            holder.isPendnding = tx.isPending;
            togglePendingAnimation(holder);

            holder.touchView.setOnClickListener(v -> {
                if (mListClickListener != null) mListClickListener.onRowClicked(tx, holder.getAdapterPosition());
            });
        }
    }


    private void addPendingAnimation(View view) {
        final Animation animation = new AlphaAnimation(1, 0);
        animation.setDuration(1000);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        view.startAnimation(animation);
    }

    private void removePendingAnimation(View view) {
        view.clearAnimation();
    }

    @Override
    public int getItemCount() {
        return mTransactions != null ? mTransactions.size() : 0;
    }

    void onTransactionsUpdated(List<Transaction> transactions) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new TransactionDiffUtil(mTransactions, transactions));
        mTransactions = transactions;
        diffResult.dispatchUpdatesTo(this);
    }

    void setTxListClickListener(TxListClickListener listClickListener) {
        mListClickListener = listClickListener;
    }

    private void onAddressBookUpdated(Context context, Transaction tx) {
        if (tx.getConterParty().isPresent()) {
            if (mAddressBook.containsKey(tx.getConterParty().get())) {
                AddressBookManager.createRemoveAddressDialog(context, tx.getConterParty().get(), this).show();
            } else {
                AddressBookManager.createEnterNameDialog(context, tx.getConterParty().get(), this).show();
            }
        };
    }

    @Override
    public void onAddressAdded(String address, String name) {
        mAddressBook.put(address, name);
        notifyDataSetChanged();
    }

    @Override
    public void onAddressRemoved(String address) {
        mAddressBook.remove(address);
        notifyDataSetChanged();
    }

    interface TxListClickListener {
        void onRowClicked(Transaction tx, int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        View touchView;
        TextView result;
        TextView timeSince;
        TextView units;
        AutoResizeTextView counterParty;
        AppCompatImageView favorite;
        boolean isPendnding;

        ViewHolder(View view) {
            super(view);
            touchView = view.findViewById(R.id.tx_touch_view);
            result = (TextView) view.findViewById(R.id.result);
            timeSince = (TextView) view.findViewById(R.id.ts);
            favorite = (AppCompatImageView) view.findViewById(R.id.favorite);
            units = (TextView) view.findViewById(R.id.units);
            counterParty = (AutoResizeTextView) view.findViewById(R.id.counter_party);
        }
    }
}
