/*
 * Created by Eduard Zaydel on 18/7/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.RemoteViews
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.analytics.AnalyticEvents
import com.wavesplatform.wallet.v2.data.analytics.analytics
import com.wavesplatform.wallet.v2.data.manager.MarketWidgetDataManager
import com.wavesplatform.wallet.v2.data.model.local.widget.MarketWidgetProgressState
import com.wavesplatform.wallet.v2.data.model.local.widget.MarketWidgetSettings
import com.wavesplatform.wallet.v2.data.model.local.widget.MarketWidgetStyle
import com.wavesplatform.wallet.v2.ui.widget.configuration.MarketWidgetConfigureActivity
import com.wavesplatform.wallet.v2.util.ACTION_AUTO_UPDATE_WIDGET
import com.wavesplatform.wallet.v2.util.EnvironmentManager
import com.wavesplatform.wallet.v2.util.getLocalizedString
import com.wavesplatform.wallet.v2.util.startAlarmUpdate
import dagger.android.AndroidInjection
import pers.victor.ext.isNetworkConnected
import java.util.*
import javax.inject.Inject


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [MarketWidgetConfigureActivity]
 */
class MarketPulseAppWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var marketWidgetDataManager: MarketWidgetDataManager
    private var onUpdateCompleteListener: EnvironmentManager.Companion.OnUpdateCompleteListener? = null

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
            loadPrice(context, appWidgetId)
            context.startAlarmUpdate<MarketPulseAppWidgetProvider>(appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            MarketWidgetSettings.clearSettings(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        analytics.trackEvent(AnalyticEvents.MarketPulseAddedEvent)
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        marketWidgetDataManager.clearSubscription()
        analytics.trackEvent(AnalyticEvents.MarketPulseRemovedEvent)
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)
        super.onReceive(context, intent)
        val widgetId = intent.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            when (intent.action) {
                ACTION_CURRENCY_CHANGE -> {
                    MarketWidgetSettings.currencySettings().switchCurrency(context, widgetId)
                    updateWidget(context, AppWidgetManager.getInstance(context), widgetId)
                    analytics.trackEvent(AnalyticEvents.MarketPulseActiveEvent)
                }
                ACTION_UPDATE -> {
                    loadPrice(context, widgetId)
                    analytics.trackEvent(AnalyticEvents.MarketPulseActiveEvent)
                }
                ACTION_AUTO_UPDATE_WIDGET -> {
                    loadPrice(context, widgetId)
                }
            }
        }
    }

    private fun loadPrice(context: Context, widgetId: Int) {
        if (isNetworkConnected()) {
            updateWidgetProgress(context, widgetId, MarketWidgetProgressState.PROGRESS)
            if (EnvironmentManager.isUpdateCompleted()) {
                marketWidgetDataManager.loadMarketsPrices(context, widgetId, successListener = {
                    updateWidget(context, AppWidgetManager.getInstance(context), widgetId, MarketWidgetProgressState.IDLE)
                }, errorListener = {
                    updateWidgetProgress(context, widgetId, MarketWidgetProgressState.IDLE)
                })
            } else {
                EnvironmentManager.update()
                onUpdateCompleteListener = object : EnvironmentManager.Companion.OnUpdateCompleteListener {

                    override fun onComplete() {
                        EnvironmentManager.removeOnUpdateCompleteListener(onUpdateCompleteListener!!)
                        loadPrice(context, widgetId)
                    }

                    override fun onError() {
                        EnvironmentManager.removeOnUpdateCompleteListener(onUpdateCompleteListener!!)
                        updateWidgetProgress(context, widgetId, MarketWidgetProgressState.IDLE)
                    }
                }
                EnvironmentManager.addOnUpdateCompleteListener(onUpdateCompleteListener!!)
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
            val theme = MarketWidgetSettings.themeSettings().getTheme(context, appWidgetId)
            val views = RemoteViews(context.packageName, theme.themeLayout)

            configureClicks(context, appWidgetId, views)
            configureProgressState(context, appWidgetId, progressState, views)
            configureMarketList(context, appWidgetId, views)
            views.setTextViewText(R.id.text_currency, highLightCurrency(context, theme, appWidgetId))

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list_markets)
        }

        internal fun updateWidgetProgress(context: Context, appWidgetId: Int,
                                          progressState: MarketWidgetProgressState = MarketWidgetProgressState.NONE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val theme = MarketWidgetSettings.themeSettings().getTheme(context, appWidgetId)
            val views = RemoteViews(context.packageName, theme.themeLayout)

            configureProgressState(context, appWidgetId, progressState, views)

            appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
        }

        fun updateAllWidgetsByBroadcast(context: Context) {
            val intent = Intent(context, MarketPulseAppWidgetProvider::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

            val widgetManager = AppWidgetManager.getInstance(context)
            val ids = widgetManager.getAppWidgetIds(ComponentName(context, MarketPulseAppWidgetProvider::class.java))

            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }

        fun updateWidgetByBroadcast(context: Context, widgetId: Int) {
            val intent = Intent(context, MarketPulseAppWidgetProvider::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

            val ids = intArrayOf(widgetId)

            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }

        private fun configureMarketList(context: Context, appWidgetId: Int, views: RemoteViews) {
            val adapter = Intent(context, MarketWidgetAdapterService::class.java)
            adapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            adapter.data = Uri.parse(adapter.toUri(Intent.URI_INTENT_SCHEME))
            views.setRemoteAdapter(R.id.list_markets, adapter)
        }

        private fun configureProgressState(context: Context, appWidgetId: Int,
                                           progressState: MarketWidgetProgressState, views: RemoteViews,
                                           locale: Locale = Locale.getDefault()) {
            when (progressState) {
                MarketWidgetProgressState.IDLE -> {
                    views.setViewVisibility(R.id.image_update, View.VISIBLE)
                    views.setViewVisibility(R.id.progress_updating, View.GONE)
                    views.setTextViewText(R.id.text_update, context.getLocalizedString(R.string.market_widget_update, locale))
                    configureClicks(context, appWidgetId, views)
                }
                MarketWidgetProgressState.PROGRESS -> {
                    views.setViewVisibility(R.id.image_update, View.GONE)
                    views.setViewVisibility(R.id.progress_updating, View.VISIBLE)
                    views.setTextViewText(R.id.text_update, context.getLocalizedString(R.string.market_widget_updating, locale))
                    views.setOnClickPendingIntent(R.id.linear_update, null)
                }
                else -> {
                    // nothing
                }
            }
        }

        private fun highLightCurrency(context: Context, theme: MarketWidgetStyle, appWidgetId: Int): SpannableString {
            val currency = MarketWidgetSettings.currencySettings().getCurrency(context, appWidgetId).name
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
            configIntent.putExtra(MarketWidgetConfigureActivity.EXTRA_APPWIDGET_CHANGE, true)
            var pIntent = PendingIntent.getActivity(context, appWidgetId,
                    configIntent, 0)
            views.setOnClickPendingIntent(R.id.image_configuration, pIntent)

            // set on click intent to 'currency switch' button
            val currencySwitcherIntent = Intent(context, MarketPulseAppWidgetProvider::class.java)
            currencySwitcherIntent.action = ACTION_CURRENCY_CHANGE
            currencySwitcherIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            pIntent = PendingIntent.getBroadcast(context, appWidgetId, currencySwitcherIntent, 0)
            views.setOnClickPendingIntent(R.id.text_currency, pIntent)

            // set on click intent to 'update' button
            val updateIntent = Intent(context, MarketPulseAppWidgetProvider::class.java)
            updateIntent.action = ACTION_UPDATE
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            pIntent = PendingIntent.getBroadcast(context, appWidgetId, updateIntent, 0)
            views.setOnClickPendingIntent(R.id.linear_update, pIntent)
        }
    }
}

