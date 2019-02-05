package com.wavesplatform.sdk.injection.module

import android.app.Application
import android.content.Context
import com.wavesplatform.sdk.injection.qualifier.ApplicationContext
import dagger.Binds
import dagger.Module

@Module
abstract class ApplicationModule {

    @Binds
    @ApplicationContext
    internal abstract fun application(app: Application): Context

}
