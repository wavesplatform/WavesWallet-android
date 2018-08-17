package com.wavesplatform.wallet.v2.injection.module

import com.wavesplatform.wallet.v2.data.service.UpdateHistoryService
import com.wavesplatform.wallet.v2.injection.scope.PerActivity
import com.wavesplatform.wallet.v2.ui.whats_new.WhatsNewActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceModule {

    @ContributesAndroidInjector
    internal abstract fun updateHistoryService(): UpdateHistoryService
}
