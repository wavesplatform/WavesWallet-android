package com.wavesplatform.wallet.ui.auth;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.MultiSelectorBindingHolder;
import com.bignerdranch.android.multiselector.SelectableHolder;
import com.wavesplatform.wallet.R;

import java.util.ArrayList;

class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.ViewHolder> {

    private ArrayList<WalletItem> items;
    private WalletCardListener listener;
    private MultiSelector mMultiSelector;

    WalletAdapter(ArrayList<WalletItem> items, MultiSelector multiSelector) {
        this.items = items;
        this.mMultiSelector = multiSelector;
    }

    public void removeWallet(int index) {
        if (index < items.size()) {
            items.remove(index);
            notifyItemRemoved(index);
        }
    }

    @Override
    public WalletAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_wallets_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        WalletItem item = items.get(position);

        holder.bindItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    void setWalletCardListener(WalletCardListener listener) {
        this.listener = listener;
    }

    class ViewHolder extends MultiSelectorBindingHolder implements SelectableHolder, View.OnClickListener, View.OnLongClickListener {

        CardView cardView;
        TextView name;
        TextView address;
        ImageView iconAccount;

        WalletItem item;

        ViewHolder(View view) {
            super(view, mMultiSelector);
            cardView = (CardView) view.findViewById(R.id.card_view);
            name = (TextView) view.findViewById(R.id.my_wallet_name);
            address = (TextView) view.findViewById(R.id.my_wallet_address);
            iconAccount = (ImageView) view.findViewById(R.id.my_account_row_icon);

            itemView.setOnClickListener(this);
            itemView.setLongClickable(true);
            itemView.setOnLongClickListener(this);
        }

        void bindItem(WalletItem item) {
            this.item = item;
            name.setText(item.name);
            address.setText(item.address);


        }

        @Override
        public void onClick(View v) {

            if (item == null) {
                return;
            }

            if (!mMultiSelector.tapSelection(this)) {
                if (listener != null)
                    listener.onCardClicked(getAdapterPosition());
            }

        }

        @Override
        public boolean onLongClick(View v) {
            if (listener != null)
                listener.onCardLongClicked(getAdapterPosition());
            mMultiSelector.setSelected(this, true);
            return true;
        }

        @Override
        public void setSelectable(boolean b) {
        }

        @Override
        public boolean isSelectable() {
            return true;
        }

        @Override
        public void setActivated(boolean b) {
            itemView.setActivated(b);
            if (b) {
                iconAccount.setImageResource(R.drawable.ic_check_black_24dp);
            } else {
                iconAccount.setImageResource(R.drawable.icon_accounthd);
            }
        }

        @Override
        public boolean isActivated() {
            return  itemView.isActivated();
        }
    }

    interface WalletCardListener {
        void onCardClicked(int position);
        void onCardLongClicked(int position);
    }
}