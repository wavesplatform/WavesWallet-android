package com.wavesplatform.sdk.net.model.response

import com.google.gson.annotations.SerializedName

data class Height(@SerializedName("height") var height: Int = 0)