package com.wavesplatform.wallet.v1.util;

import com.wavesplatform.wallet.v1.payload.AssetBalance;
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MoneyUtil {

    public static BigDecimal ONE_B = new BigDecimal(1000000000);
    public static BigDecimal ONE_M = new BigDecimal(1000000);
    public static BigDecimal ONE_K = new BigDecimal(1000);
    public static char DEFAULT_SEPARATOR_COMMA = ',';
    public static char DEFAULT_SEPARATOR_THIN_SPACE = '\u2009';

    private static MoneyUtil instance = new MoneyUtil();
    private final DecimalFormat wavesFormat;
    private final List<DecimalFormat> formatsMap;

    private MoneyUtil() {
        wavesFormat = createFormatter(8);
        formatsMap = new ArrayList<>();
        for (int i = 0; i <= 8; ++i) {
            formatsMap.add(createFormatter(i));
        }
    }

    public static DecimalFormat createFormatter(int decimals) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        formatter.setMaximumFractionDigits(decimals);
        formatter.setMinimumFractionDigits(decimals);
        formatter.setParseBigDecimal(true);
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(DEFAULT_SEPARATOR_THIN_SPACE);
        formatter.setDecimalFormatSymbols(symbols);
        return formatter;
    }

    public DecimalFormat getFormatter(int decimals) {
        return formatsMap.get(decimals);
    }

    public static MoneyUtil get() {
        return instance;
    }

    public static String getScaledPrice(long amount, int amountDecimals, int priceDecimals) {
        return get().getFormatter(priceDecimals).format(BigDecimal.valueOf(amount, 8 + priceDecimals - amountDecimals));
    }

    public static String getScaledText(long amount, int decimals) {
        try {
            return get().getFormatter(decimals).format(
                    BigDecimal.valueOf(amount, decimals));
        } catch (Exception e) {
            return get().getFormatter(8).format(
                    BigDecimal.valueOf(amount, decimals));
        }
    }

    public static String getTextStripZeros(String amount) {
        if (amount.equals("0.0")) return amount;
        return !amount.contains(".") ? amount : amount.replaceAll("0*$", "").replaceAll("\\.$", "");
    }

    public static String getTextStripZeros(long amount, int decimals) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        formatter.setMaximumFractionDigits(decimals);
        formatter.setMinimumFractionDigits(1);
        formatter.setParseBigDecimal(true);
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(DEFAULT_SEPARATOR_THIN_SPACE);
        formatter.setDecimalFormatSymbols(symbols);
        return formatter.format(
                BigDecimal.valueOf(amount, decimals));
    }

    public static String getFormattedTotal(double amount, int decimals) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        formatter.setMaximumFractionDigits(decimals);
        formatter.setMinimumFractionDigits(0);
        formatter.setParseBigDecimal(true);
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(DEFAULT_SEPARATOR_THIN_SPACE);
        formatter.setDecimalFormatSymbols(symbols);
        return formatter.format(amount);
    }

    public static String getScaledText(long amount, AssetBalance ab) {
        return getScaledText(amount, ab != null ? ab.getDecimals() : 8);
    }

    public static String getScaledText(Long amount, com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance ab) {
        return getScaledText(amount, ab != null ? ab.getDecimals() : 8);
    }

    public static String getScaledText(Long amount, AssetInfo assetInfo) {
        return getScaledText(amount, assetInfo != null ? assetInfo.getPrecision() : 8);
    }

    public static String getDisplayWaves(long amount) {
        return get().wavesFormat.format(BigDecimal.valueOf(amount, 8));
    }

    public static String getWavesStripZeros(long amount) {
        return getTextStripZeros(amount, 8);
    }

    public static long getUnscaledValue(
            String amount, com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance ab) {
        return getUnscaledValue(amount, ab.getDecimals());
    }

    public static long getUnscaledValue(String amount, int decimals) {
        if (amount == null)
            return 0L;
        try {
            Number value = get().getFormatter(decimals).parse(amount);
            if (value instanceof BigDecimal) {
                return ((BigDecimal) value).setScale(decimals,
                        RoundingMode.HALF_EVEN).unscaledValue().longValue();
            } else {
                return 0L;
            }
        } catch (Exception ex) {
            return 0L;
        }
    }
}
