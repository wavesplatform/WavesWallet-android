package com.wavesplatform.wallet.v2.ui.new_account

import android.app.Activity
import android.os.Bundle
import android.view.View
import javax.inject.Inject

import com.arellomobile.mvp.presenter.InjectPresenter

import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity;

import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.util.setSystemBarTheme
import kotlinx.android.synthetic.main.activity_new_account.*


class NewAccountActivity : BaseActivity(), NewAccountView {

    @Inject
    @InjectPresenter
    lateinit var presenter: NewAccountPresenter

    @ProvidePresenter
    fun providePresenter(): NewAccountPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_new_account


    override fun onViewReady(savedInstanceState: Bundle?) {
        setSystemBarTheme(false)
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, getString(R.string.new_account_toolbar_title), R.drawable.ic_toolbar_back_black)
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
        overridePendingTransition(0, 0)
    }

}
