package com.wavesplatform.wallet.v2.data.model.db

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.response.node.transaction.AliasTransactionResponse
import com.wavesplatform.sdk.utils.notNull
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@RealmClass
open class AliasDb(
        @PrimaryKey
        @SerializedName("alias") var alias: String? = "",
        @SerializedName("address") var address: String? = "",
        @SerializedName("own") var own: Boolean = false
) : RealmModel, Parcelable {

    constructor(alias: AliasTransactionResponse?) : this() {
        alias.notNull {
            this.alias = it.alias
            this.address = it.address
            this.own = it.own
        }
    }

    fun convertFromDb(): AliasTransactionResponse {
        return AliasTransactionResponse(
                alias = this.alias,
                address = this.address,
                own = this.own)
    }

    companion object {

        fun convertToDb(aliases: List<AliasTransactionResponse>): MutableList<AliasDb> {
            val list = mutableListOf<AliasDb>()
            aliases.forEach {
                list.add(AliasDb(it))
            }
            return list
        }

        fun convertFromDb(aliases: List<AliasDb>): MutableList<AliasTransactionResponse> {
            val list = mutableListOf<AliasTransactionResponse>()
            aliases.forEach {
                list.add(it.convertFromDb())
            }
            return list
        }
    }
}