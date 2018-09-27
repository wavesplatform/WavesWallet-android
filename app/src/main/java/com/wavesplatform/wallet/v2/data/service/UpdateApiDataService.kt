package com.wavesplatform.wallet.v2.data.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.common.base.Predicates.equalTo
import com.vicpin.krealmextensions.*
import com.wavesplatform.wallet.R.string.asset
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.manager.ApiDataManager
import com.wavesplatform.wallet.v2.data.manager.NodeDataManager
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetInfo
import com.wavesplatform.wallet.v2.data.model.remote.response.SpamAsset
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.util.RxEventBus
import com.wavesplatform.wallet.v2.util.RxUtil
import com.wavesplatform.wallet.v2.util.TransactionUtil
import com.wavesplatform.wallet.v2.util.notNull
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import pyxis.uzuki.live.richutilskt.utils.runAsync
import javax.inject.Inject


class UpdateApiDataService : Service() {

    @Inject
    lateinit var nodeDataManager: NodeDataManager
    @Inject
    lateinit var apiDataManager: ApiDataManager
    @Inject
    lateinit var transactionUtil: TransactionUtil
    @Inject
    lateinit var rxEventBus: RxEventBus
    @Inject
    lateinit var prefsUtil: PrefsUtil
    var subscriptions: CompositeDisposable = CompositeDisposable()
    var allAssets = arrayListOf<AssetInfo>()

    var currentLimit = 100
    var prevLimit = 100
    var defaultLimit = 100
    var maxLimit = 10000

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()

        subscriptions.add(rxEventBus.filteredObservable(Events.SpamFilterStateChanged::class.java)
                .subscribe {
                    reloadTransactionsAfterSpamFilterStateChanged()
                })
    }

    private fun reloadTransactionsAfterSpamFilterStateChanged() {
        runAsync {
            val singleData: Single<List<Transaction>> = if (prefsUtil.getValue(PrefsUtil.KEY_DISABLE_SPAM_FILTER, false)) {
                queryAsSingle<Transaction> {
                    `in`("transactionTypeId", arrayOf(Constants.ID_MASS_SPAM_RECEIVE_TYPE, Constants.ID_SPAM_RECEIVE_TYPE))
                }
            } else {
                queryAsSingle<Transaction> {
                    `in`("transactionTypeId", arrayOf(Constants.ID_RECEIVED_TYPE, Constants.ID_MASS_RECEIVE_TYPE))
                }
            }
            subscriptions.add(Observable.zip(
                    singleData.toObservable(),
                    queryAllAsSingle<SpamAsset>().toObservable()
                            .map { spamListFromDb ->
                                if (prefsUtil.getValue(PrefsUtil.KEY_DISABLE_SPAM_FILTER, false)) {
                                    return@map listOf<SpamAsset>()
                                } else {
                                    return@map spamListFromDb
                                }
                            },
                    queryAllAsSingle<AssetInfo>().toObservable(),
                    Function3 { t1: List<Transaction>, t2: List<SpamAsset>, t3: List<AssetInfo> ->
                        return@Function3 Triple(t1, t2, t3)
                    })
                    .subscribe { pairOfData ->
                        val spamListFromDb = pairOfData.second
                        val transactionListFromDb = pairOfData.first
                        val assetsInfoListFromDb = pairOfData.third

                        assetsInfoListFromDb.forEach { assetInfo ->
                            assetInfo.isSpam = spamListFromDb.any { it.assetId == assetInfo.id }
                        }

                        transactionListFromDb.forEach { transaction ->
                            transaction.asset = assetsInfoListFromDb.firstOrNull { it.id == transaction.assetId }
                            transaction.transactionTypeId = transactionUtil.getTransactionType(transaction)
                        }

                        transactionListFromDb.saveAll()

                        rxEventBus.post(Events.NeedUpdateHistoryScreen())
                    })
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val transaction = queryFirst<Transaction>()
        if (transaction == null) {
            nodeDataManager.currentLoadTransactionLimitPerRequest = maxLimit
            currentLimit = maxLimit
        }
        subscriptions.add(nodeDataManager.loadTransactions()
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    if (it.isNotEmpty()) {
                        val sortedList = it.sortedByDescending { it.timestamp }

                        runAsync {
                            // check if exist last transaction
                            queryAsync<Transaction>({ equalTo("id", sortedList[sortedList.size - 1].id) }, {
                                if (it.isEmpty()) {
                                    // all list is new, need load more

                                    if (currentLimit >= maxLimit) currentLimit = 50

                                    if (prevLimit == defaultLimit) {
                                        saveToDb(sortedList)
                                    } else {
                                        try {
                                            saveToDb(sortedList.subList(prevLimit - 1, sortedList.size - 1))
                                        } catch (e: Exception) {
                                            currentLimit = 50
                                        }
                                    }

                                    // save previous count of loaded transactions for future cut list
                                    prevLimit = currentLimit

                                    // multiply current limit
                                    currentLimit *= 2
                                    nodeDataManager.currentLoadTransactionLimitPerRequest = currentLimit

                                } else {
                                    // check if exist first transaction
                                    queryAsync<Transaction>({ equalTo("id", sortedList[0].id) }, {
                                        if (it.isEmpty()) {
                                            // only few new transaction
                                            saveToDb(sortedList)
                                        }
                                    })
                                }
                            })
                        }
                    }
                }, {
                    it.printStackTrace()
                }))
        subscriptions.add(nodeDataManager.currentBlocksHeight()
                .subscribe {

                })
        return Service.START_NOT_STICKY
    }

    private fun saveToDb(transactions: List<Transaction>) {

        // grab all assetsIds without duplicates
        val tempGrabbedAssets = mutableListOf<String?>()
        transactions.forEach { transition ->
            transition.order1?.assetPair?.notNull { assetPair ->
                tempGrabbedAssets.add(assetPair.amountAsset)
                tempGrabbedAssets.add(assetPair.priceAsset)
            }
            tempGrabbedAssets.add(transition.assetId)
            tempGrabbedAssets.add(transition.feeAssetId)
        }

        val allTransactionsAssets = tempGrabbedAssets.asSequence().filter { !it.isNullOrEmpty() }.distinct().toList()

        subscriptions.add(apiDataManager.assetsInfoByIds(allTransactionsAssets)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe {
                    mergeAndSaveAllAssets(ArrayList(it)) { assetsInfo ->
                        transactions.forEach { trans ->
                            if (trans.assetId.isNullOrEmpty()) {
                                trans.asset = Constants.wavesAssetInfo
                            } else {
                                trans.asset = allAssets.firstOrNull { it.id == trans.assetId }
                            }

                            if (trans.recipient.contains("alias")) {
                                val aliasName = trans.recipient.substringAfterLast(":")
                                aliasName.notNull {
                                    subscriptions.add(apiDataManager.loadAlias(it)
                                            .compose(RxUtil.applyObservableDefaultSchedulers())
                                            .subscribe {
                                                trans.recipientAddress = it.address
                                            })
                                }
                            } else {
                                trans.recipientAddress = trans.recipient
                            }

                            if (trans.order1 != null) {
                                val amountAsset =
                                        if (trans.order1?.assetPair?.amountAsset.isNullOrEmpty()) {
                                            Constants.wavesAssetInfo
                                        } else {
                                            allAssets.firstOrNull { it.id == trans.order1?.assetPair?.amountAsset }
                                        }
                                val priceAsset =
                                        if (trans.order1?.assetPair?.priceAsset.isNullOrEmpty()) {
                                            Constants.wavesAssetInfo
                                        } else {
                                            allAssets.firstOrNull { it.id == trans.order1?.assetPair?.priceAsset }
                                        }


                                trans.order1?.assetPair?.amountAssetObject = amountAsset
                                trans.order1?.assetPair?.priceAssetObject = priceAsset
                                trans.order2.notNull {
                                    it.assetPair?.amountAssetObject = amountAsset
                                    it.assetPair?.priceAssetObject = priceAsset
                                }
                            }
                            trans.transactionTypeId = transactionUtil.getTransactionType(trans)
                        }
                        transactions.saveAll()
                        rxEventBus.post(Events.NeedUpdateHistoryScreen())
                    }
                })

    }

    private fun mergeAndSaveAllAssets(arrayList: ArrayList<AssetInfo>, callback: (ArrayList<AssetInfo>) -> Unit) {
        runAsync {
            queryAllAsync<SpamAsset> { spams ->
                arrayList.forEach { newAsset ->
                    if (!allAssets.any { it.id == newAsset.id }) {
                        if (spams.any { it.assetId == newAsset.id }) {
                            newAsset.isSpam = true
                        }
                        allAssets.add(newAsset)
                    }
                }
                callback(allAssets)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        subscriptions.clear()
        super.onDestroy()
    }

}