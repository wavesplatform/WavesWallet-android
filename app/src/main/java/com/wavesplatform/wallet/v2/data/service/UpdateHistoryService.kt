package com.wavesplatform.wallet.v2.data.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.wavesplatform.wallet.v2.data.manager.NodeDataManager
import com.wavesplatform.wallet.v2.data.remote.AppService
import dagger.android.AndroidInjection
import javax.inject.Inject
import com.github.mikephil.charting.charts.Chart.LOG_TAG
import com.google.common.base.Predicates.equalTo
import com.vicpin.krealmextensions.*
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.util.RxUtil
import com.wavesplatform.wallet.v2.util.TransactionUtil
import com.wavesplatform.wallet.v2.util.notNull
import com.wavesplatform.wallet.v2.util.transactionType
import io.reactivex.disposables.CompositeDisposable


class UpdateHistoryService : Service() {

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
//                            if (transaction == null) {
                                it.forEach {
                                    if (it.assetId.isNullOrEmpty()){
                                        it.asset = Constants.defaultAssets[0]
                                    }else{
                                        it.asset = queryFirst<AssetBalance>({ equalTo("assetId", it.assetId) })
                                    }
                                    it.transactionTypeId = transactionUtil.getTransactionType(it)
                                    Log.d("123","123")
                                }
                                it.saveAll()
//                            }
                        }
                    }
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