/*
 * Created by Eduard Zaydel on 19/7/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget.model

import android.content.Context
import android.support.annotation.ColorRes
import android.support.annotation.LayoutRes
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.widget.MarketWidget

enum class MarketWidgetTheme(@LayoutRes var themeLayout: Int, // base layout for theme
                             @LayoutRes var marketItemLayout: Int, // layout for item of market
                             @ColorRes var currencyActiveColor: Int, // color for active text on switch of USD / EUR
                             @ColorRes var currencyInactiveColor: Int // color for inactive text on switch of USD / EUR
) {
    CLASSIC(R.layout.market_widget_classic, R.layout.item_market_widget_classic, R.color.black, R.color.basic500),
    DARK(R.layout.market_widget_dark, R.layout.item_market_widget_dark, R.color.white, R.color.disabled700);

    companion object {
        private const val PREF_THEME_KEY = "appwidget_theme_"

        fun getTheme(context: Context, appWidgetId: Int): MarketWidgetTheme {
            val prefs = context.getSharedPreferences(MarketWidget.PREFS_NAME, 0)
            val theme = prefs.getString(PREF_THEME_KEY + appWidgetId, null)
            return values().firstOrNull { it.name == theme } ?: CLASSIC
        }

        fun setTheme(context: Context, appWidgetId: Int, theme: MarketWidgetTheme) {
            val prefs = context.getSharedPreferences(MarketWidget.PREFS_NAME, 0).edit()
            prefs.putString(PREF_THEME_KEY + appWidgetId, theme.name)
            prefs.apply()
        }

        fun setTheme(context: Context, appWidgetId: Int, themeName: String) {
            val prefs = context.getSharedPreferences(MarketWidget.PREFS_NAME, 0).edit()
            prefs.putString(PREF_THEME_KEY + appWidgetId, themeName)
            prefs.apply()
        }

        fun removeTheme(context: Context, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(MarketWidget.PREFS_NAME, 0).edit()
            prefs.remove(PREF_THEME_KEY + appWidgetId)
            prefs.apply()
        }
    }
}