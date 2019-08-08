/*
 * Created by Eduard Zaydel on 8/8/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.local.widget

import android.content.Context
import com.wavesplatform.wallet.v2.data.manager.widget.MarketWidgetActiveAssetStore
import com.wavesplatform.wallet.v2.data.manager.widget.MarketWidgetActiveMarketStore
import com.wavesplatform.wallet.v2.data.manager.widget.MarketWidgetActiveStore
import com.wavesplatform.wallet.v2.ui.widget.MarketWidget
import com.wavesplatform.wallet.v2.util.cancelAlarmUpdate

object MarketWidgetSettings {

    fun themeSettings(): MarketWidgetStyle.Companion = MarketWidgetStyle
    fun intervalSettings(): MarketWidgetUpdateInterval.Companion = MarketWidgetUpdateInterval
    fun currencySettings(): MarketWidgetCurrency.Companion = MarketWidgetCurrency
    fun assetsSettings(): MarketWidgetActiveStore<MarketWidgetActiveAsset> = MarketWidgetActiveAssetStore
    fun marketsSettings(): MarketWidgetActiveStore<MarketWidgetActiveMarket.UI> = MarketWidgetActiveMarketStore

    fun clearSettings(context: Context, widgetId: Int) {
        context.cancelAlarmUpdate<MarketWidget>(widgetId)
        MarketWidgetStyle.removeTheme(context, widgetId)
        MarketWidgetCurrency.removeCurrency(context, widgetId)
        MarketWidgetUpdateInterval.removeInterval(context, widgetId)
        marketsSettings().clear(context, widgetId)
        assetsSettings().clear(context, widgetId)
    }

}