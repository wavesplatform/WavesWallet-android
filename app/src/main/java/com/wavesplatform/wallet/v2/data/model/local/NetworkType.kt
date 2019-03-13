package com.wavesplatform.wallet.v2.data.model.local


enum class NetworkType(val type: Int, val typeName: String) {
    NETWORK_NONE(0, "none"),
    NETWORK_MOBILE(1, "mobile"),
    NETWORK_WIFI(2, "wifi");

    companion object {
        fun getByType(type: Int): NetworkType? {
            return NetworkType.values().firstOrNull { it.type == type }
        }
    }
}

