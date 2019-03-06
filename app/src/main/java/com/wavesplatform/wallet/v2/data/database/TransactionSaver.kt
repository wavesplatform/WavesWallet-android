package com.wavesplatform.wallet.v2.data.database

import com.vicpin.krealmextensions.*
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.manager.ApiDataManager
import com.wavesplatform.wallet.v2.data.manager.NodeDataManager
import com.wavesplatform.wallet.v2.data.model.local.LeasingStatus
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetInfo
import com.wavesplatform.wallet.v2.data.model.remote.response.SpamAsset
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.data.model.remote.response.TransactionType
import com.wavesplatform.wallet.v2.util.*
import io.reactivex.disposables.CompositeDisposable
import pyxis.uzuki.live.richutilskt.utils.runAsync
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import javax.inject.Inject

@Deprecated("Temp class for saving transactions, should refactor")
class TransactionSaver @Inject constructor() {

    @Inject
    lateinit var rxEventBus: RxEventBus
    @Inject
    lateinit var nodeDataManager: NodeDataManager
    @Inject
    lateinit var apiDataManager: ApiDataManager
    @Inject
    lateinit var transactionUtil: TransactionUtil
    private var allAssets = arrayListOf<AssetInfo>()
    private var subscriptions: CompositeDisposable = CompositeDisposable()
    private var currentLimit = DEFAULT_LIMIT
    private var prevLimit = DEFAULT_LIMIT
    private var needCheckToUpdateBalance = false

    fun saveTransactions(
        sortedList: List<Transaction>,
        limit: Int = DEFAULT_LIMIT,
        changeListener: OnTransactionLimitChangeListener? = null
    ) {
        currentLimit = limit
        if (sortedList.isEmpty() || limit < 1) {
            rxEventBus.post(Events.NeedUpdateHistoryScreen())
            return
        }

        runAsync {
            // check if exist last transaction
            queryAsync<Transaction>({ equalTo("id", sortedList[sortedList.size - 1].id) },
                    {
                        if (it.isEmpty()) {
                            // all list is new, need load more

                            if (currentLimit >= MAX_LIMIT) currentLimit = 50

                            if (prevLimit == DEFAULT_LIMIT) {
                                saveToDb(sortedList)
                            } else {
                                try {
                                    saveToDb(sortedList.subList(prevLimit - 1, sortedList.size - 1))
                                } catch (e: Exception) {
                                    currentLimit = 50
                                    runOnUiThread {
                                        rxEventBus.post(Events.StopUpdateHistoryScreen())
                                    }
                                }
                            }

                            // save previous count of loaded transactions for future cut list
                            prevLimit = currentLimit

                            // multiply current limit
                            currentLimit *= 2

                            changeListener.notNull { listener ->
                                listener.onChange(currentLimit)
                            }
                        } else {
                            // check if exist first transaction
                            queryAsync<Transaction>({ equalTo("id", sortedList[0].id) },
                                    {
                                        if (it.isEmpty()) {
                                            // only few new transaction
                                            needCheckToUpdateBalance = true
                                            saveToDb(sortedList)
                                        } else {
                                            runOnUiThread {
                                                rxEventBus.post(Events.StopUpdateHistoryScreen())
                                            }
                                        }
                                    })
                        }
                    })
        }
    }

    private fun saveToDb(transactions: List<Transaction>) {

        // grab all assetsIds
        val tempGrabbedAssets = mutableListOf<String?>()
        transactions.forEach { transition ->
            transition.order1?.assetPair?.notNull { assetPair ->
                tempGrabbedAssets.add(assetPair.amountAsset)
                tempGrabbedAssets.add(assetPair.priceAsset)
            }
            tempGrabbedAssets.add(transition.assetId)
            tempGrabbedAssets.add(transition.feeAssetId)
        }

        // filter all without duplicates
        val allTransactionsAssets = tempGrabbedAssets.asSequence()
                .filter { !it.isNullOrEmpty() }
                .distinct()
                .toMutableList()

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

                            if (trans.feeAssetId.isNullOrEmpty()) {
                                trans.feeAssetObject = Constants.wavesAssetInfo
                            } else {
                                trans.feeAssetObject = allAssets.firstOrNull { it.id == trans.feeAssetId }
                            }

                            if (trans.recipient.contains("alias")) {
                                val aliasName = trans.recipient.substringAfterLast(":")
                                aliasName.notNull {
                                    subscriptions.add(apiDataManager.loadAlias(it)
                                            .compose(RxUtil.applyObservableDefaultSchedulers())
                                            .subscribe {
                                                trans.recipientAddress = it.address
                                                trans.transactionTypeId = transactionUtil.getTransactionType(trans)
                                                trans.save()
                                            })
                                }
                            } else {
                                trans.recipientAddress = trans.recipient
                            }

                            trans.transfers.forEach { trans ->
                                if (trans.recipient.contains("alias")) {
                                    val aliasName = trans.recipient.substringAfterLast(":")
                                    aliasName.notNull {
                                        subscriptions.add(apiDataManager.loadAlias(it)
                                                .compose(RxUtil.applyObservableDefaultSchedulers())
                                                .subscribe {
                                                    trans.recipientAddress = it.address
                                                    trans.save()
                                                })
                                    }
                                } else {
                                    trans.recipientAddress = trans.recipient
                                }
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

                        if (needCheckToUpdateBalance) {
                            needCheckToUpdateBalance = false
                            val needUpdateBalance = transactions.any {
                                it.transactionType() == TransactionType.SPAM_RECEIVE_TYPE || it.transactionType() == TransactionType.RECEIVED_TYPE ||
                                        it.transactionType() == TransactionType.MASS_SPAM_RECEIVE_TYPE || it.transactionType() == TransactionType.MASS_RECEIVE_TYPE ||
                                        it.transactionType() == TransactionType.EXCHANGE_TYPE
                            }
                            if (needUpdateBalance) {
                                rxEventBus.post(Events.UpdateAssetsBalance())
                            }
                        }

                        // check old started leasing transaction correct status
                        val canceledLeasingTransactions = transactions
                                .filter { it.transactionType() == TransactionType.CANCELED_LEASING_TYPE }
                        if (canceledLeasingTransactions.isNotEmpty()) {
                            canceledLeasingTransactions.forEach {
                                val first = queryFirst<Transaction> { equalTo("id", it.leaseId) }
                                if (first?.status != LeasingStatus.CANCELED.status) {
                                    first?.status = LeasingStatus.CANCELED.status
                                    first?.save()
                                }
                            }
                        }

                        transactions.saveAll()
                        runOnUiThread {
                            rxEventBus.post(Events.NeedUpdateHistoryScreen())
                        }
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

    companion object {
        const val MAX_LIMIT = 1000
        const val DEFAULT_LIMIT = 100
    }

    interface OnTransactionLimitChangeListener {
        fun onChange(limit: Int)
    }
}