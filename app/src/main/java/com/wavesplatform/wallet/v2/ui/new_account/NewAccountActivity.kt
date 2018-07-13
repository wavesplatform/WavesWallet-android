package com.wavesplatform.wallet.v2.ui.new_account

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.AppCompatImageView
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.R.id.*
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.custom.Identicon
import com.wavesplatform.wallet.v2.ui.new_account.secret_phrase.SecretPhraseActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.setSystemBarTheme
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
import pers.victor.ext.getBitmap
import pyxis.uzuki.live.richutilskt.utils.runAsync
import java.util.*
import javax.inject.Inject


class NewAccountActivity : BaseActivity(), NewAccountView {

    @Inject
    @InjectPresenter
    lateinit var presenter: NewAccountPresenter
    lateinit var validator: Validator

    @ProvidePresenter
    fun providePresenter(): NewAccountPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_new_account

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, getString(R.string.new_account_toolbar_title), R.drawable.ic_toolbar_back_black)
        isFieldsValid()
        validator = Validator.with(applicationContext).setMode(Mode.CONTINUOUS)

        button_create_account.click {
            launchActivity<SecretPhraseActivity> { }
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
                var confirmPasswordValidation = Validation(til_confirm_password)
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

        linear_images.children.forEach({
            it.click {
                // delete all background of another images
                linear_images.children.forEach {
                    it.background = null
                }

                // set selected image
                it.setBackgroundResource(R.drawable.shape_outline_checked)
                avatarIsSelected(it.getBitmap())
            }


            // draw unique identicon avatar with random background color and make image with circle crop effect
            val rnd = Random()
            val color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
            Glide.with(applicationContext)
                    .load(Identicon.create((1..999).shuffled().last().toString(),
                            Identicon.Options.Builder()
                                    .setBlankColor(color)
                                    .create()))
                    .apply(RequestOptions()
                            .circleCrop())
                    .into(it as AppCompatImageView)
        })
    }

    fun isFieldsValid() {
        button_create_account.isEnabled = presenter.isAllFieldsValid()
    }

    private fun avatarIsSelected(bitmap: Bitmap) {
        presenter.avatarValid = true
        isFieldsValid()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
        overridePendingTransition(0, android.R.anim.fade_out)
    }

}
