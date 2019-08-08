/*
 * Created by Eduard Zaydel on 8/8/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.local.widget

import android.content.Context
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.widget.MarketWidget
import com.wavesplatform.wallet.v2.data.model.local.OptionsDialogModel

enum class MarketWidgetStyle(@StringRes var styleName: Int,
                             @LayoutRes var themeLayout: Int, // base layout for theme
                             @LayoutRes var marketItemLayout: Int, // layout for item of market,
                             var colors: MarketWidgetStyleColors
) : OptionsDialogModel {
    CLASSIC(R.string.market_widget_config_classic, R.layout.market_widget_classic,
            R.layout.item_market_widget_classic, MarketWidgetStyleColors.CLASSIC),
    DARK(R.string.market_widget_config_dark, R.layout.market_widget_dark,
            R.layout.item_market_widget_dark, MarketWidgetStyleColors.DARK);

    override fun itemTitle(): Int {
        return styleName
    }

    companion object {
        private const val PREF_THEME_KEY = "appwidget_theme_"

        fun getTheme(context: Context, appWidgetId: Int): MarketWidgetStyle {
            val prefs = context.getSharedPreferences(MarketWidget.PREFS_NAME, 0)
            val theme = prefs.getString(PREF_THEME_KEY + appWidgetId, null)
            return values().firstOrNull { it.name == theme } ?: CLASSIC
        }

        fun setTheme(context: Context, appWidgetId: Int, theme: MarketWidgetStyle) {
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