/*
 * Created by Eduard Zaydel on 15/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.service.cofigs

import com.google.gson.annotations.SerializedName

data class LastAppVersionResponse(
        @SerializedName("last_version")
        var lastVersion: String
)