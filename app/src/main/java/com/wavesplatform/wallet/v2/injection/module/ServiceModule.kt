/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.injection.module

import com.wavesplatform.wallet.v2.data.service.UpdateApiDataService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceModule {

    @ContributesAndroidInjector
    internal abstract fun updateHistoryService(): UpdateApiDataService
}
