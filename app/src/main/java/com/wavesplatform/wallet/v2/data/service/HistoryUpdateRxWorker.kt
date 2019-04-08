package com.wavesplatform.wallet.v2.data.service

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.ProcessLifecycleOwner
import android.content.Context
import androidx.work.PeriodicWorkRequest
import androidx.work.RxWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.vicpin.krealmextensions.queryFirst
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.database.TransactionSaver
import com.wavesplatform.wallet.v2.data.manager.NodeDataManager
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.util.RxEventBus
import io.reactivex.Single
import java.util.concurrent.TimeUnit


class HistoryUpdateRxWorker(context: Context, workerParams: WorkerParameters) : RxWorker(context, workerParams) {

    private var transactionSaver = TransactionSaver(nodeDataManager!!, rxEventBus!!)
    private var transactionLimit = TransactionSaver.DEFAULT_LIMIT

    override fun createWork(): Single<Result> {
        if (App.getAccessManager().getWallet() == null ||
                ProcessLifecycleOwner.get().lifecycle.currentState != Lifecycle.State.RESUMED) {
            return Single.just(Result.failure())
        }

        val transaction = queryFirst<Transaction>()
        if (transaction == null) {
            transactionLimit = TransactionSaver.MAX_LIMIT
        }

        return loadTransactionsWithResult()
    }

    private fun loadTransactionsWithResult(): Single<Result> {
        return nodeDataManager!!.nodeService.transactionList(
                App.getAccessManager().getWallet()?.address,
                transactionLimit)
                .map { r -> r[0] }
                .doOnNext { saveToDb(it) }
                .doOnError {
                    rxEventBus!!.post(Events.StopUpdateHistoryScreen())
                    it.printStackTrace()
                }
                .flatMap { nodeDataManager!!.currentBlocksHeight() }
                .toList()
                .map { Result.retry() }
    }

    private fun saveToDb(transactions: List<Transaction>) {
        if (transactions.isNotEmpty()) {
            transactionSaver.saveTransactions(
                    transactions.sortedByDescending { it.timestamp },
                    transactionLimit,
                    object : TransactionSaver.OnTransactionLimitChangeListener {
                        override fun onChange(limit: Int) {
                            transactionLimit = limit
                        }
                    })
        }
    }


    companion object {

        private var workRequest: PeriodicWorkRequest? = null
        private var nodeDataManager: NodeDataManager? = null
        private var rxEventBus: RxEventBus? = null

        fun start(nodeDataManager: NodeDataManager, rxEventBus: RxEventBus) {
            this.nodeDataManager = nodeDataManager
            this.rxEventBus = rxEventBus
            workRequest = PeriodicWorkRequest.Builder(HistoryUpdateRxWorker::class.java,
                    15, TimeUnit.SECONDS).build()
            WorkManager.getInstance().enqueue(workRequest!!)
        }

        fun cancel() {
            if (workRequest != null) {
                WorkManager.getInstance().cancelWorkById(workRequest!!.id)
                workRequest = null
            }
        }
    }
}