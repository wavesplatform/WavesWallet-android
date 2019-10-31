/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.ProcessLifecycleOwner
import android.text.TextUtils
import com.vicpin.krealmextensions.*
import com.wavesplatform.sdk.model.request.node.*
import com.wavesplatform.sdk.model.response.node.*
import com.wavesplatform.sdk.model.response.node.transaction.*
import com.wavesplatform.sdk.utils.WavesConstants
import com.wavesplatform.sdk.utils.notNull
import com.wavesplatform.sdk.utils.sumByLong
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.analytics.AnalyticAssetManager
import com.wavesplatform.wallet.v2.data.helpers.ClearAssetsHelper
import com.wavesplatform.wallet.v2.data.manager.base.BaseServiceManager
import com.wavesplatform.wallet.v2.data.model.db.AliasDb
import com.wavesplatform.wallet.v2.data.model.db.AssetBalanceDb
import com.wavesplatform.wallet.v2.data.model.db.SpamAssetDb
import com.wavesplatform.wallet.v2.data.model.db.TransactionDb
import com.wavesplatform.wallet.v2.data.model.db.userdb.AssetBalanceStoreDb
import com.wavesplatform.wallet.v2.data.model.local.LeasingStatus
import com.wavesplatform.wallet.v2.data.model.service.configs.GlobalTransactionCommissionResponse
import com.wavesplatform.wallet.v2.data.model.service.configs.SpamAssetResponse
import com.wavesplatform.wallet.v2.util.*
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.schedulers.Schedulers
import java.lang.NullPointerException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

@Singleton
class NodeServiceManager @Inject constructor() : BaseServiceManager() {

    @Inject
    lateinit var dataServiceManager: DataServiceManager
    @Inject
    lateinit var githubServiceManager: GithubServiceManager
    @Inject
    lateinit var matcherServiceManager: MatcherServiceManager
    @Inject
    lateinit var analyticAssetManager: AnalyticAssetManager

    fun transactionsBroadcast(tx: TransferTransaction): Observable<TransferTransactionResponse> {
        tx.sign(App.accessManager.getWallet()?.seedStr ?: "")
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

    fun loadAssets(assetsFromDb: List<AssetBalanceResponse>? = null)
            : Observable<Pair<List<AssetBalanceResponse>, List<SpamAssetResponse>>> {
        return githubServiceManager.loadSpamAssets()
                .flatMap { spamAssets ->
                    return@flatMap nodeService.assetsBalance(getAddress())
                            .flatMap { assets ->
                                return@flatMap Observable.zip(
                                        loadWavesBalance(),
                                        matcherServiceManager.loadReservedBalances(),
                                        Observable.just(assets),
                                        Function3 { wavesBalance: AssetBalanceResponse,
                                                    reservedBalances: Map<String, Long>,
                                                    assetBalances: AssetBalancesResponse ->
                                            return@Function3 Triple(wavesBalance, reservedBalances, assetBalances)
                                        })
                            }
                            .map { tripple ->
                                val mapDbAssets = assetsFromDb?.associateBy { it.assetId }
                                val spamAssetsMap = spamAssets.associateBy { it.assetId }
                                val savedAssetPrefs = queryAll<AssetBalanceStoreDb>()

                                if (assetsFromDb != null && assetsFromDb.isNotEmpty()) {
                                    mergeNetDbData(tripple, mapDbAssets, savedAssetPrefs)
                                }

                                findElementsInDbWithZeroBalancesAndDelete(assetsFromDb, tripple)

                                tripple.third.balances.forEachIndexed { index, assetBalance ->
                                    val assetPref = savedAssetPrefs.firstOrNull { it.assetId == assetBalance.assetId }

                                    markWavesAndMyTokensAsFavorite(mapDbAssets, assetBalance)

                                    assetBalance.isFavorite = assetPref?.isFavorite
                                            ?: assetBalance.isFavorite
                                    assetBalance.position = assetPref?.position
                                            ?: assetBalance.position
                                    assetBalance.isHidden = assetPref?.isHidden
                                            ?: assetBalance.isHidden


                                    assetBalance.inOrderBalance = tripple.second[assetBalance.assetId]
                                            ?: 0L

                                    assetBalance.isSpam = spamAssetsMap[assetBalance.assetId] != null

                                    if (assetBalance.isSpam) {
                                        assetBalance.isFavorite = false
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
                                AssetBalanceStoreDb.saveAssetBalanceStore(tripple.third.balances)

                                val allAssets = AssetBalanceDb.convertFromDb(queryAll())
                                trackZeroBalances(allAssets)

                                // clear wallet from unimportant assets for new imported wallets
                                return@map Pair(ClearAssetsHelper.clearUnimportantAssets(
                                        prefsUtil, allAssets.toMutableList(), fromAPI = true),
                                        spamAssets)
                            }
                            .subscribeOn(Schedulers.io())
                }
    }

    private fun markWavesAndMyTokensAsFavorite(
            mapDbAssets: Map<String, AssetBalanceResponse>?, assetBalance: AssetBalanceResponse) {
        mapDbAssets?.let {
            if (mapDbAssets[assetBalance.assetId] == null
                    && assetBalance.isMyWavesToken(WavesWallet.getAddress())) {
                assetBalance.isFavorite = true
            }
        }
    }

    private fun mergeNetDbData(
            tripple: Triple<AssetBalanceResponse, Map<String, Long>, AssetBalancesResponse>,
            mapDbAssets: Map<String, AssetBalanceResponse>?,
            savedAssetPrefs: List<AssetBalanceStoreDb>) {
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
                    assetBalance.notNull {
                        if (isGateway(it.assetId) || isFiat(it.assetId)) {
                            it.balance = 0
                            it.save()
                        } else {
                            it.delete { equalTo("assetId", id) }
                        }
                    }
                }
            }
        }
    }

    fun loadWavesBalance(): Observable<AssetBalanceResponse> {
        return Observable.zip(
                // load total balance
                nodeService.addressesBalance(getAddress()).map {
                    return@map it.balance
                },
                // load leased balance
                activeLeasing().map {
                    return@map it.sumByLong { it.amount }
                },
                // load in order balance
                matcherServiceManager.loadReservedBalances()
                        .map {
                            return@map it[WavesConstants.WAVES_ASSET_INFO.name] ?: 0L
                        },
                Function3 { totalBalance: Long, leasedBalance: Long, inOrderBalance: Long ->
                    var currentWaves: AssetBalanceResponse
                    try {
                        currentWaves = loadDbWavesBalance()
                        currentWaves.balance = totalBalance
                        currentWaves.leasedBalance = leasedBalance
                        currentWaves.inOrderBalance = inOrderBalance
                        AssetBalanceDb(currentWaves).save()
                    } catch (exception: NullPointerException) {
                        exception.printStackTrace()
                        currentWaves = AssetBalanceResponse(WavesConstants.WAVES_ASSET_INFO.id)
                    }
                    return@Function3 currentWaves
                })
    }

    fun createAlias(request: AliasTransaction): Observable<AliasTransactionResponse> {
        request.sign(App.accessManager.getWallet()?.seedStr ?: "")
        return nodeService.transactionsBroadcast(request)
                .map {
                    it.address = getAddress()
                    it.own = true
                    AliasDb(it).save()
                    return@map it
                }
                .doOnNext {
                    rxEventBus.post(Events.UpdateAssetsBalance())
                }
    }

    fun cancelLeasing(transaction: LeaseCancelTransaction): Observable<LeaseCancelTransactionResponse> {
        transaction.sign(App.accessManager.getWallet()?.seedStr ?: "")
        return nodeService.transactionsBroadcast(transaction)
                .map {
                    val first = queryFirst<TransactionDb> {
                        equalTo("id", transaction.leaseId)
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
            createLeasingRequest: LeaseTransaction,
            fee: Long
    ): Observable<LeaseTransactionResponse> {
        createLeasingRequest.fee = fee
        createLeasingRequest.sign(App.accessManager.getWallet()?.seedStr ?: "")
        return nodeService.transactionsBroadcast(createLeasingRequest)
                .doOnNext {
                    rxEventBus.post(Events.UpdateAssetsBalance())
                }
    }

    fun loadTransactions(currentLoadTransactionLimitPerRequest: Int): Observable<List<HistoryTransactionResponse>> {
        return Observable.interval(0, 15, TimeUnit.SECONDS)
                .retry(3)
                .flatMap {
                    if (ProcessLifecycleOwner.get().lifecycle.currentState == Lifecycle.State.RESUMED) {
                        return@flatMap nodeService.transactionsAddress(getAddress(), currentLoadTransactionLimitPerRequest)
                                .map { r -> r[0] }
                    } else {
                        return@flatMap Observable.just(listOf<HistoryTransactionResponse>())
                    }
                }
                .onErrorResumeNext(Observable.empty())
    }

    fun loadLightTransactions(): Observable<List<HistoryTransactionResponse>> {
        return nodeService.transactionsAddress(getAddress(), 100)
                .map { r -> r[0] }
    }

    fun currentBlocksHeight(): Observable<HeightResponse> {
        return Observable.interval(0, 60, TimeUnit.SECONDS)
                .retry(3)
                .flatMap {
                    return@flatMap nodeService.blockHeight()
                }
                .map {
                    preferencesHelper.currentBlocksHeight = it.height
                    return@map it
                }
                .onErrorResumeNext(Observable.empty())
    }

    private fun activeLeasing(): Observable<List<HistoryTransactionResponse>> {
        return nodeService.leasingActive(getAddress())
                .map {
                    return@map it.filter {
                        it.asset = WavesConstants.WAVES_ASSET_INFO
                        it.transactionTypeId = getTransactionType(
                                it, WavesWallet.getAddress())
                        it.transactionTypeId == Constants.ID_STARTED_LEASING_TYPE
                                && it.sender == App.accessManager.getWallet()?.address
                    }
                }
                .flatMap {
                    return@flatMap Observable.fromIterable(it)
                            .flatMap { transaction ->
                                if (transaction.recipient.contains("aliasBytes")) {
                                    val aliasName = transaction.recipient.substringAfterLast(":")
                                    return@flatMap dataServiceManager.loadAlias(aliasName)
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

    fun burn(burn: BurnTransaction, totalBurn: Boolean): Observable<BurnTransactionResponse> {
        return nodeService.transactionsBroadcast(burn)
                .doOnNext {
                    if (totalBurn) {
                        delete<AssetBalanceDb> { equalTo("assetId", burn.assetId) }
                    }
                    rxEventBus.post(Events.UpdateAssetsBalance())
                }
    }

    fun scriptAddressInfo(address: String = getAddress()): Observable<ScriptInfoResponse> {
        return nodeService.scriptInfo(address)
                .doOnNext {
                    prefsUtil.setValue(PrefsUtil.KEY_SCRIPTED_ACCOUNT, it.extraFee != 0L)
                }
    }

    fun assetDetails(assetId: String?): Observable<AssetsDetailsResponse> {
        return if (TextUtils.isEmpty(assetId) || assetId == WavesConstants.WAVES_ASSET_ID_FILLED) {
            Observable.just(AssetsDetailsResponse(assetId = WavesConstants.WAVES_ASSET_ID_FILLED, scripted = false))
        } else {
            nodeService.assetDetails(assetId!!)
        }
    }

    fun getCommissionForPair(amountAsset: String?, priceAsset: String?): Observable<Long> {
        return Observable.zip(
                githubServiceManager.getGlobalCommission(),
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
                    params.transactionType = BaseTransaction.EXCHANGE
                    params.smartPriceAsset = priceAssetsDetails.scripted
                    params.smartAmountAsset = amountAssetsDetails.scripted
                    return@flatMap Observable.just(TransactionCommissionUtil.countCommission(commission, params))
                }

    }

    fun addressAssetBalance(address: String, assetId: String): Observable<AddressAssetBalanceResponse> {
        return nodeService.assetsBalance(address, assetId)
    }

    fun transaction(transaction: InvokeScriptTransaction): Observable<InvokeScriptTransactionResponse> {
        return nodeService.transactionsBroadcast(transaction)
    }
}
