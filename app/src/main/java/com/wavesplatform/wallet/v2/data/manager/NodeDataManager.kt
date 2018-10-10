package com.wavesplatform.wallet.v2.data.manager

import com.vicpin.krealmextensions.*
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R.color.i
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.manager.base.BaseDataManager
import com.wavesplatform.wallet.v2.data.model.remote.request.AliasRequest
import com.wavesplatform.wallet.v2.data.model.remote.request.CancelLeasingRequest
import com.wavesplatform.wallet.v2.data.model.remote.response.*
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

class NodeDataManager @Inject constructor() : BaseDataManager() {
    @Inject
    lateinit var transactionUtil: TransactionUtil
    @Inject
    lateinit var apiDataManager: ApiDataManager
    var transactions: List<Transaction> = ArrayList()
    var pendingTransactions: List<Transaction> = ArrayList()
    var currentLoadTransactionLimitPerRequest = 100

    fun loadSpamAssets(): Observable<ArrayList<SpamAsset>> {
        return spamService.spamAssets(prefsUtil.getValue(PrefsUtil.KEY_SPAM_URL, Constants.URL_SPAM))
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
                }.map { spamListFromDb ->
                    if (prefsUtil.getValue(PrefsUtil.KEY_DISABLE_SPAM_FILTER, false)) {
                        return@map arrayListOf<SpamAsset>()
                    } else {
                        return@map spamListFromDb
                    }
                }
    }

    fun loadAssets(assetsFromDb: List<AssetBalance>? = null): Observable<List<AssetBalance>> {
        return loadSpamAssets()
                .flatMap { spamAssets ->
                    return@flatMap nodeService.assetsBalance(getAddress())
                            .flatMap { assets ->
                                return@flatMap loadWavesBalance()
                                        .map { return@map Pair(assets, it) }
                                        .subscribeOn(Schedulers.io())
                            }
                            .map { pair ->
                                if (assetsFromDb != null && !assetsFromDb.isEmpty()) {
                                    // merge db data and API data
                                    pair.first.balances.forEachIndexed { index, assetBalance ->
                                        val dbAsset = assetsFromDb.firstOrNull { dbAsset ->
                                            dbAsset.assetId == assetBalance.assetId
                                        }
                                        dbAsset.notNull {
                                            assetBalance.isHidden = it.isHidden
                                            assetBalance.issueTransaction?.name = it.issueTransaction?.name
                                            assetBalance.isFavorite = it.isFavorite
                                            assetBalance.isFiatMoney = it.isFiatMoney
                                            assetBalance.isGateway = it.isGateway
                                            assetBalance.isSpam = it.isSpam
                                            assetBalance.position = it.position
                                            assetBalance.isWaves = it.isWaves
                                        }
                                    }
                                }

                                pair.first.balances.forEachIndexed { index, assetBalance ->
                                    assetBalance.isSpam = spamAssets.any {
                                        it.assetId == assetBalance.assetId
                                    }
                                }

                                if (pair.first.balances.any { it.position != -1 }) {
                                    pair.first.balances.forEach {
                                        if (it.position == -1) {
                                            it.position = pair.first.balances.size + 1
                                        }
                                    }
                                }

                                pair.first.balances.saveAll()

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

    fun cancelLeasing(cancelLeasingRequest: CancelLeasingRequest): Observable<Transaction> {
        cancelLeasingRequest.senderPublicKey = App.getAccessManager().getWallet()?.publicKeyStr
        cancelLeasingRequest.fee = Constants.WAVES_FEE
        cancelLeasingRequest.timestamp = currentTimeMillis

        App.getAccessManager().getWallet()?.privateKey.notNull {
            cancelLeasingRequest.sign(it)
        }
        return nodeService.cancelLeasing(cancelLeasingRequest)
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

}
