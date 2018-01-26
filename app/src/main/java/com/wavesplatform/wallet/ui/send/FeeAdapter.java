package com.wavesplatform.wallet.ui.send;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.databinding.ItemAssetAccountBinding;

import java.util.Arrays;
import java.util.List;

public class FeeAdapter extends ArrayAdapter<FeeItem> {
    public FeeAdapter(Context context) {
        super(context, R.layout.spinner_item, getPredefinedFees(context));
    }

    public static List<FeeItem> getPredefinedFees(Context context) {
        return Arrays.asList(
                new FeeItem(context.getString(R.string.fee_economic), 100000L),
                new FeeItem(context.getString(R.string.fee_standard), 150000L),
                new FeeItem(context.getString(R.string.fee_premium), 200000L));
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, parent);
    }

    private View getCustomView(int position, ViewGroup parent) {
        ItemAssetAccountBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(getContext()),
                R.layout.item_asset_account,
                parent,
                false);

        FeeItem item = getItem(position);
        binding.tvLabel.setText(item.name);
        binding.tvBalance.setText(item.fee);

        return binding.getRoot();
    }
}
