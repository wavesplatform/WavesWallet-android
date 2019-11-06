/*
 * Created by Eduard Zaydel on 6/11/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.injection.qualifier.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsManager @Inject constructor(@ApplicationContext var context: Context) {

    fun isNotificationEnabled(channelId: String? = context.getString(R.string.default_notification_channel_id)): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (channelId.isNullOrEmpty().not()) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channel = manager.getNotificationChannel(channelId)
                return channel.importance != NotificationManager.IMPORTANCE_NONE && NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
            return false
        } else {
            return NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun openNotificationSettings() {
        val channelId = context.getString(R.string.default_notification_channel_id)

        if (!isNotificationEnabled(channelId)) {
            openOldNotificationSettings()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isChannelBlocked(channelId)) {
            openChannelSettings(channelId)
            return
        }
    }

    private fun openOldNotificationSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            context.startActivity(intent)
        } else {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:${context.packageName}")
            context.startActivity(intent)
        }
    }

    @RequiresApi(26)
    private fun isChannelBlocked(channelId: String): Boolean {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = manager!!.getNotificationChannel(channelId)

        return channel != null && channel.importance == NotificationManager.IMPORTANCE_NONE
    }

    @RequiresApi(26)
    private fun openChannelSettings(channelId: String) {
        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        intent.putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
        context.startActivity(intent)
    }

}