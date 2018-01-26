package com.wavesplatform.wallet.ui.send;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.databinding.SpinnerAddressBinding;

import java.util.List;

public class AddressBookAdapter extends ArrayAdapter<AddressItem> {

    public AddressBookAdapter(Context context,
                              List<AddressItem> addressList) {
        super(context, R.layout.spinner_address, addressList);
    }

    public void updateData(List<AddressItem> addressList) {
        clear();
        addAll(addressList);
        notifyDataSetChanged();
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, parent);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, parent);
    }

    private View getCustomView(int position, ViewGroup parent) {

        SpinnerAddressBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(getContext()),
                R.layout.spinner_address,
                parent,
                false);

        AddressItem item = getItem(position);
        binding.address.setText(item.address);
        binding.label.setText(item.name);

        return binding.getRoot();
    }
}
