package com.wavesplatform.wallet.v2.injection.component

import com.wavesplatform.wallet.BlockchainApplication
import com.wavesplatform.wallet.v2.data.helpers.PublicKeyAccountHelper
import com.wavesplatform.wallet.v2.injection.module.*
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [(AndroidSupportInjectionModule::class), (ApplicationModule::class), (ActivityModule::class), (FragmentModule::class), (NetworkModule::class), (ServiceModule::class)])
internal interface ApplicationV2Component : AndroidInjector<BlockchainApplication> {
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<BlockchainApplication>()

    fun keyAccountHelper(): PublicKeyAccountHelper
}
