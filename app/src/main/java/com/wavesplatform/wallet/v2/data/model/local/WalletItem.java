/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.local;

public class WalletItem {
    public String guid;
    public String name;
    public String address;
    public String publicKey;

    public WalletItem(String guid, String name, String address, String publicKey) {
        this.guid = guid;
        this.name = name;
        this.address = address;
        this.publicKey = publicKey;
    }
}
