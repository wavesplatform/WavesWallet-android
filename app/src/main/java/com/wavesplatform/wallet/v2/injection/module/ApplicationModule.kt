/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.injection.module

import android.content.Context
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.injection.qualifier.ApplicationContext
import dagger.Binds
import dagger.Module

@Module
abstract class ApplicationModule {

    @Binds
    @ApplicationContext
    internal abstract fun application(app: App): Context
}
