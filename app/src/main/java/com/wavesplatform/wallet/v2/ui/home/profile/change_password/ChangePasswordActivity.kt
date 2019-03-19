package com.wavesplatform.wallet.v2.ui.home.profile.change_password

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.rules.EqualRule
import com.wavesplatform.wallet.v2.data.rules.EqualsAccountPasswordRule
import com.wavesplatform.wallet.v2.data.rules.MinTrimRule
import com.wavesplatform.wallet.v2.data.rules.NotEqualsAccountPasswordRule
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.EnterPassCodeActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.notNull
import io.github.anderscheow.validator.Validation
import io.github.anderscheow.validator.Validator
import io.github.anderscheow.validator.constant.Mode
import io.github.anderscheow.validator.rules.common.NotEmptyRule
import kotlinx.android.synthetic.main.activity_change_password.*
import pers.victor.ext.addTextChangedListener
import pers.victor.ext.click
import pyxis.uzuki.live.richutilskt.utils.hideKeyboard
import javax.inject.Inject

class ChangePasswordActivity : BaseActivity(), ChangePasswordView {

    @Inject
    @InjectPresenter
    lateinit var presenter: ChangePasswordPresenter
    lateinit var validator: Validator

    @ProvidePresenter
    fun providePresenter(): ChangePasswordPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_change_password

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, true,
                getString(R.string.change_password_toolbar_title), R.drawable.ic_toolbar_back_black)

        validator = Validator.with(applicationContext).setMode(Mode.CONTINUOUS)

        launchActivity<EnterPassCodeActivity>(
                requestCode = EnterPassCodeActivity.REQUEST_ENTER_PASS_CODE)

        val oldPasswordValidation = Validation(til_old_password)
                .and(NotEmptyRule(" "))
                .and(EqualsAccountPasswordRule(R.string.change_password_validation_old_password_wrong_error))

        val newPasswordValidation = Validation(til_new_password)
                .and(MinTrimRule(8, R.string.new_account_create_password_validation_length_error))
                .and(NotEqualsAccountPasswordRule(R.string.change_password_validation_new_password_already_use_error))

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
                if (edit_confirm_password.text!!.isNotEmpty()) {
                    val confirmPasswordValidation = Validation(til_confirm_password)
                            .and(EqualRule(edit_new_password.text?.trim()?.toString(),
                                    R.string.new_account_confirm_password_validation_match_error))
                    validator.validate(object : Validator.OnValidateListener {
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

        edit_confirm_password.addTextChangedListener {
            on { s, start, before, count ->
                val confirmPasswordValidation = Validation(til_confirm_password)
                        .and(EqualRule(edit_new_password.text?.trim()?.toString(),
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

        edit_confirm_password.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                goNext()
                true
            } else {
                false
            }
        }

        button_confirm.click {
            goNext()
        }
    }

    private fun goNext() {
        if (presenter.isAllFieldsValid()) {
            showProgressBar(true)
            presenter.writePassword(edit_old_password.text?.trim()?.toString(), edit_new_password.text?.trim()?.toString())
        }
    }

    fun isFieldsValid() {
        button_confirm.isEnabled = presenter.isAllFieldsValid()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {

            EnterPassCodeActivity.REQUEST_ENTER_PASS_CODE -> {
                if (resultCode == Constants.RESULT_OK) {
                    data.notNull { intent ->
                        presenter.passCode = intent.extras.getString(EnterPassCodeActivity.KEY_INTENT_PASS_CODE)
                    }
                } else {
                    setResult(Constants.RESULT_CANCELED)
                    finish()
                }
            }
        }
    }

    override fun afterSuccessChangePassword() {
        showProgressBar(false)
        setResult(Constants.RESULT_OK)
        finish()
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }
}
