package com.wavesplatform.wallet.v2.ui.splash

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.BuildConfig
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.helpers.AuthHelper
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.language.choose.ChooseLanguageActivity
import com.wavesplatform.wallet.v2.ui.welcome.WelcomeActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import javax.inject.Inject


class SplashActivity : BaseActivity(), SplashView {

    override fun onNotLoggedIn() {
        authHelper.startMainActivityAndCreateNewDBIfKeyValid(this, BuildConfig.PUBLIC_KEY)
        if (preferencesHelper.isTutorialPassed()) {
            launchActivity<WelcomeActivity>()
        } else {
            launchActivity<ChooseLanguageActivity>()
        }
//        launchActivity<LandingActivity>(clear = true)
    }

    override fun onStartMainActivity(publicKey: String) {
        authHelper.startMainActivityAndCreateNewDBIfKeyValid(this, BuildConfig.PUBLIC_KEY)
        if (preferencesHelper.isTutorialPassed()) {
            launchActivity<WelcomeActivity>()
        } else {
            launchActivity<ChooseLanguageActivity>()
        }
//        AuthUtil.startMainActivity(this, publicKey)
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
