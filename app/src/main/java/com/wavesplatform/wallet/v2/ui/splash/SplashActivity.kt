/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.splash

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.sdk.model.response.matcher.MarketResponse
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.WatchMarketResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.ui.home.dex.trade.TradeActivity
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.SendActivity
import com.wavesplatform.wallet.v2.ui.language.choose.ChooseLanguageActivity
import com.wavesplatform.wallet.v2.ui.welcome.WelcomeActivity
import com.wavesplatform.wallet.v2.util.MonkeyTest
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.setSystemBarTheme
import pyxis.uzuki.live.richutilskt.utils.setStatusNavBarColor
import javax.inject.Inject

class SplashActivity : BaseActivity(), SplashView {

    @Inject
    @InjectPresenter
    lateinit var presenter: SplashPresenter

    @ProvidePresenter
    fun providePresenter(): SplashPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_splash

    override fun askPassCode() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusNavBarColor(Color.WHITE)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setSystemBarTheme(false)
        if (intent.getBooleanExtra(EXIT, false)) {
            finish()
        } else if (!MonkeyTest.isTurnedOn()) {
            presenter.storeIncomingURI(intent)
            presenter.resolveNextAction()
        }
    }

    override fun onNotLoggedIn() {
        if (preferencesHelper.isTutorialPassed()) {
            if (TextUtils.isEmpty(App.getAccessManager().getLastLoggedInGuid())) {
                launchActivity<WelcomeActivity>()
                overridePendingTransition(R.anim.null_animation, R.anim.fade_out)
            } else {
                catchLinkOrMain()
            }
        } else {
            launchActivity<ChooseLanguageActivity>()
            overridePendingTransition(R.anim.null_animation, R.anim.fade_out)
        }
    }

    override fun onStartMainActivity(publicKey: String) {
        if (preferencesHelper.isTutorialPassed()) {
            catchLinkOrMain()
        } else {
            launchActivity<ChooseLanguageActivity>()
            overridePendingTransition(R.anim.null_animation, R.anim.fade_out)
        }
    }

    private fun catchLinkOrMain() {
        val url = intent.data?.toString() ?: ""
        if (intent.action == "android.intent.action.VIEW"
                && (url.contains("https://client.wavesplatform.com/#send/".toRegex()) ||
                        url.contains("https://client.wavesplatform.com/%23send/".toRegex()) ||
                        url.contains("https://dex.wavesplatform.com/#send/".toRegex()) ||
                        url.contains("https://dex.wavesplatform.com/%23send/".toRegex()))) {
            launchActivity<SendActivity> {
                putExtra(SendActivity.KEY_INTENT_DEEP_SEND, true)
                putExtra(SendActivity.KEY_INTENT_DEEP_SEND_LINK, url)
            }
        } else if (intent.action == "android.intent.action.VIEW"
                && url.contains("https://beta.wavesplatform.com/dex")) {
            presenter.loadMarkets(url)
        } else {
            launchActivity<MainActivity>(clear = true)
        }
    }

    override fun openDex(marketResponse: MarketResponse) {
        val args = Bundle()
        args.classLoader = WatchMarketResponse::class.java.classLoader
        args.putBoolean(TradeActivity.KEY_INTENT_OPEN_FROM_LINK, true)
        args.putParcelable(TradeActivity.BUNDLE_MARKET, WatchMarketResponse(marketResponse))
        launchActivity<TradeActivity>(options = args)
    }

    companion object {
        const val EXIT = "EXIT"
    }
}
