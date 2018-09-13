package com.wavesplatform.wallet.v2.ui.auth.new_account

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.widget.AppCompatImageView
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.wavesplatform.wallet.BlockchainApplication
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.auth.new_account.secret_phrase.SecretPhraseActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import io.github.anderscheow.validator.Validation
import io.github.anderscheow.validator.Validator
import io.github.anderscheow.validator.constant.Mode
import io.github.anderscheow.validator.rules.common.EqualRule
import io.github.anderscheow.validator.rules.common.MaxRule
import io.github.anderscheow.validator.rules.common.MinRule
import io.github.anderscheow.validator.rules.common.NotEmptyRule
import kotlinx.android.synthetic.main.activity_new_account.*
import pers.victor.ext.addTextChangedListener
import pers.victor.ext.children
import pers.victor.ext.click
import pers.victor.ext.toast
import javax.inject.Inject


class NewAccountActivity : BaseActivity(), NewAccountView {

    @Inject
    @InjectPresenter
    lateinit var presenter: NewAccountPresenter
    lateinit var validator: Validator
    private var seed: String? = null

    @ProvidePresenter
    fun providePresenter(): NewAccountPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_new_account

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, true,
                getString(R.string.new_account_toolbar_title), R.drawable.ic_toolbar_back_black)
        isFieldsValid()
        validator = Validator.with(applicationContext).setMode(Mode.CONTINUOUS)

        button_create_account.click {
            if (BlockchainApplication.getAccessManager()
                            .isAccountNameExist(edit_account_name.text.toString())) {
                toast(getString(R.string.new_account_exist_error))
            } else {
                launchActivity<SecretPhraseActivity>(options = createDataBundle())
            }
        }

        val nameValidation = Validation(til_account_name)
                .and(NotEmptyRule(R.string.new_account_account_name_validation_required_error))
                .and(MaxRule(20, R.string.new_account_account_name_validation_length_error))

        val passwordValidation = Validation(til_create_password)
                .and(MinRule(8, R.string.new_account_create_password_validation_length_error))

        edit_account_name.addTextChangedListener {
            on { s, start, before, count ->
                validator.validate(object : Validator.OnValidateListener {
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
                validator.validate(object : Validator.OnValidateListener {
                    override fun onValidateSuccess(values: List<String>) {
                        presenter.createPasswordFieldValid = true
                        isFieldsValid()
                    }

                    override fun onValidateFailed() {
                        presenter.createPasswordFieldValid = false
                        isFieldsValid()
                    }
                }, passwordValidation)
                if (edit_confirm_password.text.isNotEmpty()) {
                    val confirmPasswordValidation = Validation(til_confirm_password)
                            .and(EqualRule(edit_create_password.text.toString(),
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

        presenter.generateSeeds(this, linear_images.children as List<AppCompatImageView>)
    }

    private fun createDataBundle(): Bundle {
        val options = Bundle()
        options.putBoolean(KEY_INTENT_PROCESS_ACCOUNT_CREATION, true)
        options.putString(KEY_INTENT_SEED, seed)
        options.putString(KEY_INTENT_ACCOUNT_NAME, edit_account_name.text.toString())
        options.putString(KEY_INTENT_PASSWORD, edit_create_password.text.toString())
        return options
    }

    override fun afterSuccessGenerateAvatar(seed: String, bitmap: Bitmap, imageView: AppCompatImageView) {
        Glide.with(applicationContext)
                .load(bitmap)
                .apply(RequestOptions().circleCrop())
                .into(imageView)

        if (linear_images.children.isNotEmpty() && linear_images.children[0] == imageView) {
            setImageActive(seed, imageView)
        }

        imageView.click {
            setImageActive(seed, it)
        }
    }

    private fun setImageActive(seed: String, view: View) {
        this.seed = seed

        linear_images.children.forEach { it.background = null }

        view.setBackgroundResource(R.drawable.shape_outline_checked)
        presenter.avatarValid = true
        isFieldsValid()
    }

    private fun isFieldsValid() {
        button_create_account.isEnabled = presenter.isAllFieldsValid()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
        overridePendingTransition(0, android.R.anim.fade_out)
    }

    companion object {
        const val KEY_INTENT_PROCESS_ACCOUNT_CREATION = "intent_process_account_creation"
        const val KEY_INTENT_ACCOUNT_NAME = "intent_account_name"
        const val KEY_INTENT_PASSWORD = "intent_password"
        const val KEY_INTENT_SEED = "intent_seed"
        const val KEY_INTENT_SKIP_BACKUP = "intent_skip_backup"
    }
}
