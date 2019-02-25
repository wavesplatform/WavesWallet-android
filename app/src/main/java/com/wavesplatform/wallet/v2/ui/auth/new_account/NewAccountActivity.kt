package com.wavesplatform.wallet.v2.ui.auth.new_account

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.widget.AppCompatImageView
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.rules.AccountNameRule
import com.wavesplatform.wallet.v2.data.rules.MinTrimRule
import com.wavesplatform.wallet.v2.data.rules.NotEmptyTrimRule
import com.wavesplatform.wallet.v2.ui.auth.new_account.secret_phrase.SecretPhraseActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.showError
import io.github.anderscheow.validator.Validation
import io.github.anderscheow.validator.Validator
import io.github.anderscheow.validator.constant.Mode
import io.github.anderscheow.validator.rules.common.EqualRule
import io.github.anderscheow.validator.rules.common.MaxRule
import kotlinx.android.synthetic.main.activity_new_account.*
import pers.victor.ext.addTextChangedListener
import pers.victor.ext.children
import pers.victor.ext.click
import pers.victor.ext.isNetworkConnected
import pyxis.uzuki.live.richutilskt.utils.hideKeyboard
import pyxis.uzuki.live.richutilskt.utils.runDelayed
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

    override fun askPassCode() = false

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, true,
                getString(R.string.new_account_toolbar_title), R.drawable.ic_toolbar_back_black)
        isFieldsValid()
        validator = Validator.with(applicationContext).setMode(Mode.CONTINUOUS)

        button_create_account.click {
            goNext()
        }

        val nameValidation = Validation(til_account_name)
                .and(NotEmptyTrimRule(R.string.new_account_account_name_validation_required_error))
                .and(MinTrimRule(2, R.string.new_account_account_name_validation_min_length_error))
                .and(MaxRule(24, R.string.new_account_account_name_validation_length_error))
                .and(AccountNameRule(R.string.new_account_account_name_validation_already_exist_error))

        val passwordValidation = Validation(til_create_password)
                .and(MinTrimRule(8, R.string.new_account_create_password_validation_length_error))

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
                if (edit_confirm_password.text?.trim()?.isNotEmpty() == true) {
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
                        .and(EqualRule(edit_create_password.text?.trim()?.toString(),
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
                runDelayed(250) {
                    goNext()
                }
                true
            } else {
                false
            }
        }

        presenter.generateSeeds(this, linear_images.children as List<AppCompatImageView>)
    }

    private fun goNext() {
        if (presenter.isAllFieldsValid() && isNetworkConnected()) {
            if (presenter.avatarValid) {
                launchActivity<SecretPhraseActivity>(options = createDataBundle())
            } else {
                showError(R.string.new_account_avatar_error, R.id.relative_root)
                val animation = AnimationUtils.loadAnimation(this, R.anim.shake_error)
                linear_images?.startAnimation(animation)
            }
        }
    }

    private fun createDataBundle(): Bundle {
        val options = Bundle()
        options.putBoolean(KEY_INTENT_PROCESS_ACCOUNT_CREATION, true)
        options.putString(KEY_INTENT_SEED, seed)
        options.putString(KEY_INTENT_ACCOUNT_NAME, edit_account_name.text.toString().trim())
        options.putString(KEY_INTENT_PASSWORD, edit_create_password.text.toString().trim())
        return options
    }

    override fun afterSuccessGenerateAvatar(seed: String, bitmap: Bitmap, imageView: AppCompatImageView) {
        Glide.with(applicationContext)
                .load(bitmap)
                .apply(RequestOptions()
                        .error(R.drawable.shape_white_oval)
                        .placeholder(R.drawable.shape_white_oval)
                        .dontAnimate()
                        .circleCrop())
                .into(imageView)

        imageView.setBackgroundResource(R.drawable.shape_outline)

        imageView.click {
            setImageActive(seed, it)
        }
    }

    private fun setImageActive(seed: String, view: View) {
        this.seed = seed

        linear_images.children.forEach {
            it.background = null
            it.alpha = 0.3F
        }

        view.setBackgroundResource(R.drawable.shape_outline_checked)
        view.alpha = 1F
        presenter.avatarValid = true
        isFieldsValid()
    }

    private fun isFieldsValid() {
        button_create_account.isEnabled = presenter.isAllFieldsValid() && isNetworkConnected()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }

    override fun needToShowNetworkMessage(): Boolean = true

    override fun onNetworkConnectionChanged(networkConnected: Boolean) {
        super.onNetworkConnectionChanged(networkConnected)
        button_create_account.isEnabled = presenter.isAllFieldsValid() && networkConnected
    }

    companion object {
        const val KEY_INTENT_PROCESS_ACCOUNT_CREATION = "intent_process_account_creation"
        const val KEY_INTENT_PROCESS_ACCOUNT_IMPORT = "intent_process_account_import"
        const val KEY_INTENT_ACCOUNT_NAME = "intent_account_name"
        const val KEY_INTENT_PASSWORD = "intent_password"
        const val KEY_INTENT_SEED = "intent_seed"
        const val KEY_INTENT_SKIP_BACKUP = "intent_skip_backup"
    }
}
