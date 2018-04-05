package com.wavesplatform.wallet.v1.ui.assets;

import android.support.annotation.NonNull;

import com.wavesplatform.wallet.v1.api.NodeManager;
import com.wavesplatform.wallet.v1.payload.AssetBalance;
import com.wavesplatform.wallet.v1.util.PrefsUtil;

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
