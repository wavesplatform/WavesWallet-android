package com.wavesplatform.wallet.v2.ui.force_update

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.local.PreferencesHelper
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
        setStatusNavBarColor(ContextCompat.getColor(this, R.color.basic50))
        setSystemBarTheme(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            button_open_play_market.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }

        force_update_subtitle?.text = getString(R.string.force_update_please_update_to_continue,
                preferencesHelper.forceUpdateAppVersion)

        button_open_play_market.click {
            openAppInPlayMarket(this)
        }
    }

    override fun onBackPressed() {
        exit()
    }
}