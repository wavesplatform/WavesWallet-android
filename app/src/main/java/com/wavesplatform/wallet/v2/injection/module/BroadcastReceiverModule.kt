/*
 * Created by Eduard Zaydel on 30/7/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.injection.module

import com.wavesplatform.wallet.v2.ui.widget.MarketPulseAppWidgetProvider
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BroadcastReceiverModule {
    @ContributesAndroidInjector
    abstract fun marketWidgetReceiver(): MarketPulseAppWidgetProvider
}