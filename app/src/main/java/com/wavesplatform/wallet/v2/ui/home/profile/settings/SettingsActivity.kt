package com.wavesplatform.wallet.v2.ui.home.profile.settings

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import kotlinx.android.synthetic.main.activity_settings.*
import pers.victor.ext.click
import javax.inject.Inject

class SettingsActivity : BaseActivity(), SettingsView {

    @Inject
    @InjectPresenter
    lateinit var presenter: SettingsPresenter

    @ProvidePresenter
    fun providePresenter(): SettingsPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_settings

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, true, "Dev Settings", R.drawable.ic_toolbar_back_black)

        useTestNewsSwitch.isChecked = presenter.preferenceHelper.useTestNews
        useTestNewsSwitch.setOnCheckedChangeListener { _, isChecked ->
            presenter.preferenceHelper.useTestNews = isChecked
        }

        resetShowedNewsButton.click {
            prefsUtil.removeGlobalValue(PrefsUtil.SHOWED_NEWS_IDS)
        }
    }
}