package com.wavesplatform.wallet.v2.ui.passcode.enter

import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.custom.PassCodeEntryKeypad
import kotlinx.android.synthetic.main.activity_create_passcode.*
import javax.inject.Inject


class EnterPasscodeActivity : BaseActivity(), EnterPasscodeView {

    @Inject
    @InjectPresenter
    lateinit var presenter: EnterPasscodePresenter

    @ProvidePresenter
    fun providePresenter(): EnterPasscodePresenter = presenter

    override fun configLayoutRes() = R.layout.activity_enter_passcode


    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, icon = R.drawable.ic_toolbar_back_black)

        pass_keypad.attachDots(pdl_dots)
        pass_keypad.setPadClickedListener(object : PassCodeEntryKeypad.OnPinEntryPadClickedListener {
            override fun onPassCodeEntered(passCode: String) {

            }
        })
    }
}
