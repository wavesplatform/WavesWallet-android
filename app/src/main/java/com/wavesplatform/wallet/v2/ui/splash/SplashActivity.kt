package com.wavesplatform.wallet.v2.ui.splash

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.ui.auth.AuthUtil
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.language.choose.ChooseLanguageActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import javax.inject.Inject


class SplashActivity : BaseActivity(), SplashView {

    override fun onNotLoggedIn() {
        launchActivity<ChooseLanguageActivity>()
//        launchActivity<LandingActivity>(clear = true)
    }

    override fun onStartMainActivity(publicKey: String) {
        AuthUtil.startMainActivity(this, publicKey)
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
