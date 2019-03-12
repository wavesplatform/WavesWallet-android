package com.wavesplatform.wallet.v2.ui.home.wallet.assets.details.content

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryFirst
import com.vicpin.krealmextensions.save
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.data.model.local.HistoryItem
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.data.model.remote.response.TransactionType
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import com.wavesplatform.wallet.v2.util.isWavesId
import com.wavesplatform.wallet.v2.util.notNull
import com.wavesplatform.wallet.v2.util.transactionType
import io.reactivex.Observable
import pyxis.uzuki.live.richutilskt.utils.runAsync
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import javax.inject.Inject

@InjectViewState
class AssetDetailsContentPresenter @Inject constructor() : BasePresenter<AssetDetailsContentView>() {

    var assetBalance: AssetBalance? = null

    fun loadLastTransactionsFor(asset: AssetBalance, allTransactions: List<Transaction>) {
        runAsync {
            addSubscription(Observable.just(allTransactions)
                    .map {
                        return@map it.filter { transaction ->
                            isNotSpam(transaction) &&
                                    (asset.assetId.isWavesId() && transaction.assetId.isNullOrEmpty() && !transaction.isSponsorshipTransaction()) ||
                                    AssetDetailsContentPresenter.isAssetIdInExchange(transaction, asset.assetId) ||
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

    fun reloadAssetAddressBalance() {
        addSubscription(nodeDataManager.addressAssetBalance(
                App.getAccessManager().getWallet()?.address ?: "",
                assetBalance?.assetId ?: "")
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe { assetAddressBalance ->
                    val dbAssetBalance = queryFirst<AssetBalance> {
                        equalTo("assetId", assetBalance?.assetId ?: "")
                    }
                    dbAssetBalance.notNull {
                        it.balance = assetAddressBalance.balance
                        it.save()
                        viewState.onAssetAddressBalanceLoadSuccess(it)
                    }
                })
    }

    companion object {
        fun isAssetIdInExchange(transaction: Transaction, assetId: String) =
                transaction.transactionType() == TransactionType.EXCHANGE_TYPE &&
                        (transaction.order1?.assetPair?.amountAssetObject?.id == assetId ||
                        transaction.order1?.assetPair?.priceAssetObject?.id == assetId)

        private fun isNotSpam(transaction: Transaction) =
                transaction.transactionType() != TransactionType.MASS_SPAM_RECEIVE_TYPE ||
                        transaction.transactionType() != TransactionType.SPAM_RECEIVE_TYPE
    }
}
