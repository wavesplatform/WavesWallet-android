/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v1.payload;

import java.util.ArrayList;

public class Markets {
    public String matcherPublicKey;
    public ArrayList<Market> markets = new ArrayList<>();
}
