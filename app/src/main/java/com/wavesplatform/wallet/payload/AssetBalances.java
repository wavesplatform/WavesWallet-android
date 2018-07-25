package com.wavesplatform.wallet.payload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public Set<String> getAllAssets() {
        Set<String> allAssets = new HashSet<>();
        for (AssetBalance ab : balances) {
            allAssets.add(ab.assetId);
        }
        return allAssets;
    }
}
