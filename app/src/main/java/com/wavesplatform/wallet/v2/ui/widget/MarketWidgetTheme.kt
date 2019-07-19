/*
 * Created by Eduard Zaydel on 19/7/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget

import android.content.Context
import android.support.annotation.LayoutRes
import com.wavesplatform.wallet.R

enum class MarketWidgetTheme(@LayoutRes var themeLayout: Int, // base layout for theme
                             @LayoutRes var marketItemLayout: Int // layout for item of market
) {
    CLASSIC(R.layout.market_widget_classic, 0),
    DARK(R.layout.market_widget_dark, 0);

    companion object {
        private const val PREFS_NAME = "com.wavesplatform.wallet.v2.ui.widget.MarketWidget"
        private const val PREF_PREFIX_KEY = "appwidget_"

        fun getTheme(context: Context, appWidgetId: Int): MarketWidgetTheme {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            val theme = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null)
            return values().firstOrNull { it.name == theme } ?: CLASSIC
        }

        fun setTheme(context: Context, appWidgetId: Int, theme: MarketWidgetTheme) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.putString(PREF_PREFIX_KEY + appWidgetId, theme.name)
            prefs.apply()
        }

        fun setTheme(context: Context, appWidgetId: Int, themeName: String) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.putString(PREF_PREFIX_KEY + appWidgetId, themeName)
            prefs.apply()
        }
    }
}