package com.wavesplatform.wallet.v2.ui.home.profile.change_password

import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import io.github.anderscheow.validator.Validation
import io.github.anderscheow.validator.Validator
import io.github.anderscheow.validator.constant.Mode
import io.github.anderscheow.validator.rules.common.EqualRule
import io.github.anderscheow.validator.rules.common.MinRule
import kotlinx.android.synthetic.main.activity_change_password.*
import pers.victor.ext.addTextChangedListener
import javax.inject.Inject


class ChangePasswordActivity : BaseActivity(), ChangePasswordView {

    @Inject
    @InjectPresenter
    lateinit var presenter: ChangePasswordPresenter
    lateinit var validator: Validator

    @ProvidePresenter
    fun providePresenter(): ChangePasswordPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_change_password


    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true,
                getString(R.string.change_password_toolbar_title), R.drawable.ic_toolbar_back_black)

        validator = Validator.with(applicationContext).setMode(Mode.CONTINUOUS)

        val oldPasswordValidation = Validation(til_old_password)
                .and(MinRule(8, R.string.new_account_create_password_validation_length_error))


        val newPasswordValidation = Validation(til_new_password)
                .and(MinRule(8, R.string.new_account_create_password_validation_length_error))

        edit_old_password.addTextChangedListener {
            on { s, start, before, count ->
                validator
                        .validate(object : Validator.OnValidateListener {
                            override fun onValidateSuccess(values: List<String>) {
                                presenter.oldPasswordFieldValid = true
                                isFieldsValid()
                            }

                            override fun onValidateFailed() {
                                presenter.oldPasswordFieldValid = false
                                isFieldsValid()
                            }
                        }, oldPasswordValidation)
            }
        }
        edit_new_password.addTextChangedListener {
            on { s, start, before, count ->
                validator
                        .validate(object : Validator.OnValidateListener {
                            override fun onValidateSuccess(values: List<String>) {
                                presenter.newPasswordFieldValid = true
                                isFieldsValid()
                            }

                            override fun onValidateFailed() {
                                presenter.newPasswordFieldValid = false
                                isFieldsValid()
                            }
                        }, newPasswordValidation)
            }
        }

        edit_confirm_password.addTextChangedListener {
            on { s, start, before, count ->
                val confirmPasswordValidation = Validation(til_confirm_password)
                        .and(EqualRule(edit_new_password.text.toString(),
                                R.string.new_account_confirm_password_validation_match_error))
                validator
                        .validate(object : Validator.OnValidateListener {
                            override fun onValidateSuccess(values: List<String>) {
                                presenter.confirmPasswordFieldValid = true
                                isFieldsValid()
                            }

                            override fun onValidateFailed() {
                                presenter.confirmPasswordFieldValid = false
                                isFieldsValid()
                            }
                        }, confirmPasswordValidation)
            }
        }

    }

    fun isFieldsValid() {
        button_confirm.isEnabled = presenter.isAllFieldsValid()
    }

}
