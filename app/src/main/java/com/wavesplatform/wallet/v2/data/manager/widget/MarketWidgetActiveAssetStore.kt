/*
 * Created by Eduard Zaydel on 8/8/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager.widget

import android.content.Context
import com.google.gson.Gson
import com.wavesplatform.wallet.v2.data.model.local.widget.MarketWidgetActiveAsset
import com.wavesplatform.wallet.v2.ui.widget.MarketWidget

object MarketWidgetActiveAssetStore : MarketWidgetActiveStore<MarketWidgetActiveAsset> {

    private const val PREF_ASSET_KEY = "appwidget_asset_"

    override fun save(context: Context, widgetId: Int, data: MarketWidgetActiveAsset) {
        val assets = queryAll(context, widgetId)
        val assetPair = assets.firstOrNull {
            it.id == data.id
        }
        if (assetPair == null) {
            data.order = assets.size
            assets.add(data)
        }
        saveAll(context, widgetId, assets)
    }

    override fun remove(context: Context, widgetId: Int, data: MarketWidgetActiveAsset) {
        val assets = queryAll(context, widgetId)
        val assetPair = assets.firstOrNull {
            it.id == data.id
        }
        if (assetPair != null) {
            assets.remove(assetPair)
            saveAll(context, widgetId, assets)
        }
    }

    override fun queryAll(context: Context, widgetId: Int): MutableList<MarketWidgetActiveAsset> {
        val prefs = context.getSharedPreferences(MarketWidget.PREFS_NAME, 0)
        val assets = prefs.getStringSet(PREF_ASSET_KEY + widgetId, null)
        val assetsList = mutableListOf<MarketWidgetActiveAsset>()
        assets?.forEach {
            assetsList.add(Gson().fromJson(it, MarketWidgetActiveAsset::class.java))
        }
        assetsList.sortBy { it.order }
        return assetsList
    }

    override fun saveAll(context: Context, widgetId: Int, dataList: MutableList<MarketWidgetActiveAsset>) {
        val prefs = context.getSharedPreferences(MarketWidget.PREFS_NAME, 0).edit()
        val assets = hashSetOf<String>()
        var orderIndex = 0
        dataList.forEach {
            it.order = orderIndex
            assets.add(Gson().toJson(it))
            orderIndex++
        }
        prefs.putStringSet(PREF_ASSET_KEY + widgetId, assets)
        prefs.apply()
    }

    override fun clear(context: Context, widgetId: Int) {
        val prefs = context.getSharedPreferences(MarketWidget.PREFS_NAME, 0).edit()
        prefs.remove(PREF_ASSET_KEY + widgetId)
        prefs.apply()
    }
}