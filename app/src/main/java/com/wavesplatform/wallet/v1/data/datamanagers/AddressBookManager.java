package com.wavesplatform.wallet.v1.data.datamanagers;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputFilter;
import android.text.InputType;
import android.widget.Button;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.v1.injection.Injector;
import com.wavesplatform.wallet.v1.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.v1.ui.send.AddressItem;
import com.wavesplatform.wallet.v1.util.PrefsUtil;
import com.wavesplatform.wallet.v1.util.StringUtils;
import com.wavesplatform.wallet.v1.util.ViewUtils;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class AddressBookManager {
    private static AddressBookManager instance;

    @Inject
    protected PrefsUtil prefsUtil;

    private LinkedHashMap<String, String> addressToNameMap = new LinkedHashMap<>();

    public static AlertDialog createEnterNameDialog(Context context, final String address,
                                                    final AddressBookListener listener) {
        AppCompatEditText editText = new AppCompatEditText(context);
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
        editText.setHint(R.string.enter_address_name);
        editText.setMaxLines(20);

        int maxLength = 20;
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);
        editText.setFilters(fArray);

        final AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.AlertDialogStyle)
                .setTitle(R.string.add_to_address_book)
                .setView(ViewUtils.getAlertDialogEditTextLayout(context, editText))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, null).create();

        alertDialog.setOnShowListener(dialog -> {
            Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            b.setOnClickListener(v ->  {
                String name = editText.getText().toString().trim();
                if (!StringUtils.isValidName(name)) {
                    ToastCustom.makeText(context, context.getString(R.string.invalid_wallet_name), ToastCustom.LENGTH_LONG, ToastCustom.TYPE_ERROR);
                } else {
                    get().add(address, name);
                    ToastCustom.makeText(context, context.getString(R.string.favorite_save_ok), ToastCustom.LENGTH_LONG, ToastCustom.TYPE_OK);
                    listener.onAddressAdded(address, name);
                    dialog.dismiss();
                }
            });
        });

        return alertDialog;
    }

    public static AlertDialog createRemoveAddressDialog(Context context, final String address,
                                                    final AddressBookListener listener) {

        final AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.AlertDialogStyle)
                .setTitle(R.string.remove_from_address_book)
                .setMessage(R.string.ask_remove_address)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    get().remove(address);
                    listener.onAddressRemoved(address);
                }).create();

        return alertDialog;
    }

    private AddressBookManager() {
        Injector.getInstance().getDataManagerComponent().inject(this);
        String[] addresses = prefsUtil.getValueList(PrefsUtil.KEY_AB_ADDRESSES);
        String[] names = prefsUtil.getValueList(PrefsUtil.KEY_AB_NAMES);
        for (int i = 0; i < addresses.length; ++i) {
            addressToNameMap.put(addresses[i], names[i]);
        }
    }

    public static AddressBookManager get() {
        if (instance == null)
            instance = new AddressBookManager();

        return instance;
    }

    public void add(String address, String name) {
        if (!addressToNameMap.containsKey(address)) {
            addressToNameMap.put(address, name);
            prefsUtil.addListValue(PrefsUtil.KEY_AB_ADDRESSES, address);
            prefsUtil.addListValue(PrefsUtil.KEY_AB_NAMES, name);
        }
    }

    public void remove(String address) {
        if (addressToNameMap.containsKey(address)) {
            addressToNameMap.remove(address);
            String[] addresses = prefsUtil.getValueList(PrefsUtil.KEY_AB_ADDRESSES);
            String[] names = prefsUtil.getValueList(PrefsUtil.KEY_AB_NAMES);
            int index = ArrayUtils.indexOf(addresses, address);
            if (index >= 0) {
                prefsUtil.removeListValue(PrefsUtil.KEY_AB_ADDRESSES, index);
                prefsUtil.removeListValue(PrefsUtil.KEY_AB_NAMES, index);
            }
        }
    }

    public Map<String, String> getAll() {
        return addressToNameMap;
    }

    public String addressToLabel(String address) {
        return addressToNameMap.get(address);
    }

    public List<AddressItem> getAllList() {
        ArrayList<AddressItem> items = new ArrayList<>();
        for (Map.Entry<String, String> e : addressToNameMap.entrySet()) {
            items.add(new AddressItem(e.getValue(), e.getKey()));
        }
        return items;
    }

    public interface AddressBookListener {
        void onAddressAdded(String address, String name);
        void onAddressRemoved(String address);
    }
}
