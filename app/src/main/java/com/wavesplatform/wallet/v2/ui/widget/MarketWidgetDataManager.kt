/*
 * Created by Eduard Zaydel on 30/7/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget

import com.wavesplatform.wallet.v2.data.manager.DataServiceManager
import com.wavesplatform.wallet.v2.ui.widget.model.MarketWidgetActiveAssetMockStore
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import pyxis.uzuki.live.richutilskt.utils.runDelayed
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketWidgetDataManager @Inject constructor() {

    @Inject
    lateinit var dataServiceManager: DataServiceManager

    private val compositeDisposable = CompositeDisposable()

    fun loadMarketsPrices(widgetId: Int,
                          successListener: () -> Unit,
                          errorListener: () -> Unit) {
        val activeAssets = MarketWidgetActiveAssetMockStore.queryAll()
        compositeDisposable.add(Observable.fromIterable(activeAssets)
                .flatMap {
                    return@flatMap Observable.just(it) // TODO Change
                }.subscribe({
                    runDelayed(5000) {
                        successListener.invoke()
                    }
                }, {
                    errorListener.invoke()
                    it.printStackTrace()
                }))
    }

    fun clearSubscription() {
        compositeDisposable.clear()
    }

}