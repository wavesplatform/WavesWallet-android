/*
 * Created by Aleksandr Ershov on 23/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.service

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.ProcessLifecycleOwner
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.data.database.TransactionSaver
import com.wavesplatform.wallet.v2.data.manager.NodeDataManager
import com.wavesplatform.wallet.v2.data.model.remote.response.Height
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.util.RxEventBus
import com.wavesplatform.wallet.v2.util.RxUtil
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import timber.log.Timber
import java.util.concurrent.TimeUnit


class HistoryRepeatUpdater {

    companion object {

        private var nodeDataManager: NodeDataManager? = null
        private var rxEventBus: RxEventBus? = null
        private var running = false
        private var transactionSaver: TransactionSaver? = null
        private var transactionLimit = TransactionSaver.DEFAULT_LIMIT
        private var subscription: Disposable? = null

        fun start(nodeDataManager: NodeDataManager, rxEventBus: RxEventBus) {
            if (running || App.getAccessManager().getWallet() == null) {
                return
            }

            this.running = true
            this.nodeDataManager = nodeDataManager
            this.rxEventBus = rxEventBus
            this.transactionSaver = TransactionSaver(Companion.nodeDataManager!!, Companion.rxEventBus!!)
            this.transactionLimit = TransactionSaver.DEFAULT_LIMIT

            subscription = Observable.interval(0, 15, TimeUnit.SECONDS)
                    .flatMap {
                        Observable.zip(
                                nodeDataManager.nodeService.transactionList(
                                        App.getAccessManager().getWallet()?.address, transactionLimit)?.map { it[0] },
                                Companion.nodeDataManager?.currentBlocksHeight(),
                                BiFunction { transactions: List<Transaction>, height: Height ->
                                    Timber.d("HistoryRepeatUpdater: Success load!")
                                    if (running) {
                                        if (transactions.isNotEmpty()) {
                                            saveToDb(transactions)
                                        }
                                        nodeDataManager.preferencesHelper.currentBlocksHeight = height.height
                                        cancelIfNeed()
                                    }
                                })
                    }
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .doOnError {
                        Timber.e("HistoryRepeatUpdater: doOnError() - $it")
                        cancelIfNeed()
                    }
                    .retryWhen {
                        it.delay(15, TimeUnit.SECONDS)
                    }
                    .subscribe()

            Timber.d("HistoryRepeatUpdater: start()")
        }

        fun cancel() {
            if (!running) {
                return
            }
            running = false
            RxUtil.unsubscribe(subscription)
            subscription = null
            Timber.d("HistoryRepeatUpdater: cancel()")
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

        private fun cancelIfNeed() {
            if (!(App.getAccessManager().getWallet() != null &&
                    ProcessLifecycleOwner.get().lifecycle.currentState == Lifecycle.State.RESUMED)) {
                cancel()
            }
        }
    }
}