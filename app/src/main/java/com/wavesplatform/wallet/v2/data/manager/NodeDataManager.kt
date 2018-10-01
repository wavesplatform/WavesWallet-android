package com.wavesplatform.wallet.v2.data.manager

import com.vicpin.krealmextensions.deleteAll
import com.vicpin.krealmextensions.queryAll
import com.vicpin.krealmextensions.save
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R.color.r
import com.wavesplatform.wallet.v1.payload.TransactionsInfo
import com.wavesplatform.wallet.v1.request.ReissueTransactionRequest
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.request.AliasRequest
import com.wavesplatform.wallet.v2.data.model.remote.response.*
import com.wavesplatform.wallet.v2.util.RxUtil
import com.wavesplatform.wallet.v2.util.TransactionUtil
import com.wavesplatform.wallet.v2.util.isAppOnForeground
import com.wavesplatform.wallet.v2.util.notNull
import io.reactivex.Observable
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
    @Inject
    lateinit var apiDataManager: ApiDataManager
    var transactions: List<Transaction> = ArrayList()
    var pendingTransactions: List<Transaction> = ArrayList()
    var currentLoadTransactionLimitPerRequest = 100

    fun loadAssets(assetsFromDb: List<AssetBalance>? = null): Observable<List<AssetBalance>> {
        return spamService.spamAssets()
                .map {
                    val scanner = Scanner(it)
                    val spam = arrayListOf<SpamAsset>()
                    while (scanner.hasNextLine()) {
                        spam.add(SpamAsset(scanner.nextLine().split(",")[0]))
                    }
                    scanner.close()

                    // clear old spam list and save new
                    deleteAll<SpamAsset>()
                    spam.saveAll()

                    return@map spam
                }
                .flatMap { spamAssets ->
                    return@flatMap nodeService.assetsBalance(getAddress())
                            .flatMap { assets ->
                                return@flatMap loadWavesBalance()
                                        .map { return@map Pair(assets, it) }
                                        .subscribeOn(Schedulers.io())
                            }
                            .map {
                                if (assetsFromDb != null && !assetsFromDb.isEmpty()) {
                                    // merge db data and API data
                                    it.first.balances.forEachIndexed { index, assetBalance ->
                                        val dbAsset = assetsFromDb.firstOrNull { dbAsset ->
                                            dbAsset.assetId == assetBalance.assetId
                                        }
                                        dbAsset.notNull {
                                            assetBalance.isHidden = it.isHidden
                                            assetBalance.isFavorite = it.isFavorite
                                            assetBalance.isFiatMoney = it.isFiatMoney
                                            assetBalance.isGateway = it.isGateway
                                            assetBalance.isSpam = it.isSpam
                                            assetBalance.position = it.position
                                        }
                                    }
                                }
                                it.first.balances.forEachIndexed { index, assetBalance ->
                                    assetBalance.isSpam = spamAssets.any {
                                        it.assetId == assetBalance.assetId
                                    }
                                }
                                it.first.balances.saveAll()

                                return@map queryAll<AssetBalance>()
                            }
                            .subscribeOn(Schedulers.io())
                }

    }

    fun loadWavesBalance(): Observable<AssetBalance> {
        return nodeService.wavesBalance(getAddress())
                .map {
                    val currentWaves = Constants.defaultAssets[0]
                    currentWaves.balance = it.balance
                    currentWaves.save()
                    return@map currentWaves
                }
    }

    fun createAlias(createAliasRequest: AliasRequest): Observable<Alias> {
        createAliasRequest.senderPublicKey = App.getAccessManager().getWallet()?.publicKeyStr
        createAliasRequest.fee = Constants.WAVES_FEE
        createAliasRequest.timestamp = currentTimeMillis
        App.getAccessManager().getWallet()?.privateKey.notNull {
            createAliasRequest.sign(it)
        }
        return nodeService.createAlias(createAliasRequest)
                .map {
                    it.address = getAddress()
                    it.save()
                    return@map it
                }
    }

    fun loadTransactions(): Observable<List<Transaction>> {
        return Observable.interval(0, 15, TimeUnit.SECONDS)
                .retry(3)
                .flatMap {
                    if (app.isAppOnForeground()) {
                        return@flatMap nodeService.transactionList(getAddress(), currentLoadTransactionLimitPerRequest)
                                .map { r -> r[0] }
                    } else {
                        return@flatMap Observable.just(listOf<Transaction>())
                    }

                }
    }

    fun currentBlocksHeight(): Observable<Height> {
        return Observable.interval(0, 60, TimeUnit.SECONDS)
                .retry(3)
                .flatMap {
                    return@flatMap nodeService.currentBlocksHeight()
                }
                .map {
                    preferencesHelper.currentBlocksHeight = it.height
                    return@map it
                }
    }

    fun activeLeasing(): Observable<List<Transaction>> {
        return nodeService.activeLeasing(getAddress())
                .map {
                    return@map it.filter {
                        it.asset = Constants.wavesAssetInfo
                        it.transactionTypeId = transactionUtil.getTransactionType(it)
                        it.transactionTypeId == Constants.ID_STARTED_LEASING_TYPE && it.sender == App.getAccessManager().getWallet()?.address
                    }
                }
                .flatMap {
                    return@flatMap Observable.fromIterable(it)
                            .flatMap { transaction ->
                                if (transaction.recipient.contains("alias")) {
                                    val aliasName = transaction.recipient.substringAfterLast(":")
                                    return@flatMap apiDataManager.loadAlias(aliasName)
                                            .flatMap {
                                                transaction.recipientAddress = it.address
                                                return@flatMap Observable.just(transaction)
                                            }
                                } else {
                                    transaction.recipientAddress = transaction.recipient
                                    return@flatMap Observable.just(transaction)
                                }
                            }.toList().toObservable()
                }
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
