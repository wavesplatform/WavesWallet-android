/*
 * Created by Eduard Zaydel on 24/7/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget.model

import android.content.Context
import android.support.annotation.StringRes
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.widget.MarketWidget

enum class MarketWidgetUpdateInterval(@StringRes title: Int, interval: Int) {
    MIN_1(R.string.market_widget_config_interval_1_min, 1),
    MIN_5(R.string.market_widget_config_interval_5_min, 5),
    MIN_10(R.string.market_widget_config_interval_10_min, 10),
    MANUALLY(R.string.market_widget_config_interval_manually, 0);

    fun getIntervalOnMillis(): Long {
        return 1000L * 60L * interval
    }

    companion object {
        private const val PREF_INTERVAL_KEY = "appwidget_interval_"

        fun getInterval(context: Context, appWidgetId: Int): MarketWidgetUpdateInterval {
            val prefs = context.getSharedPreferences(MarketWidget.PREFS_NAME, 0)
            val interval = prefs.getString(PREF_INTERVAL_KEY + appWidgetId, null)
            return values().firstOrNull { it.name == interval } ?: MIN_1 // TODO: Change to 5 min after test auto update
        }

        fun setInterval(context: Context, appWidgetId: Int, interval: MarketWidgetUpdateInterval) {
            val prefs = context.getSharedPreferences(MarketWidget.PREFS_NAME, 0).edit()
            prefs.putString(PREF_INTERVAL_KEY + appWidgetId, interval.name)
            prefs.apply()
        }

        fun removeInterval(context: Context, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(MarketWidget.PREFS_NAME, 0).edit()
            prefs.remove(PREF_INTERVAL_KEY + appWidgetId)
            prefs.apply()
        }
    }
}