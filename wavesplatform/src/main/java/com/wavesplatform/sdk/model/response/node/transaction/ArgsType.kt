package com.wavesplatform.sdk.model.response.node.transaction

import com.google.gson.annotations.SerializedName

enum class ArgsType(val value: String) {
    @SerializedName("boolean")
    VALUE_BOOL("boolean"),

    @SerializedName("integer")
    VALUE_INTEGER("integer"),

    @SerializedName("string")
    VALUE_STRING("string"),

    @SerializedName("binary")
    VALUE_BINARY("binary")
}