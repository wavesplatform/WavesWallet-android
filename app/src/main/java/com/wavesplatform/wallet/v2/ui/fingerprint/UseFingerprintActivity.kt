package com.wavesplatform.wallet.v2.ui.fingerprint

import android.os.Bundle
import javax.inject.Inject

import com.arellomobile.mvp.presenter.InjectPresenter
import com.wavesplatform.wallet.v2.ui.fingerprint.UseFingerprintView
import com.wavesplatform.wallet.v2.ui.fingerprint.UseFingerprintPresenter

import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity;

import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.activity_use_fingerprint.*
import pers.victor.ext.click


class UseFingerprintActivity : BaseActivity(), UseFingerprintView {

    @Inject
    @InjectPresenter
    lateinit var presenter: UseFingerprintPresenter

    @ProvidePresenter
    fun providePresenter(): UseFingerprintPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_use_fingerprint


    override fun onViewReady(savedInstanceState: Bundle?) {
        button_use_fingerprint.click{
            launchActivity<MainActivity> {  }
        }

        button_do_it_later.click{
            launchActivity<MainActivity> {  }
        }
    }

    override fun onBackPressed() {

    }

}
