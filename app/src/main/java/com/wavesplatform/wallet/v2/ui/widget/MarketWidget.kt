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
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.RemoteViews
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.widget.model.MarketWidgetCurrency
import com.wavesplatform.wallet.v2.ui.widget.model.MarketWidgetProgressState
import com.wavesplatform.wallet.v2.ui.widget.model.MarketWidgetStyle


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [MarketWidgetConfigureActivity]
 */
class MarketWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            MarketWidgetStyle.removeTheme(context, appWidgetId)
            MarketWidgetCurrency.removeCurrency(context, appWidgetId)
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
        val widgetId = intent.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            when (intent.action) {
                ACTION_CURRENCY_CHANGE -> {
                    MarketWidgetCurrency.switchCurrency(context, widgetId)

                    updateWidget(context, AppWidgetManager.getInstance(context), widgetId)
                }
                ACTION_UPDATE -> {
                    updateWidgetProgress(context, widgetId,
                            arrayListOf(MarketWidgetProgressState.PROGRESS, MarketWidgetProgressState.IDLE).shuffled().first())
                }
            }
        }
    }

    companion object {
        const val PREFS_NAME = "com.wavesplatform.wallet.v2.ui.widget.MarketWidget"
        const val ACTION_CURRENCY_CHANGE = "currency_change_action"
        const val ACTION_UPDATE = "update_action"

        internal fun updateWidget(context: Context, appWidgetManager: AppWidgetManager,
                                  appWidgetId: Int, progressState: MarketWidgetProgressState = MarketWidgetProgressState.NONE) {
            // Construct the RemoteViews object
            val theme = MarketWidgetStyle.getTheme(context, appWidgetId)
            val views = RemoteViews(context.packageName, theme.themeLayout)

            configureClicks(context, appWidgetId, views)
            configureProgressState(context, progressState, views)
            configureMarketList(context, appWidgetId, views)
            views.setTextViewText(R.id.text_currency, highLightCurrency(context, theme, appWidgetId))

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list_markets)
        }

        internal fun updateWidgetProgress(context: Context, appWidgetId: Int,
                                          progressState: MarketWidgetProgressState = MarketWidgetProgressState.NONE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val theme = MarketWidgetStyle.getTheme(context, appWidgetId)
            val views = RemoteViews(context.packageName, theme.themeLayout)

            configureProgressState(context, progressState, views)

            appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
        }

        private fun configureMarketList(context: Context, appWidgetId: Int, views: RemoteViews) {
            val adapter = Intent(context, MarketWidgetAdapterService::class.java)
            adapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            adapter.data = Uri.parse(adapter.toUri(Intent.URI_INTENT_SCHEME))
            views.setRemoteAdapter(R.id.list_markets, adapter)
        }

        private fun configureProgressState(context: Context, progressState: MarketWidgetProgressState, views: RemoteViews) {
            when (progressState) {
                MarketWidgetProgressState.IDLE -> {
                    views.setViewVisibility(R.id.image_update, View.VISIBLE)
                    views.setViewVisibility(R.id.progress_updating, View.GONE)
                    views.setTextViewText(R.id.text_update, context.getText(R.string.market_widget_update))
                }
                MarketWidgetProgressState.PROGRESS -> {
                    views.setViewVisibility(R.id.image_update, View.GONE)
                    views.setViewVisibility(R.id.progress_updating, View.VISIBLE)
                    views.setTextViewText(R.id.text_update, context.getText(R.string.market_widget_updating))
                }
                else -> {
                    // nothing
                }
            }
        }

        private fun highLightCurrency(context: Context, theme: MarketWidgetStyle, appWidgetId: Int): SpannableString {
            val currency = MarketWidgetCurrency.getCurrency(context, appWidgetId).name
            val highLightString = SpannableString(context.getString(R.string.market_widget_currency_text))

            val activeSpan = ForegroundColorSpan(ContextCompat.getColor(context, theme.colors.currencyActiveColor))
            val inActiveSpan = ForegroundColorSpan(ContextCompat.getColor(context, theme.colors.currencyInactiveColor))

            highLightString.setSpan(inActiveSpan, 0, highLightString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            highLightString.setSpan(activeSpan, highLightString.indexOf(currency),
                    highLightString.indexOf(currency) + currency.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            return highLightString
        }

        private fun configureClicks(context: Context, appWidgetId: Int, views: RemoteViews) {
            // set on click intent to 'setting' button
            val configIntent = Intent(context, MarketWidgetConfigureActivity::class.java)
            configIntent.action = AppWidgetManager.ACTION_APPWIDGET_CONFIGURE
            configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            var pIntent = PendingIntent.getActivity(context, appWidgetId,
                    configIntent, 0)
            views.setOnClickPendingIntent(R.id.image_configuration, pIntent)

            // set on click intent to 'currency switch' button
            val currencySwitcherIntent = Intent(context, MarketWidget::class.java)
            currencySwitcherIntent.action = ACTION_CURRENCY_CHANGE
            currencySwitcherIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            pIntent = PendingIntent.getBroadcast(context, appWidgetId, currencySwitcherIntent, 0)
            views.setOnClickPendingIntent(R.id.text_currency, pIntent)

            // set on click intent to 'update' button
            val updateIntent = Intent(context, MarketWidget::class.java)
            updateIntent.action = ACTION_UPDATE
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            pIntent = PendingIntent.getBroadcast(context, appWidgetId, updateIntent, 0)
            views.setOnClickPendingIntent(R.id.linear_update, pIntent)
        }
    }
}

