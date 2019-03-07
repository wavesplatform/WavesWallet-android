package com.wavesplatform.sdk.model.response

import com.google.gson.annotations.SerializedName

data class UtilsTime(
        @SerializedName("system")
        var system: Long = 0,
        @SerializedName("NTP")
        var ntp: Long = 0)