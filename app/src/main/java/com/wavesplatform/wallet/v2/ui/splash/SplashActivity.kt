package com.wavesplatform.wallet.v2.ui.splash

import android.os.Bundle
import android.text.TextUtils
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.BlockchainApplication
import com.wavesplatform.wallet.BuildConfig
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.EnterPassCodeActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.language.choose.ChooseLanguageActivity
import com.wavesplatform.wallet.v2.ui.welcome.WelcomeActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import javax.inject.Inject


class SplashActivity : BaseActivity(), SplashView {

    override fun onNotLoggedIn() {
        authHelper.startMainActivityAndCreateNewDBIfKeyValid(this, BuildConfig.PUBLIC_KEY)
        if (preferencesHelper.isTutorialPassed()) {

            if (!TextUtils.isEmpty(BlockchainApplication.getAccessManager().getLastLoggedInGuid())
                    && TextUtils.isEmpty(BlockchainApplication.getAccessManager().getLoggedInGuid())) {
                launchActivity<EnterPassCodeActivity>() {
                    putExtra(EnterPassCodeActivity.KEY_INTENT_PROCESS_LOGIN, true)
                }
            } else {
                launchActivity<WelcomeActivity>()
            }

        } else {
            launchActivity<ChooseLanguageActivity>()
        }
    }

    override fun onStartMainActivity(publicKey: String) {
        authHelper.startMainActivityAndCreateNewDBIfKeyValid(this, BuildConfig.PUBLIC_KEY)
        if (preferencesHelper.isTutorialPassed()) {
            launchActivity<WelcomeActivity>()
        } else {
            launchActivity<ChooseLanguageActivity>()
        }
    }

    @Inject
    @InjectPresenter
    lateinit var presenter: SplashPresenter

    @ProvidePresenter
    fun providePresenter(): SplashPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_splash


    override fun onViewReady(savedInstanceState: Bundle?) {
        presenter.storeIncomingURI(intent)
        presenter.resolveNextAction()
    }
}
