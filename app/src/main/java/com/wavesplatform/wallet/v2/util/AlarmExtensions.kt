/*
 * Created by Eduard Zaydel on 29/7/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import com.wavesplatform.wallet.v2.data.model.local.widget.MarketWidgetSettings


const val ACTION_AUTO_UPDATE_WIDGET = "ACTION_AUTO_UPDATE_WIDGET"

inline fun <reified T> Context.startAlarmUpdate(id: Int, action: String = ACTION_AUTO_UPDATE_WIDGET) {
    val intent = Intent(this, T::class.java)
    intent.action = action
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
    val pendingIntent = PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    (getSystemService(Context.ALARM_SERVICE) as AlarmManager).setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(),
            MarketWidgetSettings.intervalSettings().getInterval(this, id).getIntervalOnMillis(), pendingIntent)
}

inline fun <reified T> Context.cancelAlarmUpdate(id: Int, action: String = ACTION_AUTO_UPDATE_WIDGET) {
    val intent = Intent(this, T::class.java)
    intent.action = action
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
    val pendingIntent = PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    (getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pendingIntent)
    pendingIntent.cancel()
}