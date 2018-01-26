package com.wavesplatform.wallet.ui.assets;

import android.support.annotation.NonNull;

import com.wavesplatform.wallet.api.NodeManager;
import com.wavesplatform.wallet.payload.AssetBalance;
import com.wavesplatform.wallet.util.PrefsUtil;

import java.util.ArrayList;
import java.util.List;

public class AssetsHelper {

    public AssetsHelper(PrefsUtil prefsUtil) {
    }

    @NonNull
    public List<ItemAccount> getAccountItems() {
        List<ItemAccount> accountList = new ArrayList<>();

        if (NodeManager.get() == null | NodeManager.get().assetBalances == null | NodeManager.get().assetBalances.balances == null) return accountList;

        for (AssetBalance ab : NodeManager.get().assetBalances.balances) {
            ItemAccount itemAccount = new ItemAccount(
                    ab.issueTransaction.name,
                    ab.getDisplayBalance(),
                    ab.balance,
                    ab);
            accountList.add(itemAccount);
        }

        return accountList;
    }

}
