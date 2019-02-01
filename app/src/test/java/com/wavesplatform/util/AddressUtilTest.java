package com.wavesplatform.util;

import com.wavesplatform.wallet.v2.util.AddressUtil;

import org.junit.Test;

import java.math.BigDecimal;
import java.net.URI;

import static org.junit.Assert.assertEquals;

public class AddressUtilTest {
    @Test
    public void validateAddress() {
        //assertTrue(AddressUtil.isValidAddress("3NCwEeAeVKdPySfsTeAoroPHDUg54mSDY5w"));
        BigDecimal d = new BigDecimal("92233720368.54775807");
        assertEquals(Long.MAX_VALUE, d.unscaledValue().longValue());
    }

    @Test
    public void parseUri() {
        String str = AddressUtil.WAVES_PREFIX + "3NCwEeAeVKdPySfsTeAoroPHDUg54mSDY5w?asset=123&amount=5000";
        URI uri = URI.create(str);
        uri.getHost();
    }
}