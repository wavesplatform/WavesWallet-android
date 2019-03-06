package com.wavesplatform.wallet.v2.ui.home.profile.network

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.rules.NotEmptyTrimRule
import com.wavesplatform.wallet.v2.data.rules.UrlRule
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import io.github.anderscheow.validator.Validation
import io.github.anderscheow.validator.Validator
import io.github.anderscheow.validator.constant.Mode
import kotlinx.android.synthetic.main.activity_network.*
import pers.victor.ext.addTextChangedListener
import pers.victor.ext.app
import pers.victor.ext.click
import pers.victor.ext.isNetworkConnected
import javax.inject.Inject

class NetworkActivity : BaseActivity(), NetworkView {

    @Inject
    @InjectPresenter
    lateinit var presenter: NetworkPresenter

    var validator = Validator.with(app).setMode(Mode.CONTINUOUS)

    @ProvidePresenter
    fun providePresenter(): NetworkPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_network

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, true, getString(R.string.network_toolbar_title), R.drawable.ic_toolbar_back_black)

        edit_spam_filter.setText(prefsUtil.getValue(PrefsUtil.KEY_SPAM_URL, EnvironmentManager.servers.spamUrl))

        switch_spam_filter.isChecked = prefsUtil.getValue(PrefsUtil.KEY_ENABLE_SPAM_FILTER, true)
        switch_spam_filter.setOnCheckedChangeListener { buttonView, isChecked ->
            presenter.spamFilterEnableValid = prefsUtil.getValue(PrefsUtil.KEY_ENABLE_SPAM_FILTER, true) != isChecked
            isFieldsValid()
        }

        edit_spam_filter.addTextChangedListener {
            on { s, start, before, count ->
                val spamUrlValidation = Validation(til_spam_filter)
                        .and(NotEmptyTrimRule(R.string.network_spam_url_validation_url_empty))
                        .and(UrlRule(R.string.network_spam_url_validation_bad_url))
                validator
                        .validate(object : Validator.OnValidateListener {
                            override fun onValidateSuccess(values: List<String>) {
                                presenter.spamUrlFieldValid = prefsUtil.getValue(
                                        PrefsUtil.KEY_SPAM_URL,
                                        EnvironmentManager.servers.spamUrl) != edit_spam_filter.text.toString()
                                isFieldsValid()
                            }

                            override fun onValidateFailed() {
                                presenter.spamUrlFieldValid = false
                                isFieldsValid()
                            }
                        }, spamUrlValidation)
            }
        }

        button_set_default.click {
            if (edit_spam_filter.text.toString() != EnvironmentManager.servers.spamUrl) {
                edit_spam_filter.setText(EnvironmentManager.servers.spamUrl)
            }
            switch_spam_filter.isChecked = true
        }

        button_save.click {
            when {
                presenter.spamFilterEnableValid && presenter.spamUrlFieldValid -> {
                    presenter.checkValidUrl(edit_spam_filter.text.toString().trim())
                }
                presenter.spamFilterEnableValid -> {
                    prefsUtil.setValue(PrefsUtil.KEY_ENABLE_SPAM_FILTER, switch_spam_filter.isChecked)
                    mRxEventBus.post(Events.SpamFilterStateChanged())
                    finish()
                }
                presenter.spamUrlFieldValid -> {
                    presenter.checkValidUrl(edit_spam_filter.text.toString().trim())
                }
            }
        }
    }

    override fun afterSuccessCheckSpamUrl(isValid: Boolean) {
        if (isValid) {
            prefsUtil.setValue(PrefsUtil.KEY_ENABLE_SPAM_FILTER, switch_spam_filter.isChecked)
            prefsUtil.setValue(PrefsUtil.KEY_SPAM_URL, edit_spam_filter.text.toString().trim())
            mRxEventBus.post(Events.SpamFilterUrlChanged())
            finish()
        } else {
            til_spam_filter.error = getString(R.string.network_spam_url_validation_bad_url)
            button_save.isEnabled = false
        }
    }

    private fun isFieldsValid() {
        button_save.isEnabled = presenter.isAllFieldsValid() && isNetworkConnected()
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }

    override fun needToShowNetworkMessage() = true

    override fun onNetworkConnectionChanged(networkConnected: Boolean) {
        super.onNetworkConnectionChanged(networkConnected)
        button_save.isEnabled = presenter.isAllFieldsValid() && networkConnected
    }
}
