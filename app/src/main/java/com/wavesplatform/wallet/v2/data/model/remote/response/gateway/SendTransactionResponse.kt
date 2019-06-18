/*
 * Created by Eduard Zaydel on 17/6/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.remote.response.gateway


import com.google.gson.annotations.SerializedName

data class SendTransactionResponse(
        @SerializedName("processId")
        var processId: String,
        @SerializedName("status")
        var status: String
)