package com.wavesplatform.wallet.v2.data.model.remote.response

import android.os.Parcelable
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.google.gson.annotations.SerializedName
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.AssetsAdapter
import io.realm.RealmModel
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import kotlinx.android.parcel.Parcelize
import java.util.*

data class AssetBalances(
    @SerializedName("address") var address: String? = null,
    @SerializedName("balances") var balances: List<AssetBalance> = ArrayList()
)

@Parcelize
@RealmClass
open class AssetBalance(
    @PrimaryKey
    @SerializedName("assetId") var assetId: String = "",
    @SerializedName("balance") var balance: Long? = 0,
    @SerializedName("leasedBalance") var leasedBalance: Long? = 0,
    @SerializedName("inOrderBalance") var inOrderBalance: Long? = 0,
    @SerializedName("reissuable") var reissuable: Boolean? = false,
    @SerializedName("minSponsoredAssetFee") var minSponsoredAssetFee: Long? = 0,
    @SerializedName("sponsorBalance") var sponsorBalance: Long? = 0,
    @SerializedName("quantity") var quantity: Long? = 0,
    @SerializedName("issueTransaction") var issueTransaction: IssueTransaction? = IssueTransaction(id = assetId),
    var isHidden: Boolean = false,
    var position: Int = -1,
    @Ignore var configureVisibleState: Boolean = false,
    @Ignore var isChecked: Boolean = false,
    var isFiatMoney: Boolean = false,
    var isFavorite: Boolean = false,
    var isGateway: Boolean = false,
    var isSpam: Boolean = false
) : RealmModel, Parcelable, MultiItemEntity {

    override fun getItemType(): Int {
        return when {
            isSpam -> AssetsAdapter.TYPE_SPAM_ASSET
            isHidden -> AssetsAdapter.TYPE_HIDDEN_ASSET
            else -> AssetsAdapter.TYPE_ASSET
        }
    }

    fun isSponsored(): Boolean {
        return minSponsoredAssetFee ?: 0 > 0
    }

    fun isScripted(): Boolean {
        return issueTransaction?.script != null
    }

    fun isMyWavesToken(): Boolean {
        return issueTransaction?.sender == App.getAccessManager().getWallet()?.address
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

    fun getName(): String? {
        return issueTransaction?.name
    }

    fun getDisplayBalanceWithUnit(): String {
        return getDisplayTotalBalance() + " " + getName()
    }

    fun isWaves(): Boolean {
        return assetId.isNullOrEmpty()
    }

    companion object {

        fun isFiat(assetId: String): Boolean {
            for (fiat in Constants.defaultFiat()) {
                if (assetId == fiat) {
                    return true
                }
            }
            return false
        }

        fun isGateway(assetId: String): Boolean {
            if (assetId == "") {
                return false
            }

            for (fiat in Constants.defaultCrypto()) {
                if (assetId == fiat) {
                    return true
                }
            }
            return false
        }
    }
}

@Parcelize
@RealmClass
open class IssueTransaction(
    @SerializedName("type") var type: Int? = 0,
    @PrimaryKey
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
) : RealmModel, Parcelable

@Parcelize
open class AssetBalanceStore(
        @SerializedName("assetId") var assetId: String = "",
        @SerializedName("isHidden") var isHidden: Boolean = false,
        @SerializedName("position") var position: Int = -1,
        @SerializedName("isFavorite") var isFavorite: Boolean = false) : Parcelable