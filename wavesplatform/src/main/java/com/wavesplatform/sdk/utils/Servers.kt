package com.wavesplatform.sdk.utils

import com.google.gson.annotations.SerializedName
import pers.victor.ext.currentTimeMillis

data class Servers(
        @SerializedName("nodeUrl") var nodeUrl: String = "",
        @SerializedName("dataUrl") var dataUrl: String = "",
        @SerializedName("matcherUrl") var matcherUrl: String = "",
        @SerializedName("netCode") var netCode: Byte = 'W'.toByte(),
        @SerializedName("timeCorrection") var timeCorrection: Long = 0L
) {

    fun getTime(): Long {
        return currentTimeMillis + timeCorrection
    }

    companion object {

        val DEFAULT = Servers(
                nodeUrl = Constants.URL_NODE,
                dataUrl = Constants.URL_DATA,
                matcherUrl = Constants.URL_MATCHER,
                netCode = 'W'.toByte(),
                timeCorrection = 0L
        )
    }
}