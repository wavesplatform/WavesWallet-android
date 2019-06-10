/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.database

import com.vicpin.krealmextensions.*
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.Events
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

class TransactionSaver(private var nodeDataManager: NodeDataManager,
                       private var rxEventBus: RxEventBus) {

    private var transactionUtil = TransactionUtil()
    private var allAssets = arrayListOf<AssetInfo>()
    private var subscriptions: CompositeDisposable = CompositeDisposable()
    private var currentLimit = DEFAULT_LIMIT
    private var prevLimit = DEFAULT_LIMIT
    private var checkAssetsUpdateBalance = false
    private var changeListener: OnTransactionLimitChangeListener? = null

    fun save(transactions: List<Transaction>, limit: Int = DEFAULT_LIMIT,
             changeListener: OnTransactionLimitChangeListener? = null) {

        val sortTransactions = transactions.sortedByDescending { it.timestamp }

        this.currentLimit = limit
        this.changeListener = changeListener

        if (App.getAccessManager().getWallet() == null
                || sortTransactions.isEmpty()
                || limit < 1) {
            rxEventBus.post(Events.NeedUpdateHistoryScreen())
            return
        }

        runAsync {
            val lastSavedTransaction = queryFirst<Transaction> {
                equalTo("id", sortTransactions.last().id)
            }
            if (lastSavedTransaction == null) {
                saveTransactionsAndIncreaseLimit(sortTransactions)
            } else {
                saveTransactions(sortTransactions)
            }
        }
    }

    private fun saveTransactions(sortTransactions: List<Transaction>) {
        val firstSavedTransaction = queryFirst<Transaction> {
            equalTo("id", sortTransactions[0].id)
        }
        if (firstSavedTransaction == null) {
            checkAssetsUpdateBalance = true
            saveToDb(sortTransactions)
        } else {
            runOnUiThread {
                rxEventBus.post(Events.StopUpdateHistoryScreen())
            }
        }
    }

    private fun saveTransactionsAndIncreaseLimit(transactions: List<Transaction>) {
        if (currentLimit >= MAX_LIMIT) {
            currentLimit = 50
        }

        if (prevLimit == DEFAULT_LIMIT) {
            saveToDb(transactions)
        } else {
            try {
                saveToDb(transactions.subList(prevLimit - 1, transactions.size - 1))
            } catch (e: Exception) {
                currentLimit = 50
                runOnUiThread {
                    rxEventBus.post(Events.StopUpdateHistoryScreen())
                }
            }
        }

        prevLimit = currentLimit
        currentLimit *= 2
        changeListener?.onChange(currentLimit)
    }

    private fun saveToDb(transactions: List<Transaction>) {
        subscriptions.add(nodeDataManager.apiDataManager.assetsInfoByIds(grabAssetIds(transactions))
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe {
                    mergeAndSaveAllAssets(ArrayList(it)) {
                        transactions.forEach { transaction ->
                            setAssetInfo(transaction)
                            setAliasesAddresses(transaction)
                            setExchangePriceAmount(transaction)
                            transaction.transactionTypeId = transactionUtil.getTransactionType(transaction)
                        }

                        broadcastUpdateAssetsBalance(transactions)
                        checkLeasingStatus(transactions)
                        transactions.saveAll()
                        runOnUiThread {
                            rxEventBus.post(Events.NeedUpdateHistoryScreen())
                        }
                    }
                })
    }

    private fun grabAssetIds(transactions: List<Transaction>): MutableList<String?> {
        val allGrabbedAssetsIds = mutableListOf<String?>()

        transactions.forEach { transition ->
            transition.order1?.assetPair?.notNull { assetPair ->
                allGrabbedAssetsIds.add(assetPair.amountAsset)
                allGrabbedAssetsIds.add(assetPair.priceAsset)
            }
            if (!transition.payment.isNullOrEmpty()) {
                transition.payment.first()?.notNull { payment ->
                    allGrabbedAssetsIds.add(payment.assetId)
                }
            }
            allGrabbedAssetsIds.add(transition.assetId)
            allGrabbedAssetsIds.add(transition.feeAssetId)
        }

        // filter unique
        return allGrabbedAssetsIds
                .asSequence()
                .filter { !it.isNullOrEmpty() }
                .distinct()
                .toMutableList()
    }

    private fun broadcastUpdateAssetsBalance(transactions: List<Transaction>) {
        if (checkAssetsUpdateBalance) {
            checkAssetsUpdateBalance = false
            val sendBroadcastUpdateAssetsBalance = transactions.any {
                it.transactionType() == TransactionType.SPAM_RECEIVE_TYPE
                        || it.transactionType() == TransactionType.RECEIVED_TYPE
                        || it.transactionType() == TransactionType.MASS_SPAM_RECEIVE_TYPE
                        || it.transactionType() == TransactionType.MASS_RECEIVE_TYPE
                        || it.transactionType() == TransactionType.EXCHANGE_TYPE
            }
            if (sendBroadcastUpdateAssetsBalance) {
                rxEventBus.post(Events.UpdateAssetsBalance())
            }
        }
    }

    private fun checkLeasingStatus(transactions: List<Transaction>) {
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
    }

    private fun setAssetInfo(trans: Transaction) {
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

        if (!trans.payment.isNullOrEmpty()) {
            trans.payment.first()?.let { payment ->
                if (payment.assetId.isNullOrEmpty()) {
                    payment.asset = Constants.wavesAssetInfo
                } else {
                    payment.asset = allAssets.firstOrNull { it.id == payment.assetId }
                }
            }
        }
    }

    private fun setExchangePriceAmount(trans: Transaction) {
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
    }

    private fun setAliasesAddresses(trans: Transaction) {
        when {
            trans.recipient.isAlias() -> {
                val aliasName = trans.recipient.parseAlias()
                loadAliasAddress(aliasName) { address ->
                    trans.recipientAddress = address
                    trans.transactionTypeId = transactionUtil.getTransactionType(trans)
                    trans.save()
                }

            }
            trans.lease?.recipient?.isAlias() == true -> {
                val aliasName = trans.lease?.recipient?.parseAlias()
                loadAliasAddress(aliasName) { address ->
                    trans.lease?.recipientAddress = address
                    trans.transactionTypeId = transactionUtil.getTransactionType(trans)
                    trans.save()
                }
            }
            else -> {
                trans.recipientAddress = trans.recipient
                trans.lease?.recipientAddress = trans.lease?.recipient
            }
        }

        trans.transfers.forEach { transfer ->
            when {
                transfer.recipient.isAlias() -> {
                    val aliasName = transfer.recipient.parseAlias()
                    loadAliasAddress(aliasName) { address ->
                        transfer.recipientAddress = address
                        transfer.save()
                    }
                }
                else -> {
                    transfer.recipientAddress = transfer.recipient
                }
            }
        }
    }

    private fun loadAliasAddress(alias: String?, listener: (String?) -> Unit) {
        if (App.getAccessManager().getWallet() != null) {
            alias.notNull {
                subscriptions.add(apiDataManager.loadAlias(it)
                        .compose(RxUtil.applyObservableDefaultSchedulers())
                        .subscribe {
                            listener.invoke(it.address)
                        })
            }
        }
    }

    private fun mergeAndSaveAllAssets(arrayList: ArrayList<AssetInfo>,
                                      callback: (ArrayList<AssetInfo>) -> Unit) {
        runAsync {
            queryAllAsync<SpamAsset> { spams ->
                val spamMap = spams.associateBy { it.assetId }
                val allAssetMap = allAssets.associateBy { it.id }
                arrayList.iterator().forEach { newAsset ->
                    if (allAssetMap[newAsset.id] == null) {
                        newAsset.isSpam = spamMap[newAsset.id] != null
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