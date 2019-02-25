package com.wavesplatform.wallet.v2.data.service

import android.app.Service
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.ProcessLifecycleOwner
import android.content.Intent
import android.os.IBinder
import com.vicpin.krealmextensions.queryFirst
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.manager.NodeDataManager
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.data.database.TransactionSaver
import com.wavesplatform.wallet.v2.util.RxEventBus
import com.wavesplatform.wallet.v2.util.RxUtil
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class UpdateApiDataService : Service() {

    @Inject
    lateinit var nodeDataManager: NodeDataManager
    @Inject
    lateinit var rxEventBus: RxEventBus
    var subscriptions: CompositeDisposable = CompositeDisposable()
    @Inject
    lateinit var transactionSaver: TransactionSaver
    private var transactionLimit = TransactionSaver.DEFAULT_LIMIT

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (App.getAccessManager().getWallet() == null ||
                ProcessLifecycleOwner.get().lifecycle.currentState != Lifecycle.State.RESUMED) {
            stopSelf()
            return Service.START_NOT_STICKY
        }

        val transaction = queryFirst<Transaction>()
        if (transaction == null) {
            transactionLimit = TransactionSaver.MAX_LIMIT
        }
        subscriptions.add(nodeDataManager.loadTransactions(transactionLimit)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    if (it.isNotEmpty()) {
                        transactionSaver.saveTransactions(
                                it.sortedByDescending { it.timestamp },
                                transactionLimit,
                                object : TransactionSaver.OnTransactionLimitChangeListener {
                                    override fun onChange(limit: Int) {
                                        transactionLimit = limit
                                    }
                                })
                    }
                }, {
                    rxEventBus.post(Events.StopUpdateHistoryScreen())
                    it.printStackTrace()
                }))
        subscriptions.add(nodeDataManager.currentBlocksHeight()
                .subscribe {
                })
        return Service.START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        subscriptions.clear()
        super.onDestroy()
    }
}