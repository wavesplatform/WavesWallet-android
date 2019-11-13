package com.wavesplatform.wallet.v2.data.model.service.configs

import com.google.gson.annotations.SerializedName

data class DevConfigResponse(
        @SerializedName("force_update_version")
        var forceUpdateVersion: String?,
        @SerializedName("service_available")
        var serviceAvailable: Boolean? = true,
        @SerializedName("matcher_swap_timestamp")
        var matcherSwapTimestamp: Long? = 1575288000L
)