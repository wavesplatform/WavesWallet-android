package com.wavesplatform.wallet.v2.injection.component

import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.injection.module.*
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    (AndroidSupportInjectionModule::class),
    (ApplicationModule::class),
    (NetworkModule::class),
    (ServiceModule::class),
    (UtilsModule::class)])
internal interface ApplicationV2Component : AndroidInjector<App> {
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<App>()
}
