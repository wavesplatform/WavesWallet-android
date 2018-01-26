package com.wavesplatform.wallet.request;

public enum OrderType {
    buy(0, "buy"),
    sell(1, "sell");

    private String name;
    private int value;

    OrderType(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public byte[] toBytes() {
        return new byte[]{(byte) value};
    }

    public static OrderType fromString(String text) {
        if (buy.name.compareToIgnoreCase(text) == 0) {
            return buy;
        } else if (sell.name.compareToIgnoreCase(text) == 0) {
            return sell;
        }

        return null;
    }

    @Override
    public String toString() {
        return name;
    }
}
