package com.wavesplatform.wallet.v1.util;

import org.junit.Test;

import static com.wavesplatform.wallet.v2.util.ExtensionsKt.getScaledAmount;
import static org.junit.Assert.assertEquals;

public class MoneyUtilTest {

    @Test
    public void checkBalances() {
        assertEquals("0",
                getScaledAmount(0L, 2));
        assertEquals("1",
                getScaledAmount(100L, 2));
        assertEquals("123",
                getScaledAmount(12300L, 2));
        assertEquals("1.2k",
                getScaledAmount(123400L, 2));
        assertEquals("1.3k",
                getScaledAmount(128400L, 2));
        assertEquals("12.3k",
                getScaledAmount(1234500L, 2));
        assertEquals("123.5k",
                getScaledAmount(12345600L, 2));
        assertEquals("1.2M",
                getScaledAmount(123456700L, 2));
        assertEquals("12.3M",
                getScaledAmount(1234567800L, 2));
        assertEquals("123.5M",
                getScaledAmount(12345678900L, 2));
        assertEquals("1234.6M",
                getScaledAmount(123456789800L, 2));
        assertEquals("0.12",
                getScaledAmount(12L, 2));
        assertEquals("1.23",
                getScaledAmount(123L, 2));
        assertEquals("123.45",
                getScaledAmount(12345L, 2));
        assertEquals("-123.45",
                getScaledAmount(-12345L, 2));
        assertEquals("-1.2k",
                getScaledAmount(-123456L, 2));
        assertEquals("-1.2M",
                getScaledAmount(-123456700L, 2));
        assertEquals("9.87654321",
                getScaledAmount(987654321L, 8));
        assertEquals("-9.87654321",
                getScaledAmount(-987654321L, 8));
        assertEquals("9.00000001",
                getScaledAmount(900000001L, 8));
    }
}
