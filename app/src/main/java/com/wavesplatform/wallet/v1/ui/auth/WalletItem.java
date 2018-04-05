package com.wavesplatform.wallet.v1.ui.auth;

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
