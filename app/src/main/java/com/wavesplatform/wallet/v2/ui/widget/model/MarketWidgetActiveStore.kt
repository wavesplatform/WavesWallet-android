/*
 * Created by Eduard Zaydel on 25/7/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget.model

import android.content.Context

interface MarketWidgetActiveStore<T> {
    fun queryAll(context: Context, widgetId: Int): MutableList<T>
    fun save(context: Context, widgetId: Int, data: T)
    fun saveAll(context: Context, widgetId: Int, dataList: MutableList<T>)
    fun remove(context: Context, widgetId: Int, data: T)
    fun clear(context: Context, widgetId: Int)
}