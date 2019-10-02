/*
 * Created by Eduard Zaydel on 26/9/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.auth.passcode.enter.use_account_password.forgot_password

import android.os.Bundle
import android.support.v7.app.AlertDialog
import javax.inject.Inject
import com.arellomobile.mvp.presenter.InjectPresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.v2.data.analytics.AnalyticEvents
import com.wavesplatform.wallet.v2.data.analytics.analytics
import com.wavesplatform.wallet.v2.util.makeStyled
import kotlinx.android.synthetic.main.activity_forgot_password.*
import pers.victor.ext.click


class ForgotPasswordActivity : BaseActivity(), ForgotPasswordView {

    @Inject
    @InjectPresenter
    lateinit var presenter: ForgotPasswordPresenter

    @ProvidePresenter
    fun providePresenter(): ForgotPasswordPresenter = presenter

    override fun askPassCode() = false

    override fun configLayoutRes() = R.layout.activity_forgot_password

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, true, icon = R.drawable.ic_toolbar_back_black)

        button_reset.click {
            val alertDialog = AlertDialog.Builder(this).create()
            alertDialog.setTitle(getString(R.string.forgot_password_dialog_title))
            alertDialog.setMessage(getString(R.string.forgot_password_dialog_description))
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.forgot_password_dialog_confirm)) { dialog, which ->
                dialog.dismiss()
                // TODO: Multi account logic here
            }
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.forgot_password_dialog_cancel)) { dialog, which ->
                dialog.dismiss()
            }
            alertDialog.show()
            alertDialog.makeStyled()
        }

        button_cancel.click {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }

}
