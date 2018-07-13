package com.wavesplatform.wallet.v2.ui.import_account.protect_account

import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.passcode.create.CreatePasscodeActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import io.github.anderscheow.validator.Validation
import io.github.anderscheow.validator.Validator
import io.github.anderscheow.validator.constant.Mode
import io.github.anderscheow.validator.rules.common.EqualRule
import io.github.anderscheow.validator.rules.common.MaxRule
import io.github.anderscheow.validator.rules.common.MinRule
import io.github.anderscheow.validator.rules.common.NotEmptyRule
import kotlinx.android.synthetic.main.activity_protect_account.*
import pers.victor.ext.addTextChangedListener
import pers.victor.ext.click
import javax.inject.Inject


class ProtectAccountActivity : BaseActivity(), ProtectAccountView {

    @Inject
    @InjectPresenter
    lateinit var presenter: ProtectAccountPresenter
    lateinit var validator: Validator
    @ProvidePresenter
    fun providePresenter(): ProtectAccountPresenter = presenter

    companion object {
        var BUNDLE_ACCOUNT_ADDRESS = "account_address"
    }

    override fun configLayoutRes() = R.layout.activity_protect_account


    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, getString(R.string.enter_seed_manually_toolbar_title), R.drawable.ic_toolbar_back_black)

        validator = Validator.with(applicationContext).setMode(Mode.CONTINUOUS)

        text_account_address.text = intent.getStringExtra(BUNDLE_ACCOUNT_ADDRESS)

        isFieldsValid()

        button_create_account.click {
            launchActivity<CreatePasscodeActivity> { }
        }

        val nameValidation = Validation(til_account_name)
                .and(NotEmptyRule(R.string.new_account_account_name_validation_required_error))
                .and(MaxRule(20, R.string.new_account_account_name_validation_length_error))

        val passwordValidation = Validation(til_create_password)
                .and(MinRule(8, R.string.new_account_create_password_validation_length_error))

        edit_account_name.addTextChangedListener {
            on({ s, start, before, count ->
                validator
                        .validate(object : Validator.OnValidateListener {
                            override fun onValidateSuccess(values: List<String>) {
                                presenter.accountNameFieldValid = true
                                isFieldsValid()
                            }

                            override fun onValidateFailed() {
                                presenter.accountNameFieldValid = false
                                isFieldsValid()
                            }
                        }, nameValidation)
            })
        }
        edit_create_password.addTextChangedListener {
            on({ s, start, before, count ->
                validator
                        .validate(object : Validator.OnValidateListener {
                            override fun onValidateSuccess(values: List<String>) {
                                presenter.createPasswrodFieldValid = true
                                isFieldsValid()
                            }

                            override fun onValidateFailed() {
                                presenter.createPasswrodFieldValid = false
                                isFieldsValid()
                            }
                        }, passwordValidation)
                if (edit_confirm_password.text.isNotEmpty()){
                    val confirmPasswordValidation = Validation(til_confirm_password)
                            .and(EqualRule(edit_create_password.text.toString(), R.string.new_account_confirm_password_validation_match_error))
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
            })
        }

        edit_confirm_password.addTextChangedListener {
            on({ s, start, before, count ->
                val confirmPasswordValidation = Validation(til_confirm_password)
                        .and(EqualRule(edit_create_password.text.toString(), R.string.new_account_confirm_password_validation_match_error))
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
            })
        }
    }


    fun isFieldsValid() {
        button_create_account.isEnabled = presenter.isAllFieldsValid()
    }


}
