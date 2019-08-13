/*
 * Created by Eduard Zaydel on 8/8/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager.widget

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wavesplatform.wallet.v2.data.model.local.widget.MarketWidgetActiveMarket
import com.wavesplatform.wallet.v2.ui.widget.MarketPulseAppWidgetProvider

object MarketWidgetActiveMarketStore : MarketWidgetActiveStore<MarketWidgetActiveMarket.UI> {

    private val PREF_ACTIVE_MARKET_KEY = "appwidget_active_markets_"

    override fun queryAll(context: Context, widgetId: Int): MutableList<MarketWidgetActiveMarket.UI> {
        val prefs = context.getSharedPreferences(MarketPulseAppWidgetProvider.PREFS_NAME, 0)
        val listType = object : TypeToken<MutableList<MarketWidgetActiveMarket.UI>>() {}.type
        return Gson().fromJson<MutableList<MarketWidgetActiveMarket.UI>>(
                prefs.getString(PREF_ACTIVE_MARKET_KEY + widgetId, ""), listType)
                ?: mutableListOf()
    }

    override fun save(context: Context, widgetId: Int, data: MarketWidgetActiveMarket.UI) {
        // not used
    }

    override fun saveAll(context: Context, widgetId: Int, dataList: MutableList<MarketWidgetActiveMarket.UI>) {
        val prefs = context.getSharedPreferences(MarketPulseAppWidgetProvider.PREFS_NAME, 0)
        prefs.edit()
                .putString(PREF_ACTIVE_MARKET_KEY + widgetId, Gson().toJson(dataList))
                .apply()
    }

    override fun remove(context: Context, widgetId: Int, data: MarketWidgetActiveMarket.UI) {
        // not used
    }

    override fun clear(context: Context, widgetId: Int) {
        val prefs = context.getSharedPreferences(MarketPulseAppWidgetProvider.PREFS_NAME, 0).edit()
        prefs.remove(PREF_ACTIVE_MARKET_KEY + widgetId)
        prefs.apply()
    }
}