package com.wavesplatform.wallet.v2.ui.auth.import_account.protect_account

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.data.auth.WavesWallet
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.create.CreatePassCodeActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.custom.Identicon
import com.wavesplatform.wallet.v2.util.launchActivity
import io.github.anderscheow.validator.Validation
import io.github.anderscheow.validator.Validator
import io.github.anderscheow.validator.constant.Mode
import io.github.anderscheow.validator.rules.common.EqualRule
import io.github.anderscheow.validator.rules.common.MaxRule
import io.github.anderscheow.validator.rules.common.MinRule
import io.github.anderscheow.validator.rules.common.NotEmptyRule
import kotlinx.android.synthetic.main.activity_protect_account.*
import org.apache.commons.io.Charsets
import pers.victor.ext.addTextChangedListener
import pers.victor.ext.click
import javax.inject.Inject


class ProtectAccountActivity : BaseActivity(), ProtectAccountView {

    @Inject
    @InjectPresenter
    lateinit var presenter: ProtectAccountPresenter
    lateinit var validator: Validator
    private lateinit var seed: String

    @ProvidePresenter
    fun providePresenter(): ProtectAccountPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_protect_account


    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, true, icon = R.drawable.ic_toolbar_back_black)

        validator = Validator.with(applicationContext).setMode(Mode.CONTINUOUS)

        isFieldsValid()

        button_create_account.click {
            if (intent.hasExtra(NewAccountActivity.KEY_INTENT_SEED)) {
                launchActivity<CreatePassCodeActivity>(options = createDataBundle())
            }
        }

        val nameValidation = Validation(til_account_name)
                .and(NotEmptyRule(R.string.new_account_account_name_validation_required_error))
                .and(MaxRule(20, R.string.new_account_account_name_validation_length_error))

        val passwordValidation = Validation(til_create_password)
                .and(MinRule(8, R.string.new_account_create_password_validation_length_error))

        edit_account_name.addTextChangedListener {
            on { s, start, before, count ->
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
            }
        }
        edit_create_password.addTextChangedListener {
            on { s, start, before, count ->
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
                if (edit_confirm_password.text.isNotEmpty()) {
                    val confirmPasswordValidation = Validation(til_confirm_password)
                            .and(EqualRule(edit_create_password.text.toString(),
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

        edit_confirm_password.addTextChangedListener {
            on { s, start, before, count ->
                val confirmPasswordValidation = Validation(til_confirm_password)
                        .and(EqualRule(edit_create_password.text.toString(),
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

        if (intent.hasExtra(NewAccountActivity.KEY_INTENT_SEED)) {
            seed = intent.extras.getString(NewAccountActivity.KEY_INTENT_SEED)
            setAccountData()
        }
    }

    private fun createDataBundle(): Bundle {
        val options = Bundle()
        options.putString(NewAccountActivity.KEY_INTENT_SEED, seed)
        options.putString(NewAccountActivity.KEY_INTENT_ACCOUNT_NAME,
                edit_account_name.text.toString())
        options.putString(NewAccountActivity.KEY_INTENT_PASSWORD,
                edit_confirm_password.text.toString())
        return options
    }

    private fun setAccountData() {
        val wallet = WavesWallet(seed.toByteArray(Charsets.UTF_8))
        val bitmap = Identicon.create(wallet.address,
                Identicon.Options.Builder()
                        .setRandomBlankColor()
                        .create())

        Glide.with(applicationContext)
                .load(bitmap)
                .apply(RequestOptions().circleCrop())
                .into(image_account_icon)
        text_account_address.text = wallet.address
    }


    fun isFieldsValid() {
        button_create_account.isEnabled = presenter.isAllFieldsValid()
    }
}
