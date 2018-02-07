package com.wavesplatform.wallet.util;

/**
 * Created by drblast on 05.02.18.
 */

public class StringBuilderPlus {
    private StringBuilder sb;

    public StringBuilderPlus() {
        sb = new StringBuilder();
    }

    public void append(String str) {
        sb.append(str != null ? str : "");
    }

    public void appendLine(String title, String value) {
        append(title);
        append(value);
        append(System.getProperty("line.separator"));

    }

    public void appendLine(String str) {
        sb.append(str != null ? str : "").append(System.getProperty("line.separator"));
    }

    public String toString() {
        return sb.toString();
    }
}

