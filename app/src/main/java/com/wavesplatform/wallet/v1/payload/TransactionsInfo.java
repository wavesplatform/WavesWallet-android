/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v1.payload;

public class TransactionsInfo {
    public int type;
    public String id;
    public String sender;
    public String senderPublicKey;
    public Long fee;
    public String timestamp;
    public String signature;
    public String assetId;
    public String name;
    public String description;
    public Long quantity;
    public int decimals;
    public boolean reissuable;
    public Long height;

    public TransactionsInfo(String assetId, String name, int decimals) {
        this.assetId = assetId;
        this.name = name;
        this.decimals = decimals;
    }
}
