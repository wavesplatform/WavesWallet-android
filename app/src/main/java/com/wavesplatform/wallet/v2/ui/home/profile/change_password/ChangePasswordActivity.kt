package com.wavesplatform.wallet.v2.ui.home.profile.change_password

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.data.auth.WavesWallet
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.EnterPassCodeActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import io.github.anderscheow.validator.Validation
import io.github.anderscheow.validator.Validator
import io.github.anderscheow.validator.constant.Mode
import io.github.anderscheow.validator.rules.common.EqualRule
import io.github.anderscheow.validator.rules.common.MinRule
import kotlinx.android.synthetic.main.activity_change_password.*
import pers.victor.ext.addTextChangedListener
import pers.victor.ext.click
import pers.victor.ext.toast
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
        setupToolbar(toolbar_view,  true,
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

        button_confirm.click {
            launchActivity<EnterPassCodeActivity>(
                    requestCode = EnterPassCodeActivity.REQUEST_ENTER_PASS_CODE)
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
                    writePassword(data!!.extras
                            .getString(EnterPassCodeActivity.KEY_INTENT_PASS_CODE))
                }
            }
        }
    }

    private fun writePassword(passCode: String) {
        val guid = App.getAccessManager().getLoggedInGuid()
        try {
            val oldWallet = WavesWallet(
                    App.getAccessManager()
                            .getCurrentWavesWalletEncryptedData(),
                    edit_old_password.text.toString()
            )
            val newWallet = WavesWallet(oldWallet.seed)
            val newPassWord = edit_confirm_password.text.toString()

            App.getAccessManager().storePassword(
                    guid, newWallet.publicKeyStr,
                    newWallet.getEncryptedData(newPassWord))

            App
                    .getAccessManager()
                    .writePassCodeObservable(guid, newPassWord, passCode)
                    .subscribe({
                        toast(getString(R.string.change_password_success))
                        finish()
                    }, { throwable ->
                        Log.e("CreatePassCodeActivity", throwable.message)
                    })

        } catch (e: Exception) {
            toast(getString(R.string.change_password_error))
        }
    }
}
