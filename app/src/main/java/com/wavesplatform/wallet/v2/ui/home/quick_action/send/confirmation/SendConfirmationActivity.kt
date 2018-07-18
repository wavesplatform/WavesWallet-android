package com.wavesplatform.wallet.v2.ui.home.quick_action.send.confirmation

import android.os.Bundle
import android.view.View
import javax.inject.Inject

import com.arellomobile.mvp.presenter.InjectPresenter

import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity;

import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.wavesplatform.wallet.R
import kotlinx.android.synthetic.main.activity_send_confirmation.*


class SendConfirmationActivity : BaseActivity(), SendConfirmationView {

    @Inject @InjectPresenter lateinit var presenter: SendConfirmationPresenter

    @ProvidePresenter fun providePresenter(): SendConfirmationPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_send_confirmation


    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, getString(R.string.send_confirmation_toolbar_title), R.drawable.ic_toolbar_back_white)
    }

}
