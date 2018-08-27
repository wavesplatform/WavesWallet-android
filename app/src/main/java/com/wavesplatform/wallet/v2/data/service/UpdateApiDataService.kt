package com.wavesplatform.wallet.v2.data.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.wavesplatform.wallet.v2.data.manager.NodeDataManager
import dagger.android.AndroidInjection
import javax.inject.Inject
import com.vicpin.krealmextensions.*
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.util.RxUtil
import com.wavesplatform.wallet.v2.util.TransactionUtil
import com.wavesplatform.wallet.v2.util.notNull
import io.reactivex.disposables.CompositeDisposable


class UpdateApiDataService : Service() {

    @Inject
    lateinit var nodeDataManager: NodeDataManager
    @Inject
    lateinit var transactionUtil: TransactionUtil
    var subscirtions: CompositeDisposable = CompositeDisposable()

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        subscirtions.add(nodeDataManager.loadTransactions(100)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    it.first.notNull {
                        if (it.isNotEmpty()) {
                            val transaction = queryFirst<Transaction>({ equalTo("id", it[0].id) })
                            if (transaction == null) {
                                it.forEach { trans ->
                                    if (trans.assetId.isNullOrEmpty()) {
                                        trans.asset = Constants.defaultAssets[0]
                                    } else {
                                        trans.asset = queryFirst<AssetBalance>({ equalTo("assetId", trans.assetId) })
                                    }
                                    trans.order1.notNull {
                                        val amountAsset = queryFirst<AssetBalance>({ equalTo("assetId", it.assetPair?.amountAsset) })
                                        val priceAsset = queryFirst<AssetBalance>({ equalTo("assetId", it.assetPair?.priceAsset) })
                                        it.assetPair?.amountAssetObject = amountAsset
                                        it.assetPair?.priceAssetObject = priceAsset
                                        trans.order2.notNull {
                                            it.assetPair?.amountAssetObject = amountAsset
                                            it.assetPair?.priceAssetObject = priceAsset
                                        }
                                    }
                                    trans.transactionTypeId = transactionUtil.getTransactionType(trans)
                                }
                                it.saveAll()
                            }
                        }
                    }
                }))
        subscirtions.add(nodeDataManager.currentBlocksHeight()
                .subscribe({

                }))
        return Service.START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        subscirtions.clear()
        super.onDestroy()
    }

}