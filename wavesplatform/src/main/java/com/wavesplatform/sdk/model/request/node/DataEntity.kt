package com.wavesplatform.sdk.model.request.node

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
open class DataEntity : Parcelable {

    @Parcelize
    class Text : DataEntity(), Parcelable {

        @SerializedName("key")
        var key: String? = null

        @SerializedName("type")
        var type: String? = "string"

        @SerializedName("value")
        var value: String? = null
    }

    @Parcelize
    class Digit : DataEntity(), Parcelable {

        @SerializedName("key")
        var key: String? = null

        @SerializedName("type")
        var type: String? = "integer"

        @SerializedName("value")
        var value: Long? = null
    }

    @Parcelize
    class Bool : DataEntity(), Parcelable {

        @SerializedName("key")
        var key: String? = null

        @SerializedName("type")
        var type: String? = "boolean"

        @SerializedName("value")
        var value: Boolean? = null
    }

    @Parcelize
    class Binary : DataEntity(), Parcelable {

        @SerializedName("key")
        var key: String? = null

        @SerializedName("type")
        var type: String? = "binary"

        @SerializedName("value")
        var value: ByteArray? = null
    }
}