package com.wavesplatform.wallet.v2.ui.splash

import android.os.Bundle
import android.text.TextUtils
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.BuildConfig
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.language.choose.ChooseLanguageActivity
import com.wavesplatform.wallet.v2.ui.welcome.WelcomeActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import javax.inject.Inject


class SplashActivity : BaseActivity(), SplashView {

    override fun onNotLoggedIn() {
        authHelper.startMainActivityAndCreateNewDBIfKeyValid(this, BuildConfig.PUBLIC_KEY)
        if (preferencesHelper.isTutorialPassed()) {
            if (!TextUtils.isEmpty(App.getAccessManager().getLastLoggedInGuid())) {
                launchActivity<com.wavesplatform.wallet.v2.ui.home.MainActivity>(clear = true)
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

    override fun askPassCode() = false


    override fun onViewReady(savedInstanceState: Bundle?) {
        presenter.storeIncomingURI(intent)
        presenter.resolveNextAction()
    }
}
