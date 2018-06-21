package com.wavesplatform.wallet.v2.data.manager

import com.vicpin.krealmextensions.queryAllAsFlowable
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.wallet.v1.payload.AssetBalances
import com.wavesplatform.wallet.v1.payload.Transaction
import com.wavesplatform.wallet.v1.payload.TransactionsInfo
import com.wavesplatform.wallet.v1.request.ReissueTransactionRequest
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.IssueTransaction
import io.reactivex.Observable
import java.util.*
import javax.inject.Inject

class NodeDataManager @Inject constructor() : DataManager() {

    var assetBalances = AssetBalances()
    var transactions: List<Transaction> = ArrayList()
    var pendingTransactions: List<Transaction> = ArrayList()
    var pendingAssets: MutableList<AssetBalance> = ArrayList()

    fun loadAssetsBalance(): Observable<List<AssetBalance>> {
        return Observable.mergeDelayError(queryAllAsFlowable<AssetBalance>().toObservable(), appService.assetsBalance(getAddress())
                        .map({
                            it.balances.saveAll()
                            return@map it.balances
                        }))

    }

    private fun unconfirmedTransactionsWithOwnFilter(): Observable<List<Transaction>> {
        return appService.unconfirmedTransactions()
                .flatMap {
                    Observable.fromIterable(it)
                            .map {
                                if (it.isOwn) it.isPending = true
                                it
                            }
                            .toList()
                            .toObservable()
                }
    }

    fun broadcastIssue(tx: ReissueTransactionRequest): Observable<ReissueTransactionRequest> {
        return appService.broadcastReissue(tx)
    }

    fun getTransactionsInfo(asset: String): Observable<TransactionsInfo> {
        return appService.getTransactionsInfo(asset)
    }

    var wavesAsset: AssetBalance = AssetBalance(
            assetId = "",
            quantity = 100000000L * 100000000L,
            issueTransaction = IssueTransaction(
                    decimals = 8,
                    quantity = 100000000L * 100000000L,
                    name = "WAVES"
            ))

    private fun updatePendingTxs() {
        for (tx in this.transactions) {
            val i = pendingTransactions.iterator() as MutableIterator<Transaction>
            while (i.hasNext()) {
                if (tx.id == i.next().id) {
                    i.remove()
                }
            }
        }
    }

    private fun updatePendingBalances() {
        for (ab in this.assetBalances.balances) {
            val i = pendingAssets.iterator()
            while (i.hasNext()) {
                if (ab.isAssetId(i.next().assetId)) {
                    i.remove()
                }
            }
        }
    }

}
