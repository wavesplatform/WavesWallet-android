/*
 * Created by Eduard Zaydel on 5/11/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.NotificationID
import com.wavesplatform.wallet.v2.ui.splash.SplashActivity

class WavesMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        sendNotification(remoteMessage)
    }

    override fun onNewToken(token: String) {
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to server.
    }

    private fun sendNotification(remoteMessage: RemoteMessage?) {

        val newNotification: Notification?
        val pendingIntent: PendingIntent?

        val intent = Intent(this, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(getString(R.string.default_notification_channel_id), getString(R.string.app_name), importance)
            notificationManager.createNotificationChannel(channel)
            newNotification = NotificationCompat.Builder(applicationContext, getString(R.string.default_notification_channel_id))
                    .setSmallIcon(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) R.drawable.ic_notification_logo else R.mipmap.ic_launcher) // TODO: Change icon
                    .setContentTitle(if (remoteMessage?.notification?.title.isNullOrEmpty()) getString(R.string.app_name) else remoteMessage?.notification?.title)
                    .setContentText(remoteMessage?.notification?.body)
                    .setChannelId(getString(R.string.default_notification_channel_id))
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build()
        } else {
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            newNotification = NotificationCompat.Builder(applicationContext)
                    .setSmallIcon(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) R.drawable.ic_notification_logo else R.mipmap.ic_launcher) // TODO: Change icon
                    .setContentTitle(if (remoteMessage?.notification?.title.isNullOrEmpty()) getString(R.string.app_name) else remoteMessage?.notification?.title)
                    .setContentText(remoteMessage?.notification?.body)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)
                    .build()
        }

        notificationManager.notify(NotificationID.id, newNotification)
    }
}