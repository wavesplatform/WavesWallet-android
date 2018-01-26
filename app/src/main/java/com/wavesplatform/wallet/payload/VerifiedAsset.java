package com.wavesplatform.wallet.payload;

/**
 * Created by anonymous on 13.07.17.
 */

public class VerifiedAsset {
    public String assetId;
    public String assetName;

    public VerifiedAsset(String assetId, String assetName) {
        this.assetId = assetId;
        this.assetName = assetName;
    }
}
