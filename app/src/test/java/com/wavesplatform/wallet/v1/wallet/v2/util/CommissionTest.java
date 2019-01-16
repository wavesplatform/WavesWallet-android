package com.wavesplatform.wallet.v1.wallet.v2.util;

import org.junit.Test;

import static com.wavesplatform.wallet.v2.util.TransactionUtil.Companion;
import static org.junit.Assert.assertEquals;

public class CommissionTest {

    @Test
    public void checkCommissions() {
        assertEquals(0, Companion.getCommission(4));
    }
}
