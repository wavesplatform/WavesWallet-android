package com.wavesplatform.wallet.v2.injection.module

import com.wavesplatform.wallet.v2.injection.scope.PerActivity
import com.wavesplatform.wallet.v2.ui.auth.choose_account.ChooseAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.choose_account.edit.EditAccountNameActivity
import com.wavesplatform.wallet.v2.ui.auth.fingerprint.UseFingerprintActivity
import com.wavesplatform.wallet.v2.ui.auth.import_account.ImportAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.import_account.protect_account.ProtectAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.new_account.secret_phrase.SecretPhraseActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.create.CreatePassCodeActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.EnterPassCodeActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.use_account_password.UseAccountPasswordActivity
import com.wavesplatform.wallet.v2.ui.auth.qr_scanner.QrCodeScannerActivity
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.ui.home.dex.markets.DexMarketsActivity
import com.wavesplatform.wallet.v2.ui.home.dex.sorting.ActiveMarketsSortingActivity
import com.wavesplatform.wallet.v2.ui.home.dex.trade.TradeActivity
import com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.success.TradeBuyAndSendSuccessActivity
import com.wavesplatform.wallet.v2.ui.home.history.HistoryActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.add.AddAddressActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.edit.EditAddressActivity
import com.wavesplatform.wallet.v2.ui.home.profile.addresses.AddressesAndKeysActivity
import com.wavesplatform.wallet.v2.ui.home.profile.addresses.alias.create.CreateAliasActivity
import com.wavesplatform.wallet.v2.ui.home.profile.backup.BackupPhraseActivity
import com.wavesplatform.wallet.v2.ui.home.profile.backup.confirm.ConfirmBackupPhraseActivity
import com.wavesplatform.wallet.v2.ui.home.profile.change_password.ChangePasswordActivity
import com.wavesplatform.wallet.v2.ui.home.profile.network.NetworkActivity
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.ReceiveActivity
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.about_redirection.AboutRedirectionActivity
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.address_view.ReceiveAddressViewActivity
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.SendActivity
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.confirmation.SendConfirmationActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.address.MyAddressQRActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.details.AssetDetailsActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.sorting.AssetsSortingActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.token_burn.TokenBurnActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.token_burn.confirmation.TokenBurnConfirmationActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.leasing.cancel.confirmation.ConfirmationCancelLeasingActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.leasing.start.StartLeasingActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.leasing.start.confirmation.ConfirmationStartLeasingActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.your_assets.YourAssetsActivity
import com.wavesplatform.wallet.v2.ui.language.change_welcome.ChangeLanguageActivity
import com.wavesplatform.wallet.v2.ui.language.choose.ChooseLanguageActivity
import com.wavesplatform.wallet.v2.ui.splash.SplashActivity
import com.wavesplatform.wallet.v2.ui.success.SuccessActivity
import com.wavesplatform.wallet.v2.ui.tutorial.TutorialActivity
import com.wavesplatform.wallet.v2.ui.web.WebActivity
import com.wavesplatform.wallet.v2.ui.welcome.WelcomeActivity
import com.wavesplatform.wallet.v2.ui.whats_new.WhatsNewActivity
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
    internal abstract fun secretPhraseActivity(): SecretPhraseActivity

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
    internal abstract fun createPasscodeActivity(): CreatePassCodeActivity

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
    internal abstract fun enterPasscodeActivity(): EnterPassCodeActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun useAccountPasswordActivity(): UseAccountPasswordActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun startLeasingActivity(): StartLeasingActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun confirmationLeasingActivity(): ConfirmationStartLeasingActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun importAccountActivity(): ImportAccountActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun qrCodeScannerActivity(): QrCodeScannerActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun protectAccountActivity(): ProtectAccountActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun yourAssetsActivity(): YourAssetsActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun receiveActivity(): ReceiveActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun aboutRedirectionActivity(): AboutRedirectionActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun sendActivity(): SendActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun sendConfirmationActivity(): SendConfirmationActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun receiveLoadingActivity(): ReceiveAddressViewActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun activeMarketsSortingActivity(): ActiveMarketsSortingActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun dexMarketsActivity(): DexMarketsActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun tradeActivity(): TradeActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun tokenBurnActivity(): TokenBurnActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun tokenBurnConfirmationActivity(): TokenBurnConfirmationActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun tradeBuyAndSendSucessActivity(): TradeBuyAndSendSuccessActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun whatsNewActivity(): WhatsNewActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun leasingHistoryActivity(): HistoryActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun webActivity(): WebActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun successActivity(): SuccessActivity

    @PerActivity
    @ContributesAndroidInjector
    internal abstract fun confirmationCancelLeasingActivity(): ConfirmationCancelLeasingActivity
}
