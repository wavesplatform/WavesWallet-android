/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.injection.component

import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.injection.module.*
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [(AndroidSupportInjectionModule::class), (ApplicationModule::class), (ActivityModule::class),
    (FragmentModule::class), (NetworkModule::class), (UtilsModule::class)])
internal interface ApplicationV2Component : AndroidInjector<App> {
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<App>()
}
