/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.ProcessLifecycleOwner
import android.text.TextUtils
import com.vicpin.krealmextensions.*
import com.wavesplatform.sdk.utils.Constants
import com.wavesplatform.sdk.net.model.request.*
import com.wavesplatform.sdk.net.model.response.*
import com.wavesplatform.sdk.utils.EnvironmentManager
import com.wavesplatform.sdk.utils.TransactionUtil
import com.wavesplatform.sdk.utils.notNull
import com.wavesplatform.sdk.utils.sumByLong
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.analytics.AnalyticAssetManager
import com.wavesplatform.wallet.v2.data.helpers.ClearAssetsHelper
import com.wavesplatform.wallet.v2.data.manager.base.BaseDataManager
import com.wavesplatform.wallet.v2.data.model.db.AliasDb
import com.wavesplatform.wallet.v2.data.model.db.AssetBalanceDb
import com.wavesplatform.wallet.v2.data.model.db.SpamAssetDb
import com.wavesplatform.wallet.v2.data.model.db.TransactionDb
import com.wavesplatform.wallet.v2.data.model.local.LeasingStatus
import com.wavesplatform.wallet.v2.data.model.userdb.AssetBalanceStore
import com.wavesplatform.wallet.v2.util.loadDbWavesBalance
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

@Singleton
class NodeDataManager @Inject constructor() : BaseDataManager() {

    @Inject
    lateinit var apiDataManager: ApiDataManager
    @Inject
    lateinit var githubDataManager: GithubDataManager
    @Inject
    lateinit var matcherDataManager: MatcherDataManager
    @Inject
    lateinit var analyticAssetManager: AnalyticAssetManager
    var transactions: List<TransactionResponse> = ArrayList()

    fun loadSpamAssets(): Observable<ArrayList<SpamAssetResponse>> {
        return githubService.spamAssets(prefsUtil.getValue(PrefsUtil.KEY_SPAM_URL, EnvironmentManager.servers.spamUrl))
                .map {
                    val scanner = Scanner(it)
                    val spam = arrayListOf<SpamAssetResponse>()
                    while (scanner.hasNextLine()) {
                        spam.add(SpamAssetResponse(scanner.nextLine().split(",")[0]))
                    }
                    scanner.close()

                    // clear old spam list and save new
                    deleteAll<SpamAssetDb>()
                    SpamAssetDb.convertToDb(spam).saveAll()

                    return@map spam
                }.map { spamListFromDb ->
                    if (prefsUtil.getValue(PrefsUtil.KEY_ENABLE_SPAM_FILTER, true)) {
                        return@map spamListFromDb
                    } else {
                        return@map arrayListOf<SpamAssetResponse>()
                    }
                }
    }

    fun transactionsBroadcast(tx: TransactionsBroadcastRequest): Observable<TransactionsBroadcastRequest> {
        return nodeService.transactionsBroadcast(tx)
                .doOnNext {
                    rxEventBus.post(Events.UpdateAssetsBalance())
                }
    }

    fun assetsBalances(withWaves: Boolean = true): Observable<MutableList<AssetBalanceResponse>> {
        return if (withWaves) {
            Observable.zip(
                    nodeService.assetsBalance(getAddress())
                            .map { it.balances.toMutableList() },
                    loadWavesBalance(),
                    BiFunction { assetsBalance: MutableList<AssetBalanceResponse>, wavesBalance: AssetBalanceResponse ->
                        assetsBalance.add(0, wavesBalance)
                        return@BiFunction assetsBalance
                    })
        } else {
            nodeService.assetsBalance(getAddress())
                    .map {
                        it.balances.toMutableList()
                    }
        }
    }

    fun loadAssets(assetsFromDb: List<AssetBalanceResponse>? = null): Observable<List<AssetBalanceResponse>> {
        return loadSpamAssets()
                .flatMap { spamAssets ->
                    return@flatMap nodeService.assetsBalance(getAddress())
                            .flatMap { assets ->
                                return@flatMap Observable.zip(loadWavesBalance(), matcherDataManager.loadReservedBalances(), Observable.just(assets), Function3 { t1: AssetBalanceResponse, t2: Map<String, Long>, t3: AssetBalancesResponse ->
                                    return@Function3 Triple(t1, t2, t3)
                                })
                            }
                            .map { tripple ->
                                val mapDbAssets = assetsFromDb?.associateBy { it.assetId }
                                val savedAssetPrefs = queryAll<AssetBalanceStore>()

                                if (assetsFromDb != null && !assetsFromDb.isEmpty()) {
                                    // merge db data and API data
                                    tripple.third.balances.forEachIndexed { index, assetBalance ->
                                        val dbAsset = mapDbAssets?.get(assetBalance.assetId)
                                        dbAsset?.let {
                                            assetBalance.issueTransaction?.name =
                                                    it.issueTransaction?.name
                                            assetBalance.issueTransaction?.quantity =
                                                    it.issueTransaction?.quantity
                                            assetBalance.issueTransaction?.decimals =
                                                    it.issueTransaction?.decimals
                                            assetBalance.issueTransaction?.timestamp =
                                                    it.issueTransaction?.timestamp
                                            assetBalance.isFiatMoney = it.isFiatMoney
                                            assetBalance.isGateway = it.isGateway
                                            assetBalance.isSpam = it.isSpam

                                            val assetPref = savedAssetPrefs.firstOrNull {
                                                it.assetId == assetBalance.assetId
                                            }
                                            assetBalance.isFavorite = assetPref?.isFavorite
                                                    ?: it.isFavorite
                                            assetBalance.position = assetPref?.position
                                                    ?: it.position
                                            assetBalance.isHidden = assetPref?.isHidden
                                                    ?: it.isHidden
                                        }
                                    }
                                }

                                findElementsInDbWithZeroBalancesAndDelete(assetsFromDb, tripple)

                                tripple.third.balances.forEachIndexed { index, assetBalance ->
                                    val assetPref = savedAssetPrefs.firstOrNull { it.assetId == assetBalance.assetId }
                                    assetBalance.isFavorite = assetPref?.isFavorite
                                            ?: assetBalance.isFavorite
                                    assetBalance.position = assetPref?.position
                                            ?: assetBalance.position
                                    assetBalance.isHidden = assetPref?.isHidden
                                            ?: assetBalance.isHidden


                                    assetBalance.inOrderBalance = tripple.second[assetBalance.assetId]
                                            ?: 0L

                                    assetBalance.isSpam = spamAssets.any {
                                        it.assetId == assetBalance.assetId
                                    }
                                    if (assetBalance.isSpam) {
                                        assetBalance.isFavorite = false
                                    }

                                    if (savedAssetPrefs.isEmpty()) {
                                        mapDbAssets?.let {
                                            if (mapDbAssets[assetBalance.assetId] == null && assetBalance.isMyWavesToken()) {
                                                assetBalance.isFavorite = true
                                            }
                                        }
                                    }
                                }

                                if (tripple.third.balances.any { it.position != -1 }) {
                                    tripple.third.balances.forEach {
                                        if (it.position == -1) {
                                            it.position = tripple.third.balances.size + 1
                                        }
                                    }
                                }

                                AssetBalanceDb.convertToDb(tripple.third.balances).saveAll()
                                AssetBalanceStore.saveAssetBalanceStore(tripple.third.balances)

                                val allAssets = AssetBalanceDb.convertFromDb(queryAll())
                                trackZeroBalances(allAssets)

                                // clear wallet from unimportant assets for new imported wallets
                                return@map ClearAssetsHelper.clearUnimportantAssets(prefsUtil, allAssets.toMutableList(), fromAPI = true)
                            }
                            .subscribeOn(Schedulers.io())
                }
    }

    private fun trackZeroBalances(balances: List<AssetBalanceResponse>) {
        val generalAssets = balances.filter { it.isGateway || it.isWaves() }.toMutableList()
        analyticAssetManager.trackFromZeroBalances(generalAssets)
    }

    private fun findElementsInDbWithZeroBalancesAndDelete(assetsFromDb: List<AssetBalanceResponse>?, tripple: Triple<AssetBalanceResponse, Map<String, Long>, AssetBalancesResponse>) {
        if (assetsFromDb?.size != tripple.third.balances.size) {
            val dbIds = assetsFromDb?.mapTo(ArrayList()) { it.assetId }
            val apiIds = tripple.third.balances.mapTo(ArrayList()) { it.assetId }
            val offsetAsset = dbIds?.minus(apiIds)

            offsetAsset?.forEach { id ->
                if (id.isNotEmpty()) {
                    val assetBalance = queryFirst<AssetBalanceDb> { equalTo("assetId", id) }
                    if (assetBalance?.isGateway == false) {
                        assetBalance.delete { equalTo("assetId", id) }
                    } else {
                        assetBalance?.balance = 0
                        assetBalance?.save()
                    }
                }
            }
        }
    }

    fun loadWavesBalance(): Observable<AssetBalanceResponse> {
        return Observable.zip(
                // load total balance
                nodeService.wavesBalance(getAddress()).map {
                    return@map it.balance
                },
                // load leased balance
                activeLeasing().map {
                    return@map it.sumByLong { it.amount }
                },
                // load in order balance
                matcherDataManager.loadReservedBalances()
                        .map {
                            return@map it[Constants.WAVES_ASSET_INFO.name] ?: 0L
                        },
                Function3 { totalBalance: Long, leasedBalance: Long, inOrderBalance: Long ->
                    val currentWaves = loadDbWavesBalance()
                    currentWaves.balance = totalBalance
                    currentWaves.leasedBalance = leasedBalance
                    currentWaves.inOrderBalance = inOrderBalance
                    AssetBalanceDb(currentWaves).save()
                    return@Function3 currentWaves
                })
    }

    fun createAlias(createAliasRequest: AliasRequest): Observable<AliasResponse> {
        createAliasRequest.senderPublicKey = getPublicKeyStr()
        createAliasRequest.timestamp = EnvironmentManager.getTime()
        App.getAccessManager().getWallet()?.privateKey.notNull {
            createAliasRequest.sign(it)
        }
        return nodeService.createAlias(createAliasRequest)
                .map {
                    it.address = getAddress()
                    AliasDb(it).save()
                    return@map it
                }
                .doOnNext {
                    rxEventBus.post(Events.UpdateAssetsBalance())
                }
    }

    fun cancelLeasing(cancelLeasingRequest: CancelLeasingRequest): Observable<TransactionResponse> {
        cancelLeasingRequest.senderPublicKey = getPublicKeyStr()
        cancelLeasingRequest.timestamp = EnvironmentManager.getTime()
        App.getAccessManager().getWallet()?.privateKey.notNull {
            cancelLeasingRequest.sign(it)
        }
        return nodeService.cancelLeasing(cancelLeasingRequest)
                .map {
                    val first = queryFirst<TransactionDb> {
                        equalTo("id", cancelLeasingRequest.leaseId)
                    }
                    first?.status = LeasingStatus.CANCELED.status
                    first?.save()
                    return@map it
                }
                .doOnNext {
                    rxEventBus.post(Events.UpdateAssetsBalance())
                }
    }

    fun startLeasing(
            createLeasingRequest: CreateLeasingRequest,
            recipientIsAlias: Boolean,
            fee: Long
    ): Observable<TransactionResponse> {
        createLeasingRequest.senderPublicKey = getPublicKeyStr()
        createLeasingRequest.fee = fee
        createLeasingRequest.timestamp = EnvironmentManager.getTime()

        App.getAccessManager().getWallet()?.privateKey.notNull {
            createLeasingRequest.sign(it, recipientIsAlias)
        }
        return nodeService.createLeasing(createLeasingRequest)
                .doOnNext {
                    rxEventBus.post(Events.UpdateAssetsBalance())
                }
    }

    fun loadTransactions(currentLoadTransactionLimitPerRequest: Int): Observable<List<TransactionResponse>> {
        return Observable.interval(0, 15, TimeUnit.SECONDS)
                .retry(3)
                .flatMap {
                    if (ProcessLifecycleOwner.get().lifecycle.currentState == Lifecycle.State.RESUMED) {
                        return@flatMap nodeService.transactionList(getAddress(), currentLoadTransactionLimitPerRequest)
                                .map { r -> r[0] }
                    } else {
                        return@flatMap Observable.just(listOf<TransactionResponse>())
                    }
                }
                .onErrorResumeNext(Observable.empty())
    }

    fun loadLightTransactions(): Observable<List<TransactionResponse>> {
        return nodeService.transactionList(getAddress(), 100)
                .map { r -> r[0] }
    }

    fun currentBlocksHeight(): Observable<HeightResponse> {
        return Observable.interval(0, 60, TimeUnit.SECONDS)
                .retry(3)
                .flatMap {
                    return@flatMap nodeService.currentBlocksHeight()
                }
                .map {
                    preferencesHelper.currentBlocksHeight = it.height
                    return@map it
                }
                .onErrorResumeNext(Observable.empty())
    }

    private fun activeLeasing(): Observable<List<TransactionResponse>> {
        return nodeService.activeLeasing(getAddress())
                .map {
                    return@map it.filter {
                        it.asset = Constants.WAVES_ASSET_INFO
                        it.transactionTypeId = TransactionUtil.getTransactionType(it)
                        it.transactionTypeId == Constants.ID_STARTED_LEASING_TYPE
                                && it.sender == App.getAccessManager().getWallet()?.address
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
                .doOnNext {
                    rxEventBus.post(Events.UpdateAssetsBalance())
                }
    }

    fun scriptAddressInfo(address: String = getAddress() ?: ""): Observable<ScriptInfoResponse> {
        return nodeService.scriptAddressInfo(address)
                .doOnNext {
                    prefsUtil.setValue(PrefsUtil.KEY_SCRIPTED_ACCOUNT, it.extraFee != 0L)
                }
    }

    fun assetDetails(assetId: String?): Observable<AssetsDetailsResponse> {
        return if (TextUtils.isEmpty(assetId) || assetId == Constants.WAVES_ASSET_ID_FILLED) {
            Observable.just(AssetsDetailsResponse(assetId = Constants.WAVES_ASSET_ID_FILLED, scripted = false))
        } else {
            nodeService.assetDetails(assetId!!)
        }
    }

    fun getCommissionForPair(amountAsset: String?, priceAsset: String?): Observable<Long> {
        return Observable.zip(
                githubDataManager.getGlobalCommission(),
                assetDetails(amountAsset),
                assetDetails(priceAsset),
                Function3 { t1: GlobalTransactionCommissionResponse,
                            t2: AssetsDetailsResponse,
                            t3: AssetsDetailsResponse ->
                    return@Function3 Triple(t1, t2, t3)
                })
                .flatMap {
                    val commission = it.first
                    val amountAssetsDetails = it.second
                    val priceAssetsDetails = it.third
                    val params = GlobalTransactionCommissionResponse.ParamsResponse()
                    params.transactionType = TransactionResponse.EXCHANGE
                    params.smartPriceAsset = priceAssetsDetails.scripted
                    params.smartAmountAsset = amountAssetsDetails.scripted
                    return@flatMap Observable.just(TransactionUtil.countCommission(commission, params))
                }

    }

    fun addressAssetBalance(address: String, assetId: String): Observable<AddressAssetBalanceResponse> {
        return nodeService.addressAssetBalance(address, assetId)
    }
}
