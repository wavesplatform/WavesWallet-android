package com.wavesplatform.sdk.model.request.node

import com.google.gson.annotations.SerializedName

/**
 * Data transaction a entity type.
 */
class DataEntity(key: String, type: String, value: Any) {

    /**
     * Data transaction key
     */
    @SerializedName("key")
    var key: String? = key

    /**
     * Data transaction type can be only "string", "boolean", "integer", "binary"
     */
    @SerializedName("type")
    var type: String? = type

    /**
     * Data transaction value can be string, boolean, Long, and binary string as Base64
     */
    @SerializedName("value")
    var value: Any? = value
}