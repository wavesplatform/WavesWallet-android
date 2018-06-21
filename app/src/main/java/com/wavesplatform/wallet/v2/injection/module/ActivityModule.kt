package com.wavesplatform.wallet.v2.injection.module

import com.wavesplatform.wallet.v2.injection.scope.PerActivity
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.sorting.AssetsSortingActivity
import com.wavesplatform.wallet.v2.ui.language.change.ChangeLanguageActivity
import com.wavesplatform.wallet.v2.ui.language.choose.ChooseLanguageActivity
import com.wavesplatform.wallet.v2.ui.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.new_account.backup_info.BackupInfoActivity
import com.wavesplatform.wallet.v2.ui.new_account.secret_phrase.SecretPhraseActivity
import com.wavesplatform.wallet.v2.ui.splash.SplashActivity
import com.wavesplatform.wallet.v2.ui.tutorial.TutorialActivity
import com.wavesplatform.wallet.v2.ui.welcome.WelcomeActivity
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

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun tutorialActivity(): TutorialActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun newAccountActivity(): NewAccountActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun welcomeActivity(): WelcomeActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun changeLanguageActivity(): ChangeLanguageActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun secretPhraseActivity(): SecretPhraseActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun backupInfoActivity(): BackupInfoActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun mainActivity(): MainActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun assetsSortingActivity(): AssetsSortingActivity
}
