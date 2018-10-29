package com.wavesplatform.util;

import com.wavesplatform.wallet.ui.auth.EnvironmentManager;
import com.wavesplatform.wallet.util.AddressUtil;
import com.wavesplatform.wallet.util.PrefsUtil;

import org.junit.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.net.URI;

import static org.junit.Assert.*;

public class AddressUtilTest {

    private static final String ADDR = "3NCwEeAeVKdPySfsTeAoroPHDUg54mSDY5w";

    @Test
    public void validateAddress() {
        PrefsUtil prefsUtil = Mockito.mock(PrefsUtil.class);
        Mockito.when(prefsUtil.getEnvironment()).thenReturn("env_testnet");
        EnvironmentManager.init(prefsUtil, null);
        assertTrue(AddressUtil.isValidAddress(ADDR));
        BigDecimal d = new BigDecimal("92233720368.54775807");
        assertEquals(Long.MAX_VALUE, d.unscaledValue().longValue());
    }

    @Test
    public void parseUri() {
        String str = "waves://" + ADDR + "?asset=123&amount=5000";
        URI uri = URI.create(str);
        assertEquals(uri.getHost(), ADDR);
    }
}