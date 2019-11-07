package com.wavesplatform.wallet.v2.ui.force_update

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.util.openAppInPlayMarket
import com.wavesplatform.wallet.v2.util.setSystemBarTheme
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import pyxis.uzuki.live.richutilskt.utils.setStatusNavBarColor
import javax.inject.Inject
import kotlinx.android.synthetic.main.activity_force_update.*
import pers.victor.ext.click

class ForceUpdateActivity : BaseActivity(), ForceUpdateView {

    @Inject
    @InjectPresenter
    lateinit var presenter: ForceUpdatePresenter

    @ProvidePresenter
    fun providePresenter(): ForceUpdatePresenter = presenter

    override fun configLayoutRes() = R.layout.activity_force_update

    override fun askPassCode() = false

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusNavBarColor(Color.WHITE)
        setSystemBarTheme(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }

        button_open_play_market.click {
            openAppInPlayMarket(this)
        }
    }

    override fun onBackPressed() {
        exit()
    }
}