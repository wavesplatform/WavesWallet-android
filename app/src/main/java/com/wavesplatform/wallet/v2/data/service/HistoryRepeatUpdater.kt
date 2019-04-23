/*
 * Created by Aleksandr Ershov on 23/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.service

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.ProcessLifecycleOwner
import android.content.Context
import android.os.Handler
import androidx.work.OneTimeWorkRequest
import androidx.work.RxWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.vicpin.krealmextensions.queryFirst
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.data.database.TransactionSaver
import com.wavesplatform.wallet.v2.data.manager.NodeDataManager
import com.wavesplatform.wallet.v2.data.model.remote.response.Height
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.util.RxEventBus
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import timber.log.Timber


class HistoryRepeatUpdater(context: Context, workerParams: WorkerParameters) : RxWorker(context, workerParams) {

    override fun createWork(): Single<Result> {
        if (App.getAccessManager().getWallet() == null ||
                ProcessLifecycleOwner.get().lifecycle.currentState != Lifecycle.State.RESUMED) {
            cancel()
            return Single.just(Result.failure())
        }

        val transaction = queryFirst<Transaction>()
        if (transaction == null) {
            transactionLimit = TransactionSaver.MAX_LIMIT
        }

        return loadTransactionsAndHeightWithResult()
    }

    private fun loadTransactionsAndHeightWithResult(): Single<Result> {
        Timber.d("HistoryRepeatUpdater: Start request-set to load Transactions & Height!")
        return Observable.zip(
                nodeDataManager!!.nodeService.transactionList(
                        App.getAccessManager().getWallet()?.address, transactionLimit).map { it[0] },
                nodeDataManager!!.currentBlocksHeight(),
                BiFunction { transactions: List<Transaction>, height: Height ->
                    Timber.d("HistoryRepeatUpdater: Success load Transactions & Height finished!")
                    if (running) {
                        saveToDb(transactions)
                        nodeDataManager?.preferencesHelper?.currentBlocksHeight = height.height
                        scheduleWork()
                    }
                })
                .doOnError {
                    Timber.d("HistoryRepeatUpdater: Error load Transactions & Height finished!")
                    if (running) {
                        scheduleWork()
                    }
                    Result.failure()
                }
                .toList()
                .map { Result.success() }
    }

    private fun saveToDb(transactions: List<Transaction>) {
        if (transactions.isNotEmpty()) {
            transactionSaver?.saveTransactions(
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

        private var workRequest: OneTimeWorkRequest? = null
        private var nodeDataManager: NodeDataManager? = null
        private var rxEventBus: RxEventBus? = null
        private var handler: Handler? = null
        private var repeatRunnable: Runnable? = null
        private var running = false
        private var transactionSaver: TransactionSaver? = null
        private var transactionLimit = TransactionSaver.DEFAULT_LIMIT

        fun start(handler: Handler, nodeDataManager: NodeDataManager, rxEventBus: RxEventBus) {
            if (running || App.getAccessManager().getWallet() == null) {
                return
            }

            this.handler = handler
            this.nodeDataManager = nodeDataManager
            this.rxEventBus = rxEventBus
            this.transactionSaver = TransactionSaver(Companion.nodeDataManager!!, Companion.rxEventBus!!)
            this.transactionLimit = TransactionSaver.DEFAULT_LIMIT

            scheduleWork(true)
            Timber.d("HistoryRepeatUpdater: Started service load Transactions & Height!")
            running = true
        }

        fun cancel() {
            if (running) {
                handler?.removeCallbacks(repeatRunnable)
                cancelWork()
                Timber.d("HistoryRepeatUpdater: Stopped service load Transactions & Height!")
                running = false
            }
        }

        private fun scheduleWork(startImmediately: Boolean = false) {
            repeatRunnable = Runnable {
                if (running) {
                    workRequest = OneTimeWorkRequest.Builder(HistoryRepeatUpdater::class.java).build()
                    WorkManager.getInstance().enqueue(workRequest!!)
                } else {
                    cancel()
                }
            }

            if (startImmediately) {
                handler?.post(repeatRunnable)
            } else {
                handler?.postDelayed(repeatRunnable, 15_000)
            }
        }

        private fun cancelWork() {
            if (workRequest != null) {
                WorkManager.getInstance().cancelWorkById(workRequest!!.id)
                workRequest = null
            }
        }
    }
}