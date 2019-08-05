/*
 * Created by Eduard Zaydel on 2/8/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.injection.module

import com.google.gson.Gson
import com.wavesplatform.wallet.v2.ui.widget.model.*
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [NetworkModule::class])
class WidgetModule {

    @Provides
    @Singleton
    internal fun provideWidgetActiveMarketStore(gson: Gson): MarketWidgetActiveStore<MarketWidgetActiveMarket.UI> {
        return MarketWidgetActiveMarketStore(gson)
    }

    @Provides
    @Singleton
    internal fun provideWidgetActiveAssetMockStore(): MarketWidgetActiveStore<MarketWidgetActiveAsset> {
        return MarketWidgetActiveAssetPrefStore
    }

}