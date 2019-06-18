/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.assets.details.content

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryFirst
import com.vicpin.krealmextensions.save
import com.wavesplatform.wallet.v2.data.model.local.TransactionType
import com.wavesplatform.sdk.model.response.node.AddressAssetBalanceResponse
import com.wavesplatform.sdk.model.response.node.AssetBalanceResponse
import com.wavesplatform.sdk.model.response.node.AssetsDetailsResponse
import com.wavesplatform.sdk.model.response.node.TransactionResponse
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.sdk.utils.isWavesId
import com.wavesplatform.sdk.utils.notNull
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.data.model.db.AssetBalanceDb
import com.wavesplatform.wallet.v2.data.model.local.HistoryItem
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.isSponsorshipTransaction
import com.wavesplatform.wallet.v2.util.transactionType
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import pyxis.uzuki.live.richutilskt.utils.runAsync
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@InjectViewState
class AssetDetailsContentPresenter @Inject constructor() : BasePresenter<AssetDetailsContentView>() {

    var assetBalance: AssetBalanceResponse? = null

    fun loadLastTransactionsFor(asset: AssetBalanceResponse, allTransactions: List<TransactionResponse>) {
        runAsync {
            addSubscription(Observable.just(allTransactions)
                    .map {
                        return@map filterNodeCancelLeasing(it)
                    }
                    .map {
                        return@map it.asSequence().filter { transaction ->
                            isNotSpam(transaction) &&
                                    (asset.assetId.isWavesId() && transaction.assetId.isNullOrEmpty() && !transaction.isSponsorshipTransaction()) ||
                                    isAssetIdInExchange(transaction, asset.assetId) ||
                                    transaction.assetId == asset.assetId && transaction.transactionType() != TransactionType.RECEIVE_SPONSORSHIP_TYPE ||
                                    (transaction.feeAssetId == asset.assetId && transaction.isSponsorshipTransaction())
                        }
                                .sortedByDescending { it.timestamp }
                                .mapTo(ArrayList()) { HistoryItem(HistoryItem.TYPE_DATA, it) }
                                .take(10)
                                .toMutableList()
                    }
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({ list ->
                        runOnUiThread {
                            viewState.showLastTransactions(list)
                        }
                    }, {
                        it.printStackTrace()
                        runOnUiThread {
                            viewState.showLastTransactions(
                                    emptyList<HistoryItem>().toMutableList())
                        }
                    }))
        }
    }

    private fun filterNodeCancelLeasing(transactions: List<TransactionResponse>): List<TransactionResponse> {
        return transactions.filter { transaction ->
            if (transaction.transactionType() != TransactionType.CANCELED_LEASING_TYPE) {
                true
            } else {
                transaction.lease?.recipientAddress != App.getAccessManager().getWallet()?.address
            }
        }
    }

    fun reloadAssetDetails(delay: Long = 0) {
        addSubscription(Observable.zip(
                nodeServiceManager.addressAssetBalance(
                        App.getAccessManager().getWallet()?.address,
                        assetBalance?.assetId ?: ""),
                nodeServiceManager.assetDetails(assetBalance?.assetId),
                BiFunction { assetAddressBalance: AddressAssetBalanceResponse,
                             details: AssetsDetailsResponse ->
                    val dbAssetBalance = queryFirst<AssetBalanceDb> {
                        equalTo("assetId", assetBalance?.assetId ?: "")
                    }
                    dbAssetBalance.notNull {
                        it.balance = assetAddressBalance.balance
                        it.quantity = details.quantity
                        it.save()
                        assetBalance = it.convertFromDb()
                    }
                })
                .delay(delay, TimeUnit.MILLISECONDS)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe {
                    assetBalance.notNull {
                        viewState.onAssetAddressBalanceLoadSuccess(it)
                    }
                })
    }

    companion object {
        fun isAssetIdInExchange(transaction: TransactionResponse, assetId: String) =
                transaction.transactionType() == TransactionType.EXCHANGE_TYPE &&
                        (transaction.order1?.assetPair?.amountAssetObject?.id == assetId ||
                                transaction.order1?.assetPair?.priceAssetObject?.id == assetId)

        private fun isNotSpam(transaction: TransactionResponse) =
                transaction.transactionType() != TransactionType.MASS_SPAM_RECEIVE_TYPE ||
                        transaction.transactionType() != TransactionType.SPAM_RECEIVE_TYPE
    }
}
