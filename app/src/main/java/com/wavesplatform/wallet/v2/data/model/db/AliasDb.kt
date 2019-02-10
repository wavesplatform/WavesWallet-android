package com.wavesplatform.wallet.v2.data.model.db

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.response.Alias
import com.wavesplatform.sdk.model.response.SpamAsset
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@RealmClass(name = "Alias")
open class AliasDb(
        @PrimaryKey
        @SerializedName("alias") var alias: String? = "",
        @SerializedName("address") var address: String? = "",
        var own: Boolean = false
) : RealmModel, Parcelable {

        fun convertFromDb(): Alias {
                return Alias()
        }

        companion object {

                fun convertToDb(alias: Alias): AliasDb {
                        return AliasDb()
                }

                fun convertToDb(aliases: List<Alias>): List<AliasDb> {
                        return listOf()
                }

                fun convertFromDb(aliases: List<AliasDb>): List<Alias> {
                        return listOf()
                }
        }
}