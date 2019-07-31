/*
 * Created by Eduard Zaydel on 25/7/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget.model

import android.content.Context

object MarketWidgetActiveAssetPrefStore : MarketWidgetActiveStore<MarketWidgetActiveAsset> {
    override fun save(context: Context, widgetId: Int, data: MarketWidgetActiveAsset) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remove(context: Context, widgetId: Int, data: MarketWidgetActiveAsset) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun queryAll(context: Context, widgetId: Int): MutableList<MarketWidgetActiveAsset> {
        TODO("not implemented")
    }

    override fun saveAll(context: Context, widgetId: Int, dataList: MutableList<MarketWidgetActiveAsset>) {
        TODO("not implemented")
    }

    override fun clear(context: Context, widgetId: Int) {
        TODO("not implemented")
    }
}