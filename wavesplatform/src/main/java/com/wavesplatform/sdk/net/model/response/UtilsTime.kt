/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.net.model.response

import com.google.gson.annotations.SerializedName

data class UtilsTime(
        @SerializedName("system")
        var system: Long = 0,
        @SerializedName("NTP")
        var ntp: Long = 0)