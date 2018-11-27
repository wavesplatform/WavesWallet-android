package com.wavesplatform.wallet.v2.ui.home.wallet.assets.details.content

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsSingle
import com.wavesplatform.wallet.v2.data.model.local.HistoryItem
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.data.model.remote.response.TransactionType
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import com.wavesplatform.wallet.v2.util.isWavesId
import com.wavesplatform.wallet.v2.util.transactionType
import pyxis.uzuki.live.richutilskt.utils.runAsync
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import javax.inject.Inject

@InjectViewState
class AssetDetailsContentPresenter @Inject constructor() : BasePresenter<AssetDetailsContentView>() {

    var assetBalance: AssetBalance? = null

    fun loadLastTransactionsFor(assetId: String) {
        runAsync {
            addSubscription(queryAllAsSingle<Transaction>().toObservable()
                    .map {
                        return@map it
                                .sortedByDescending { it.timestamp }
                                .filter { transaction ->
                                    return@filter isNotSpam(transaction)
                                }
                                .filter { transaction ->
                                    when {
                                        assetId.isWavesId() ->
                                            return@filter transaction.assetId.isNullOrEmpty()
                                        isAssetIdInExchange(transaction, assetId) ->
                                            return@filter true
                                        else ->
                                            return@filter transaction.assetId == assetId
                                    }
                                }
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


    companion object {

        fun isAssetIdInExchange(transaction: Transaction, assetId: String) =
                (transaction.order1?.assetPair?.amountAssetObject?.id == assetId
                        || transaction.order1?.assetPair?.priceAssetObject?.id == assetId)

        private fun isNotSpam(transaction: Transaction) =
                transaction.transactionType() != TransactionType.MASS_SPAM_RECEIVE_TYPE
                        || transaction.transactionType() != TransactionType.SPAM_RECEIVE_TYPE
    }
}
