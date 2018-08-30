package com.wavesplatform.wallet.v2.data.manager

import com.vicpin.krealmextensions.queryAll
import com.vicpin.krealmextensions.save
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.wallet.v1.payload.TransactionsInfo
import com.wavesplatform.wallet.v1.request.ReissueTransactionRequest
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.request.AliasRequest
import com.wavesplatform.wallet.v2.data.model.remote.response.*
import com.wavesplatform.wallet.v2.util.TransactionUtil
import com.wavesplatform.wallet.v2.util.isAppOnForeground
import com.wavesplatform.wallet.v2.util.notNull
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import pers.victor.ext.app
import pers.victor.ext.currentTimeMillis
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList

class NodeDataManager @Inject constructor() : DataManager() {
    @Inject
    lateinit var transactionUtil: TransactionUtil
    var transactions: List<Transaction> = ArrayList()
    var pendingTransactions: List<Transaction> = ArrayList()
    var currentLoadTransactionLimitPerRequest = 100

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
        return spamService.spamAssets()
                .map({
                    val scanner = Scanner(it)
                    val spam = arrayListOf<SpamAsset>()
                    while (scanner.hasNextLine()) {
                        spam.add(SpamAsset(scanner.nextLine().split(",")[0]))
                    }
                    scanner.close()
                    spam.saveAll()
                    return@map spam
                })
                .flatMap({ spamAssets ->
                    return@flatMap nodeService.assetsBalance(getAddress())
                            .flatMap({ assets ->
                                return@flatMap loadWavesBalance()
                                        .map({
                                            return@map Pair(assets, it)
                                        })
                                        .subscribeOn(Schedulers.io())
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
                                            assetBalance.isFlatMoney = it.isFlatMoney
                                            assetBalance.isGateway = it.isGateway
                                            assetBalance.isSpam = it.isSpam
                                        }
                                    })
                                }
                                it.first.balances.forEachIndexed { index, assetBalance ->
                                    assetBalance.isSpam = spamAssets.any({
                                        it.assetId == assetBalance.assetId
                                    })
                                }
                                it.first.balances.saveAll()

                                return@map queryAll<AssetBalance>()
                            })
                            .subscribeOn(Schedulers.io())
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

    fun createAlias(createAliasRequest: AliasRequest): Observable<AliasRequest> {
        createAliasRequest.signature = "" // TODO: Need to sign request
        createAliasRequest.senderPublicKey = publicKeyAccountHelper.publicKeyAccount?.publicKeyStr
        createAliasRequest.type = 10
        createAliasRequest.fee = Constants.WAVES_FEE
        createAliasRequest.timestamp = currentTimeMillis
        return nodeService.createAlias(createAliasRequest)
    }

    fun loadTransactions(): Observable<Pair<List<Transaction>?, List<Transaction>?>> {
        return Observable.interval(0, 15, TimeUnit.SECONDS)
                .retry(3)
                .flatMap({
                    if (app.isAppOnForeground()) {
                        return@flatMap Observable.zip(nodeService.transactionList(getAddress(), currentLoadTransactionLimitPerRequest).map({ r -> r[0] }),
                                nodeService.unconfirmedTransactions(), BiFunction<List<Transaction>, List<Transaction>, Pair<List<Transaction>, List<Transaction>>> { t1, t2 ->
                            return@BiFunction Pair(t1, t2)
                        })
                    } else {
                        return@flatMap Observable.just(Pair(null, null))
                    }

                })
    }

    fun currentBlocksHeight(): Observable<Height> {
        return Observable.interval(0, 60, TimeUnit.SECONDS)
                .retry(3)
                .flatMap({
                    return@flatMap nodeService.currentBlocksHeight()
                })
                .map({
                    preferencesHelper.currentBlocksHeight = it.height
                    return@map it
                })
    }

    fun activeLeasing(): Observable<List<Transaction>> {
        return nodeService.activeLeasing(getAddress())
                .map({
                    val activeTransactionList = it.filter {
                        it.asset = Constants.defaultAssets[0]
                        it.transactionTypeId = transactionUtil.getTransactionType(it)
                        it.transactionTypeId == Constants.ID_STARTED_LEASING_TYPE
                    }
                    return@map activeTransactionList
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
