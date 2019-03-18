package com.wavesplatform.wallet.v2.data.model.db

import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.net.model.response.Data
import com.wavesplatform.sdk.utils.notNull
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.annotations.RealmClass

@RealmClass
open class DataDb(
        @SerializedName("key") var key: String = "",
        @SerializedName("type") var type: String = "",
        @SerializedName("value") var value: String = ""
) : RealmModel {

    constructor(data: Data?) : this() {
        data.notNull {
            this.key = it.key
            this.type = it.type
            this.value = it.value
        }
    }

    fun convertFromDb(): Data {
        return Data(
                key = this.key,
                type = this.type,
                value = this.value)
    }

    companion object {

        fun convertToDb(data: List<Data>): RealmList<DataDb> {
            val list = RealmList<DataDb>()
            data.forEach {
                list.add(DataDb(it))
            }
            return list
        }

        fun convertFromDb(data: RealmList<DataDb>): MutableList<Data> {
            val list = mutableListOf<Data>()
            data.forEach {
                list.add(it.convertFromDb())
            }
            return list
        }
    }
}