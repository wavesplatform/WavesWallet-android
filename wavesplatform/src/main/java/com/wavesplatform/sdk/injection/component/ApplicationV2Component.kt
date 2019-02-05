package com.wavesplatform.sdk.injection.component

import android.app.Application
import com.wavesplatform.sdk.injection.module.ApplicationModule
import com.wavesplatform.sdk.injection.module.NetworkModule
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
    (UtilsModule::class)])
internal interface ApplicationV2Component : AndroidInjector<Application> {
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<Application>()
}
