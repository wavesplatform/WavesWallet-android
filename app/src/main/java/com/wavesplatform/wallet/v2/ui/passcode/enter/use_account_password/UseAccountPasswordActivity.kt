package com.wavesplatform.wallet.v2.ui.passcode.enter.use_account_password

import android.os.Bundle
import android.view.View
import javax.inject.Inject

import com.arellomobile.mvp.presenter.InjectPresenter

import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity;

import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.ui.home.MainActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import io.github.anderscheow.validator.Validation
import io.github.anderscheow.validator.Validator
import io.github.anderscheow.validator.constant.Mode
import io.github.anderscheow.validator.rules.common.MinRule
import kotlinx.android.synthetic.main.activity_use_account_password.*
import pers.victor.ext.addTextChangedListener
import pers.victor.ext.click


class UseAccountPasswordActivity : BaseActivity(), UseAccountPasswordView {

    @Inject
    @InjectPresenter
    lateinit var presenter: UseAccountPasswordPresenter

    @ProvidePresenter
    fun providePresenter(): UseAccountPasswordPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_use_account_password


    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, icon = R.drawable.ic_toolbar_back_black)


        var accountPasswordValidation = Validation(til_account_password)
                .and(MinRule(8, R.string.new_account_create_password_validation_length_error))

        edit_account_password.addTextChangedListener {
            on({ s, start, before, count ->
                Validator.with(applicationContext)
                        .setMode(Mode.CONTINUOUS)
                        .validate(object : Validator.OnValidateListener {
                            override fun onValidateSuccess(values: List<String>) {
                                button_sign_in.isEnabled = true
                            }

                            override fun onValidateFailed() {
                                button_sign_in.isEnabled = false
                            }
                        }, accountPasswordValidation)
            })
        }

        button_sign_in.click {
            launchActivity<MainActivity> {  }
        }
    }

}
