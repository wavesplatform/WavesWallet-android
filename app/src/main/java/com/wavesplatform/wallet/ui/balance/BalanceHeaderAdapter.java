package com.wavesplatform.wallet.ui.balance;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.databinding.ItemBalanceAccountDropdownBinding;
import com.wavesplatform.wallet.databinding.SpinnerBalanceHeaderBinding;
import com.wavesplatform.wallet.ui.assets.ItemAccount;

import java.util.List;

class BalanceHeaderAdapter extends ArrayAdapter<ItemAccount> {

    BalanceHeaderAdapter(Context context,
                         int textViewResourceId,
                         List<ItemAccount> accountList) {
        super(context, textViewResourceId, accountList);
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
            ItemBalanceAccountDropdownBinding binding = DataBindingUtil.inflate(
                    LayoutInflater.from(getContext()),
                    R.layout.item_balance_account_dropdown,
                    parent,
                    false);

            ItemAccount item = getItem(position);

            binding.accountName.setText(item.label);

            if (item.accountObject != null) {
                binding.balance.setText(item.accountObject.getDisplayBalance());
                binding.balance.setVisibility(View.VISIBLE);
            } else {
                binding.balance.setVisibility(View.GONE);
            }

            return binding.getRoot();

        } else {
            SpinnerBalanceHeaderBinding binding = DataBindingUtil.inflate(
                    LayoutInflater.from(getContext()),
                    R.layout.spinner_balance_header,
                    parent,
                    false);

            ItemAccount item = getItem(position);

            binding.text.setText(item.label);
            return binding.getRoot();
        }
    }
}
