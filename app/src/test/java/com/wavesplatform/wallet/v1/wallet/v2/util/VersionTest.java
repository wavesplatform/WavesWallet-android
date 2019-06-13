package com.wavesplatform.wallet.v1.wallet.v2.util;

import com.wavesplatform.wallet.v2.util.Version;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VersionTest {

    @Test
    public void checkVersions() {
        assertTrue(Version.Companion.needAppUpdate("2.4.0", "2.3.1"));
        assertTrue(Version.Companion.needAppUpdate("2.4.0", "2.5.0"));
        assertFalse(Version.Companion.needAppUpdate("4.4.0", "2.3.1"));
    }
}
