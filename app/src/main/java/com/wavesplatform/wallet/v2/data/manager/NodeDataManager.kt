package com.wavesplatform.wallet.v2.data.manager

import com.vicpin.krealmextensions.*
import com.wavesplatform.wallet.v1.payload.TransactionsInfo
import com.wavesplatform.wallet.v1.request.ReissueTransactionRequest
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.*
import com.wavesplatform.wallet.v2.util.isAppOnForeground
import com.wavesplatform.wallet.v2.util.notNull
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import pers.victor.ext.app
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList

class NodeDataManager @Inject constructor() : DataManager() {

    var transactions: List<Transaction> = ArrayList()
    var pendingTransactions: List<Transaction> = ArrayList()

//    fun loadAssetsFromDBAndNetwork(): Observable<List<AssetBalance>> {
//        return Observable.mergeDelayError(queryAllAsFlowable<AssetBalance>().toObservable(), nodeService.assetsBalance(getAddress())
//                .map({ assets ->
//
//                    // merge db data and API data
//                    executeTransaction {
//                        assets.balances.forEachIndexed({ index, assetBalance ->
//                            val dbAsset = queryFirst<AssetBalance>({ equalTo("assetId", assetBalance.assetId) })
//                            dbAsset.notNull {
//                                assetBalance.isHidden = it.isHidden
//                                assetBalance.isFavorite = it.isFavorite
//                            }
//                        })
//                        assets.balances.saveAll()
//                    }
//
//                    return@map queryAll<AssetBalance>()
//                }), nodeService.wavesBalance(getAddress())
//                .map {
//                    val currentWaves = Constants.defaultAssets[0]
//                    currentWaves.balance = it.balance
//                    currentWaves.save()
//                    return@map arrayListOf<AssetBalance>(currentWaves)
//                })
//
//    }

    fun loadAssets(assetsFromDb: List<AssetBalance>? = null): Observable<List<AssetBalance>> {
        return nodeService.assetsBalance(getAddress())
                .flatMap({ assets ->
                    return@flatMap loadWavesBalance()
                            .map({
                                return@map Pair(assets, it)
                            })
                })
                .map({
                    if (assetsFromDb != null && !assetsFromDb.isEmpty()) {
                        // merge db data and API data
                        it.first.balances.forEachIndexed({ index, assetBalance ->
                            val dbAsset = assetsFromDb.firstOrNull({ dbAsset ->
                                dbAsset.assetId == assetBalance.assetId
                            })
                            dbAsset.notNull {
                                assetBalance.isHidden = it.isHidden
                                assetBalance.isFavorite = it.isFavorite
                            }
                        })
                    }
                    it.first.balances.saveAll()

                    return@map queryAll<AssetBalance>()
                })
    }

    fun loadWavesBalance(): Observable<AssetBalance> {
        return nodeService.wavesBalance(getAddress())
                .map({
                    val currentWaves = Constants.defaultAssets[0]
                    currentWaves.balance = it.balance
                    currentWaves.save()
                    return@map currentWaves
                })

    }

    fun loadTransactions(limit: Int): Observable<Pair<List<Transaction>?, List<Transaction>?>> {
        return Observable.interval(0, 15, TimeUnit.SECONDS)
                .retry(3)
                .flatMap({
                    if (app.isAppOnForeground()) {
                        return@flatMap Observable.zip(nodeService.transactionList(getAddress(), 100).map({ r -> r.get(0) }),
                                nodeService.unconfirmedTransactions(), BiFunction<List<Transaction>, List<Transaction>, Pair<List<Transaction>, List<Transaction>>> { t1, t2 ->
                            return@BiFunction Pair(t1, t2)
                        })
                    } else {
                        return@flatMap Observable.just(Pair(null, null))
                    }

                })
    }


    private fun unconfirmedTransactionsWithOwnFilter(): Observable<List<Transaction>> {
        return nodeService.unconfirmedTransactions()
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
        return nodeService.broadcastReissue(tx)
    }

    fun getTransactionsInfo(asset: String): Observable<TransactionsInfo> {
        return nodeService.getTransactionsInfo(asset)
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

}
