/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v1.util;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Shorts;
import com.wavesplatform.wallet.v1.crypto.Base58;

public class SignUtil {
    public static byte[] arrayWithSize(String s) throws Base58.InvalidBase58 {
        if (s != null && s.length() > 0) {
            byte[] b = Base58.decode(s);
            return Bytes.concat(Shorts.toByteArray((short) b.length), b);
        } else {
            return Shorts.toByteArray((short) 0);
        }
    }

    public static byte[] arrayOption(String o) throws Base58.InvalidBase58 {
        return org.apache.commons.lang3.StringUtils.isEmpty(o) ?
                new byte[]{0} : Bytes.concat(new byte[]{1}, Base58.decode(o));
    }
}
