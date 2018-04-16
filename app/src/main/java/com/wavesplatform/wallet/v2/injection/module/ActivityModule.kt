package com.wavesplatform.wallet.v2.injection.module

import com.wavesplatform.wallet.v2.injection.scope.PerActivity
import com.wavesplatform.wallet.v2.ui.choose_language.ChooseLanguageActivity
import com.wavesplatform.wallet.v2.ui.splash.SplashActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityModule {

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun splashActivity(): SplashActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun chooseLanguageActivity(): ChooseLanguageActivity

}
