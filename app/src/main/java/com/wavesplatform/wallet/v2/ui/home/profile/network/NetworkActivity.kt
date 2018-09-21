package com.wavesplatform.wallet.v2.ui.home.profile.network

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.BuildConfig
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import kotlinx.android.synthetic.main.activity_network.*
import javax.inject.Inject


class NetworkActivity : BaseActivity(), NetworkView {

    @Inject
    @InjectPresenter
    lateinit var presenter: NetworkPresenter

    @ProvidePresenter
    fun providePresenter(): NetworkPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_network


    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.white)
        setupToolbar(toolbar_view, true, getString(R.string.network_toolbar_title), R.drawable.ic_toolbar_back_black)

        edit_spam_filter.setText(BuildConfig.SPAM_URL)
    }

}
