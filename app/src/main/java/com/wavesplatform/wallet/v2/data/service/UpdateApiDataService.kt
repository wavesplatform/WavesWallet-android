package com.wavesplatform.wallet.v2.data.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.vicpin.krealmextensions.queryFirst
import com.vicpin.krealmextensions.save
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.helpers.PublicKeyAccountHelper
import com.wavesplatform.wallet.v2.data.manager.ApiDataManager
import com.wavesplatform.wallet.v2.data.manager.NodeDataManager
import com.wavesplatform.wallet.v2.data.model.remote.response.Alias
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.Order
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.util.RxUtil
import com.wavesplatform.wallet.v2.util.TransactionUtil
import com.wavesplatform.wallet.v2.util.notNull
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import javax.inject.Inject


class UpdateApiDataService : Service() {

    @Inject
    lateinit var nodeDataManager: NodeDataManager
    @Inject
    lateinit var apiDataManager: ApiDataManager
    @Inject
    lateinit var transactionUtil: TransactionUtil
    @Inject
    lateinit var publicKeyAccountHelper: PublicKeyAccountHelper
    var subscriptions: CompositeDisposable = CompositeDisposable()

    var currentLimit = 100
    var prevLimit = 100
    var defaultLimit = 100
    var maxLimit = 10000

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val transaction = queryFirst<Transaction>()
        if (transaction == null) {
            nodeDataManager.currentLoadTransactionLimitPerRequest = maxLimit
        }
        subscriptions.add(nodeDataManager.loadTransactions()
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    it.first.notNull {
                        if (it.isNotEmpty()) {
                            val sortedList = it.sortedByDescending { it.timestamp }

                            val firstTransaction = queryFirst<Transaction>({ equalTo("id", sortedList[0].id) })
                            val lastTransaction = queryFirst<Transaction>({ equalTo("id", sortedList[sortedList.size - 1].id) })

                            if (lastTransaction == null) {
                                // all list is new, need load more

                                if (currentLimit >= maxLimit) currentLimit = 50

                                if (prevLimit == defaultLimit) {
                                    saveToDb(it)
                                } else {
                                    try {
                                        saveToDb(it.subList(prevLimit - 1, it.size - 1))
                                    } catch (e: Exception) {
                                        currentLimit = 50
                                    }
                                }

                                // save previous count of loaded transactions for future cut list
                                prevLimit = currentLimit

                                // multiply current limit
                                currentLimit *= 2
                                nodeDataManager.currentLoadTransactionLimitPerRequest = currentLimit

                            } else if (firstTransaction == null) {
                                // only few new transaction
                                saveToDb(it)
                            }
                        }
                    }
                }, {
                    it.printStackTrace()
                    Log.d("test", "test")
                }))
        subscriptions.add(nodeDataManager.currentBlocksHeight()
                .subscribe({

                }))
        return Service.START_NOT_STICKY
    }

    private fun saveToDb(it: List<Transaction>) {
        it.forEach { trans ->
            if (trans.assetId.isNullOrEmpty()) {
                trans.asset = Constants.defaultAssets[0]
            } else {
                trans.asset = queryFirst<AssetBalance>({ equalTo("assetId", trans.assetId) })
            }

            if (trans.recipient.contains("alias")) {
                val aliasName = trans.recipient.substringAfterLast(":")
                val alias = queryFirst<Alias>({ equalTo("alias", aliasName) })
                if (alias != null) {
                    trans.recipientAddress = publicKeyAccountHelper.publicKeyAccount?.address
                }else{
                    aliasName.notNull {
                        subscriptions.add(apiDataManager.loadAlias(it)
                                .compose(RxUtil.applyObservableDefaultSchedulers())
                                .subscribe({
                                    trans.recipientAddress = it.address
                                }))
                    }
                }
            } else {
                trans.recipientAddress = trans.recipient
            }

            if (trans.order1 != null) {
                val amountAsset =
                        if (trans.order1?.assetPair?.amountAsset.isNullOrEmpty()) {
                            Constants.defaultAssets[0]
                        } else {
                            queryFirst<AssetBalance>({ equalTo("assetId", trans.order1?.assetPair?.amountAsset) })
                        }
                val priceAsset =
                        if (trans.order1?.assetPair?.priceAsset.isNullOrEmpty()) {
                            Constants.defaultAssets[0]
                        } else {
                            queryFirst<AssetBalance>({ equalTo("assetId", trans.order1?.assetPair?.priceAsset) })
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
        it.saveAll()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        subscriptions.clear()
        super.onDestroy()
    }

}