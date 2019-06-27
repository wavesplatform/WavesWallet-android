package com.wavesplatform.sdk.model.response.node.transaction

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import com.wavesplatform.sdk.model.request.node.ExchangeTransaction
import kotlinx.android.parcel.Parcelize

/**
 * See [com.wavesplatform.sdk.model.request.node.ExchangeTransaction]
 */
@Parcelize
internal class ExchangeTransactionResponse(@SerializedName("order1")
                                  var order1: ExchangeTransaction.Order,
                                           @SerializedName("order2")
                                  var order2: ExchangeTransaction.Order,
                                           @SerializedName("price")
                                  var price: Long,
                                           @SerializedName("amount")
                                  var amount: Long,
                                           @SerializedName("buyMatcherFee")
                                  var buyMatcherFee: Long,
                                           @SerializedName("sellMatcherFee")
                                  var sellMatcherFee: Long)
    : BaseTransactionResponse(type = BaseTransaction.EXCHANGE), Parcelable