package com.wavesplatform.wallet.v2.injection.module

import com.wavesplatform.wallet.v2.injection.scope.PerActivity
import com.wavesplatform.wallet.v2.ui.address.MyAddressQRActivity
import com.wavesplatform.wallet.v2.ui.choose_account.ChooseAccountActivity
import com.wavesplatform.wallet.v2.ui.choose_account.edit.EditAccountNameActivity
import com.wavesplatform.wallet.v2.ui.fingerprint.UseFingerprintActivity
import com.wavesplatform.wallet.v2.ui.home.profile.change_password.ChangePasswordActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.add.AddAddressActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.edit.EditAddressActivity
import com.wavesplatform.wallet.v2.ui.home.profile.addresses.AddressesAndKeysActivity
import com.wavesplatform.wallet.v2.ui.home.profile.addresses.create.CreateAliasActivity
import com.wavesplatform.wallet.v2.ui.home.profile.backup.BackupPhraseActivity
import com.wavesplatform.wallet.v2.ui.home.profile.backup.confirm.ConfirmBackupPhraseActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.details.AssetDetailsActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.sorting.AssetsSortingActivity
import com.wavesplatform.wallet.v2.ui.language.change_welcome.ChangeLanguageActivity
import com.wavesplatform.wallet.v2.ui.language.change_welcome.ChangeWelcomeLanguageActivity
import com.wavesplatform.wallet.v2.ui.language.choose.ChooseLanguageActivity
import com.wavesplatform.wallet.v2.ui.home.profile.network.NetworkActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.leasing.confirmation.ConfirmationLeasingActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.leasing.start.StartLeasingActivity
import com.wavesplatform.wallet.v2.ui.import_account.ImportAccountActivity
import com.wavesplatform.wallet.v2.ui.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.new_account.backup_info.BackupInfoActivity
import com.wavesplatform.wallet.v2.ui.new_account.secret_phrase.SecretPhraseActivity
import com.wavesplatform.wallet.v2.ui.passcode.create.CreatePasscodeActivity
import com.wavesplatform.wallet.v2.ui.passcode.enter.EnterPasscodeActivity
import com.wavesplatform.wallet.v2.ui.passcode.enter.use_account_password.UseAccountPasswordActivity
import com.wavesplatform.wallet.v2.ui.qr_scanner.CustomScannerActivity
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
    internal abstract fun changeWelcomeLanguageActivity(): ChangeWelcomeLanguageActivity

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

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun assetDetailsActivity(): AssetDetailsActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun myAddressQRActivity(): MyAddressQRActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun addressBookActivity(): AddressBookActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun profileAddressesActivity(): AddressesAndKeysActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun createAliasActivity(): CreateAliasActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun backupPharseActivity(): BackupPhraseActivity


    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun addAddressActivity(): AddAddressActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun editAddressActivity(): EditAddressActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun changeLanguageActivity(): ChangeLanguageActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun changePasswordActivity(): ChangePasswordActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun networkActivity(): NetworkActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun confirmBackupPharseActivity(): ConfirmBackupPhraseActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun createPasscodeActivity(): CreatePasscodeActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun useFingerprintActivity(): UseFingerprintActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun chooseAccountActivity(): ChooseAccountActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun editAccountNameActivity(): EditAccountNameActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun enterPasscodeActivity(): EnterPasscodeActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun useAccountPasswordActivity(): UseAccountPasswordActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun startLeasingActivity(): StartLeasingActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun confirmationLeasingActivity(): ConfirmationLeasingActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun importAccountActivity(): ImportAccountActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun customScannerActivity(): CustomScannerActivity
}
