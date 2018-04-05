package com.wavesplatform.wallet.v1.ui.assets;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.wavesplatform.wallet.R;

import java.util.ArrayList;

public class AssetsAdapter extends RecyclerView.Adapter<AssetsAdapter.ViewHolder> {

    private static final int TYPE_IMPORTED_HEADER = -1;
    private static final int TYPE_CREATE_NEW_WALLET_BUTTON = -2;
    private static final int TYPE_IMPORT_ADDRESS_BUTTON = -3;
    private ArrayList<AssetItem> items;
    private AccountHeadersListener listener;

    public AssetsAdapter(ArrayList<AssetItem> myAssetItems) {
        items = myAssetItems;
    }

    @Override
    public AssetsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_accounts_row, parent, false);

        if (viewType == TYPE_IMPORTED_HEADER) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_accounts_row_header, parent, false);

        } else if (viewType == TYPE_CREATE_NEW_WALLET_BUTTON || viewType == TYPE_IMPORT_ADDRESS_BUTTON) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_accounts_row_buttons, parent, false);
        }

        return new ViewHolder(v);
    }

    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder.isPending) {
            addPendingAnimation(holder.cardView);
        } else {
            removePendingAnimation(holder.cardView);
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
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Imported Items header
        if (holder.getItemViewType() == TYPE_IMPORTED_HEADER)
            return;

        // Create new wallet button
        if (holder.getItemViewType() == TYPE_CREATE_NEW_WALLET_BUTTON) {
            holder.description.setText(R.string.create_new);

            holder.cardView.setOnClickListener(v -> {
                if (listener != null) listener.onCreateNewClicked();
            });
            return;
        }

        AssetItem assetItem = items.get(position);

        // Normal account view
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) listener.onCardClicked(assetItem.getCorrectPosition());
        });

        holder.title.setText(assetItem.getLabel());

        if (assetItem.isPending) {
            holder.isPending = true;
            addPendingAnimation(holder.cardView);
        } else {
            holder.isPending = false;
            removePendingAnimation(holder.cardView);
        }

        if (!assetItem.getAddress().isEmpty()) {
            holder.address.setVisibility(View.VISIBLE);
            holder.address.setText(assetItem.getAddress());
        } else {
            holder.address.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return !items.isEmpty() ? items.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        String title = items.get(position).getLabel();

        if (title.equals(AssetsActivity.IMPORT_ADDRESS)) {
            return TYPE_IMPORT_ADDRESS_BUTTON;
        } else if (title.equals(AssetsActivity.CREATE_NEW)) {
            return TYPE_CREATE_NEW_WALLET_BUTTON;
        }

        return 0;
    }

    public void setAccountHeaderListener(AccountHeadersListener accountHeadersListener) {
        listener = accountHeadersListener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        TextView title;
        TextView address;
        ImageView icon;
        TextView amount;
        TextView tag;
        TextView description;
        boolean isPending;

        ViewHolder(View view) {
            super(view);
            cardView = (CardView) view.findViewById(R.id.card_view);
            title = (TextView) view.findViewById(R.id.my_account_row_label);
            address = (TextView) view.findViewById(R.id.my_account_row_address);
            icon = (ImageView) view.findViewById(R.id.my_account_row_icon);
            amount = (TextView) view.findViewById(R.id.my_account_row_amount);
            tag = (TextView) view.findViewById(R.id.my_account_row_tag);
            description = (TextView) view.findViewById(R.id.description);
        }
    }

    public interface AccountHeadersListener {

        void onCreateNewClicked();

        void onCardClicked(int correctedPosition);
    }
}