package com.wavesplatform.sdk.manager

import android.content.Context
import android.text.TextUtils
import com.wavesplatform.sdk.Constants
import com.wavesplatform.sdk.Wavesplatform
import com.wavesplatform.sdk.manager.base.BaseDataManager
import com.wavesplatform.sdk.model.request.*
import com.wavesplatform.sdk.model.response.*
import com.wavesplatform.sdk.utils.TransactionUtil
import com.wavesplatform.sdk.utils.notNull
import com.wavesplatform.sdk.utils.sumByLong
import io.reactivex.Observable
import io.reactivex.functions.Function3
import io.reactivex.schedulers.Schedulers
import pers.victor.ext.currentTimeMillis
import retrofit2.CallAdapter
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

@Singleton
class NodeDataManager (context: Context,
                       factory: CallAdapter.Factory?) : BaseDataManager(context, factory) {

    @Inject
    lateinit var apiDataManager: ApiDataManager
    @Inject
    lateinit var matcherDataManager: MatcherDataManager
    var transactions: List<Transaction> = ArrayList()

    fun loadSpamAssets(enableSpamFilter: Boolean): Observable<ArrayList<SpamAsset>> {
        return spamService.spamAssets(Constants.URL_SPAM_FILE)
                .map {
                    val scanner = Scanner(it)
                    val spam = arrayListOf<SpamAsset>()
                    while (scanner.hasNextLine()) {
                        spam.add(SpamAsset(scanner.nextLine().split(",")[0]))
                    }
                    scanner.close()
                    return@map spam
                }.map { spamListFromDb ->
                    if (enableSpamFilter) {
                        return@map spamListFromDb
                    } else {
                        return@map arrayListOf<SpamAsset>()
                    }
                }
    }

    fun transactionsBroadcast(tx: TransactionsBroadcastRequest): Observable<TransactionsBroadcastRequest> {
        return nodeService.transactionsBroadcast(tx)
    }

    fun loadAssets(assetsFromDb: List<AssetBalance>? = null): Observable<List<AssetBalance>> {
        return loadSpamAssets(false)
                .flatMap { spamAssets ->
                    return@flatMap nodeService.assetsBalance(Wavesplatform.getWallet().address)
                            .flatMap { assets ->
                                return@flatMap Observable.zip(loadWavesBalance(), matcherDataManager.loadReservedBalances(), Observable.just(assets), Function3 { t1: AssetBalance, t2: Map<String, Long>, t3: AssetBalances ->
                                    return@Function3 Triple(t1, t2, t3)
                                })
                            }
                            .map { tripple ->
                                if (assetsFromDb != null && !assetsFromDb.isEmpty()) {
                                    // merge db data and API data
                                    tripple.third.balances.forEachIndexed { index, assetBalance ->
                                        val dbAsset = assetsFromDb.firstOrNull { dbAsset ->
                                            dbAsset.assetId == assetBalance.assetId
                                        }
                                        dbAsset.notNull {
                                            assetBalance.isHidden = it.isHidden
                                            assetBalance.issueTransaction?.name = it.issueTransaction?.name
                                            assetBalance.issueTransaction?.quantity = it.issueTransaction?.quantity
                                            assetBalance.issueTransaction?.decimals = it.issueTransaction?.decimals
                                            assetBalance.issueTransaction?.timestamp = it.issueTransaction?.timestamp
                                            assetBalance.isFavorite = it.isFavorite
                                            assetBalance.isFiatMoney = it.isFiatMoney
                                            assetBalance.isGateway = it.isGateway
                                            assetBalance.isSpam = it.isSpam
                                            assetBalance.position = it.position
                                        }
                                    }
                                }

                                findElementsInDbWithZeroBalancesAndDelete(assetsFromDb, tripple)

                                tripple.third.balances.forEachIndexed { index, assetBalance ->
                                    assetBalance.inOrderBalance = tripple.second[assetBalance.assetId]
                                            ?: 0L

                                    assetBalance.isSpam = spamAssets.any {
                                        it.assetId == assetBalance.assetId
                                    }
                                }

                                if (tripple.third.balances.any { it.position != -1 }) {
                                    tripple.third.balances.forEach {
                                        if (it.position == -1) {
                                            it.position = tripple.third.balances.size + 1
                                        }
                                    }
                                }

                                /*tripple.third.balances.saveAll()
                                return@map queryAll<AssetBalance>()*/

                                return@map tripple.third.balances
                            }
                            .subscribeOn(Schedulers.io())
                }
    }

    private fun findElementsInDbWithZeroBalancesAndDelete(
            assetsFromDb: List<AssetBalance>?,
            tripple: Triple<AssetBalance, Map<String, Long>, AssetBalances>) {
        if (assetsFromDb?.size != tripple.third.balances.size) {
            val dbIds = assetsFromDb?.mapTo(ArrayList()) { it.assetId }
            val apiIds = tripple.third.balances.mapTo(ArrayList()) { it.assetId }
            val offsetAsset = dbIds?.minus(apiIds)

            offsetAsset?.forEach { id ->
                if (id.isNotEmpty()) {
                    /*val assetBalance = queryFirst<AssetBalance> { equalTo("assetId", id) }
                    if (assetBalance?.isGateway == false) {
                        assetBalance.delete { equalTo("assetId", id) }
                    } else {
                        assetBalance?.balance = 0
                        assetBalance?.save()
                    }*/
                }
            }
        }
    }

    fun loadWavesBalance(): Observable<AssetBalance> {
        return Observable.zip(
                // load total balance
                nodeService.wavesBalance(Wavesplatform.getWallet().address).map {
                    return@map it.balance
                },
                // load leased balance
                activeLeasing().map {
                    return@map it.sumByLong { it.amount }
                },
                // load in order balance
                matcherDataManager.loadReservedBalances()
                        .map {
                            return@map it[Constants.wavesAssetInfo.name] ?: 0L
                        },
                Function3 { totalBalance: Long, leasedBalance: Long, inOrderBalance: Long ->
                    val currentWaves = Constants.defaultAssets[0]
                    currentWaves.balance = totalBalance
                    currentWaves.leasedBalance = leasedBalance
                    currentWaves.inOrderBalance = inOrderBalance
                    //currentWaves.save()
                    return@Function3 currentWaves
                })
    }

    fun createAlias(createAliasRequest: AliasRequest): Observable<Alias> {
        createAliasRequest.senderPublicKey = Wavesplatform.getWallet().publicKeyStr ?: ""
        createAliasRequest.timestamp = currentTimeMillis
        Wavesplatform.getWallet().privateKey.notNull {
            createAliasRequest.sign(it)
        }
        return nodeService.createAlias(createAliasRequest)
    }

    fun cancelLeasing(cancelLeasingRequest: CancelLeasingRequest): Observable<Transaction> {
        cancelLeasingRequest.senderPublicKey = Wavesplatform.getWallet().publicKeyStr ?: ""
        cancelLeasingRequest.timestamp = currentTimeMillis

        Wavesplatform.getWallet().privateKey.notNull {
            cancelLeasingRequest.sign(it)
        }
        return nodeService.cancelLeasing(cancelLeasingRequest)
    }

    fun startLeasing(createLeasingRequest: CreateLeasingRequest,
                     recipientIsAlias: Boolean,
                     fee: Long): Observable<Transaction> {
        createLeasingRequest.senderPublicKey = Wavesplatform.getWallet().publicKeyStr ?: ""
        createLeasingRequest.fee = fee
        createLeasingRequest.timestamp = currentTimeMillis

        Wavesplatform.getWallet().privateKey.notNull {
            createLeasingRequest.sign(it, recipientIsAlias)
        }
        return nodeService.createLeasing(createLeasingRequest)
    }

    fun loadTransactions(currentLoadTransactionLimitPerRequest: Int): Observable<List<Transaction>> {
        return Observable.interval(0, 15, TimeUnit.SECONDS)
                .retry(3)
                .flatMap {
                    /*if (ProcessLifecycleOwner.get().lifecycle.currentState == Lifecycle.State.RESUMED) {
                        return@flatMap nodeService.transactionList(getAddress(), currentLoadTransactionLimitPerRequest)
                                .map { r -> r[0] }
                    } else {
                        return@flatMap Observable.just(listOf<Transaction>())
                    }*/
                    return@flatMap nodeService.transactionList(
                            Wavesplatform.getWallet().address,
                            currentLoadTransactionLimitPerRequest)
                            .map { r -> r[0] }
                }
                .onErrorResumeNext(Observable.empty())
    }

    fun loadLightTransactions(): Observable<List<Transaction>> {
        return nodeService.transactionList(Wavesplatform.getWallet().address, 100)
                .map { r -> r[0] }
    }

    fun currentBlocksHeight(): Observable<Height> {
        return Observable.interval(0, 60, TimeUnit.SECONDS)
                .retry(3)
                .flatMap {
                    return@flatMap nodeService.currentBlocksHeight()
                }
                .map {
                    //preferencesHelper.currentBlocksHeight = it.height
                    return@map it
                }
                .onErrorResumeNext(Observable.empty())
    }

    fun activeLeasing(): Observable<List<Transaction>> {
        return nodeService.activeLeasing(Wavesplatform.getWallet().address)
                .map {
                    return@map it.filter {
                        it.asset = Constants.wavesAssetInfo
                        it.transactionTypeId = TransactionUtil.getTransactionType(it)
                        it.transactionTypeId == Constants.ID_STARTED_LEASING_TYPE
                                && it.sender == Wavesplatform.getWallet().address
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

    fun burn(burn: BurnRequest): Observable<BurnRequest> {
        return nodeService.burn(burn)
    }

    fun scriptAddressInfo(address: String): Observable<ScriptInfo> {
        return nodeService.scriptAddressInfo(address)
    }

    fun assetDetails(assetId: String?): Observable<AssetsDetails> {
        return if (TextUtils.isEmpty(assetId) || assetId == "WAVES") {
            Observable.just(AssetsDetails(assetId = "WAVES", scripted = false))
        } else {
            nodeService.assetDetails(assetId!!)
        }
    }
}
