/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.utils.MoneyUtil
import kotlinx.android.parcel.Parcelize
import java.util.*

open class AssetBalancesResponse(
        @SerializedName("address") var address: String? = null,
        @SerializedName("balances") var balances: List<AssetBalanceResponse> = ArrayList()
)

@Parcelize
open class AssetBalanceResponse(
        @SerializedName("assetId") var assetId: String = "",
        @SerializedName("balance") var balance: Long? = 0,
        @SerializedName("leasedBalance") var leasedBalance: Long? = 0,
        @SerializedName("inOrderBalance") var inOrderBalance: Long? = 0,
        @SerializedName("reissuable") var reissuable: Boolean? = false,
        @SerializedName("minSponsoredAssetFee") var minSponsoredAssetFee: Long? = 0,
        @SerializedName("sponsorBalance") var sponsorBalance: Long? = 0,
        @SerializedName("quantity") var quantity: Long? = 0,
        @SerializedName("issueTransaction") var issueTransaction: IssueTransactionResponse? = IssueTransactionResponse(id = assetId),
        var isHidden: Boolean = false,
        var position: Int = -1,
        var configureVisibleState: Boolean = false,
        var isChecked: Boolean = false,
        var isFiatMoney: Boolean = false,
        var isFavorite: Boolean = false,
        var isGateway: Boolean = false,
        var isSpam: Boolean = false
) : Parcelable {

    fun isSponsored(): Boolean {
        return minSponsoredAssetFee ?: 0 > 0
    }

    fun isScripted(): Boolean {
        return !issueTransaction?.script.isNullOrEmpty()
    }

    fun isMyWavesToken(address: String): Boolean {
        return issueTransaction?.sender == address
    }

    fun getDecimals(): Int {
        return if (issueTransaction != null) {
            issueTransaction!!.decimals ?: 8
        } else {
            8
        }
    }

    fun getDescription(): String {
        return if (issueTransaction == null) {
            ""
        } else {
            issueTransaction!!.description ?: ""
        }
    }

    fun getDisplayTotalBalance(): String {
        return MoneyUtil.getScaledText(balance, this)
    }

    fun getDisplayInOrderBalance(): String {
        return MoneyUtil.getScaledText(inOrderBalance, this)
    }

    fun getDisplayLeasedBalance(): String {
        return MoneyUtil.getScaledText(leasedBalance, this)
    }

    fun getDisplayAvailableBalance(): String {
        return MoneyUtil.getScaledText(getAvailableBalance(), this)
    }

    fun getAvailableBalance(): Long {
        val availableBalance = balance
                ?.minus(inOrderBalance ?: 0)
                ?.minus(leasedBalance ?: 0) ?: 0L
        return if (availableBalance < 0) {
            0L
        } else {
            availableBalance
        }
    }

    fun getSponsorBalance(): Long {
        return sponsorBalance ?: 0
    }

    fun isAssetId(assetId: String): Boolean {
        return assetId == this.assetId
    }

    fun getName(): String {
        return issueTransaction?.name ?: ""
    }

    fun getDisplayBalanceWithUnit(): String {
        return getDisplayTotalBalance() + " " + getName()
    }

    fun isWaves(): Boolean {
        return assetId.isNullOrEmpty()
    }
}

@Parcelize
open class IssueTransactionResponse(
        @SerializedName("type") var type: Int? = 0,
        @SerializedName("id") var id: String? = "",
        @SerializedName("sender") var sender: String? = "",
        @SerializedName("senderPublicKey") var senderPublicKey: String? = "",
        @SerializedName("fee") var fee: Int? = 0,
        @SerializedName("timestamp") var timestamp: Long? = 0,
        @SerializedName("signature") var signature: String? = "",
        @SerializedName("version") var version: Int? = 0,
        @SerializedName("assetId") var assetId: String? = "",
        @SerializedName("name") var name: String? = "",
        @SerializedName("quantity") var quantity: Long? = 0,
        @SerializedName("reissuable") var reissuable: Boolean? = false,
        @SerializedName("decimals") var decimals: Int? = 0,
        @SerializedName("description") var description: String? = "",
        @SerializedName("script") var script: String? = ""
) : Parcelable