/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v1.payload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssetBalances {
    public String address;
    public List<AssetBalance> balances = new ArrayList<>();

    private Map<String, String> assetId2Name = null;

    public String getAssetName(String assetId) {
        if (assetId2Name == null) {
            assetId2Name = new HashMap<>();
            for (AssetBalance ab : balances) {
                assetId2Name.put(ab.assetId, ab.issueTransaction.name);
            }
        }

        return assetId2Name.get(assetId);
    }
}
