package com.wavesplatform.wallet.v2.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.ProcessLifecycleOwner
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.vicpin.krealmextensions.queryFirst
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.database.TransactionSaver
import com.wavesplatform.wallet.v2.data.manager.NodeDataManager
import com.wavesplatform.wallet.v2.data.model.db.TransactionDb
import com.wavesplatform.wallet.v2.util.RxEventBus
import com.wavesplatform.sdk.utils.RxUtil
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
        showServiceNotification()
    }

    private fun showServiceNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationId = (System.currentTimeMillis() % 10000).toInt()
            val channelId = "update_wavesplatform_history_data_service"
            val channel = NotificationChannel(channelId, "Wavesplatform Channel",
                    NotificationManager.IMPORTANCE_MIN)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .createNotificationChannel(channel)
            startForeground(notificationId, NotificationCompat.Builder(this, channelId).build())
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (App.getAccessManager().isAuthenticated() ||
                ProcessLifecycleOwner.get().lifecycle.currentState != Lifecycle.State.RESUMED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                stopForeground(true)
            } else {
                stopSelf()
            }
            return Service.START_NOT_STICKY
        }

        val transaction = queryFirst<TransactionDb>()
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