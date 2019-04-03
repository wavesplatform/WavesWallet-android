/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.local

enum class LeasingStatus(var status: String) {
    ACTIVE("active"),
    CANCELED("canceled");
}