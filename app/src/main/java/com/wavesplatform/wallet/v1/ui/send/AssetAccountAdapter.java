package com.wavesplatform.wallet.v1.ui.send;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.databinding.ItemAssetAccountBinding;
import com.wavesplatform.wallet.databinding.SpinnerItemBinding;
import com.wavesplatform.wallet.v1.payload.AssetBalance;
import com.wavesplatform.wallet.v1.ui.assets.ItemAccount;

import java.util.List;

public class AssetAccountAdapter extends ArrayAdapter<ItemAccount> {

    public AssetAccountAdapter(Context context,
                               int textViewResourceId,
                               List<ItemAccount> accountList) {
        super(context, textViewResourceId, accountList);
    }

    public void updateData(List<ItemAccount> accountList) {
        clear();
        addAll(accountList);
        notifyDataSetChanged();
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, parent, true);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, parent, false);
    }

    private View getCustomView(int position, ViewGroup parent, boolean isDropdownView) {

        if (isDropdownView) {
            ItemAssetAccountBinding binding = DataBindingUtil.inflate(
                    LayoutInflater.from(getContext()),
                    R.layout.item_asset_account,
                    parent,
                    false);

            ItemAccount item = getItem(position);

            binding.tvLabel.setText(item.label);

            if (item.accountObject instanceof AssetBalance) {
                AssetBalance ab = item.accountObject;
                binding.tvBalance.setText(ab.getDisplayBalance());
            }

            return binding.getRoot();

        } else {
            SpinnerItemBinding binding = DataBindingUtil.inflate(
                    LayoutInflater.from(getContext()),
                    R.layout.spinner_item,
                    parent,
                    false);

            ItemAccount item = getItem(position);
            binding.text.setText(item.label);

            return binding.getRoot();
        }
    }
}
