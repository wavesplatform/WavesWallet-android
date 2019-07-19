/*
 * Created by Eduard Zaydel on 18/7/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.widget.RemoteViews
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.widget.model.MarketWidgetTheme
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import pers.victor.ext.Ext.ctx
import com.wavesplatform.wallet.v2.ui.widget.model.MarketWidgetCurrency


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [MarketWidgetConfigureActivity]
 */
class MarketWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            MarketWidgetTheme.removeTheme(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_CURRENCY_CHANGE -> {
                val widgetId = intent.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                        ?: AppWidgetManager.INVALID_APPWIDGET_ID

                if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    MarketWidgetCurrency.switchCurrency(context, widgetId)

                    updateAppWidget(context, AppWidgetManager.getInstance(context), widgetId)
                }
            }
        }
    }

    companion object {
        const val PREFS_NAME = "com.wavesplatform.wallet.v2.ui.widget.MarketWidget"
        const val ACTION_CURRENCY_CHANGE = "currency_change_action"

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                     appWidgetId: Int) {
            // Construct the RemoteViews object
            val theme = MarketWidgetTheme.getTheme(context, appWidgetId)
            val views = RemoteViews(context.packageName, theme.themeLayout)

            configureClicks(context, appWidgetId, views)
            views.setTextViewText(R.id.text_currency, highLightCurrency(context, theme, appWidgetId))

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun highLightCurrency(context: Context, theme: MarketWidgetTheme, appWidgetId: Int): SpannableString {
            val currency = MarketWidgetCurrency.getCurrency(context, appWidgetId).name
            val spannableString = SpannableString(context.getString(R.string.market_widget_currency_text))

            val activeSpan = ForegroundColorSpan(ContextCompat.getColor(context, theme.currencyActiveColor))
            val inActiveSpan = ForegroundColorSpan(ContextCompat.getColor(context, theme.currencyInactiveColor))

            spannableString.setSpan(inActiveSpan, 0, spannableString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(activeSpan, spannableString.indexOf(currency),
                    spannableString.indexOf(currency) + currency.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            return spannableString
        }

        private fun configureClicks(context: Context, appWidgetId: Int, views: RemoteViews) {
            // set on click on 'setting' button
            val configIntent = Intent(context, MarketWidgetConfigureActivity::class.java)
            configIntent.action = AppWidgetManager.ACTION_APPWIDGET_CONFIGURE
            configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            var pIntent = PendingIntent.getActivity(context, appWidgetId,
                    configIntent, 0)
            views.setOnClickPendingIntent(R.id.image_configuration, pIntent)

            // set on click on 'currency switch' button
            val countIntent = Intent(ctx, MarketWidget::class.java)
            countIntent.action = ACTION_CURRENCY_CHANGE
            countIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            pIntent = PendingIntent.getBroadcast(context, appWidgetId, countIntent, 0)
            views.setOnClickPendingIntent(R.id.text_currency, pIntent)
        }
    }
}

