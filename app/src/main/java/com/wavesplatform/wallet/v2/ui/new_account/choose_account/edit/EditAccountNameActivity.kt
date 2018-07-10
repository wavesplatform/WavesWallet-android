package com.wavesplatform.wallet.v2.ui.new_account.choose_account.edit

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressTestObject
import com.wavesplatform.wallet.v2.ui.new_account.choose_account.ChooseAccountActivity.Companion.BUNDLE_POSITION
import io.github.anderscheow.validator.Validation
import io.github.anderscheow.validator.Validator
import kotlinx.android.synthetic.main.activity_edit_account_name.*
import pers.victor.ext.addTextChangedListener
import pers.victor.ext.click
import javax.inject.Inject
import io.github.anderscheow.validator.constant.Mode
import io.github.anderscheow.validator.rules.common.MaxRule
import io.github.anderscheow.validator.rules.common.NotEmptyRule
import pyxis.uzuki.live.richutilskt.utils.hideKeyboard

class EditAccountNameActivity : BaseActivity(), EditAccountNameView {

    @Inject
    @InjectPresenter
    lateinit var presenter: EditAccountNamePresenter

    @ProvidePresenter
    fun providePresenter(): EditAccountNamePresenter = presenter

    override fun configLayoutRes(): Int = R.layout.activity_edit_account_name

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener {
            onBackPressed()

        }, true, getString(R.string.edit_account_name), R.drawable.ic_toolbar_back_white)

        presenter.account = intent.getParcelableExtra(AddressBookActivity.BUNDLE_ADDRESS_ITEM)


        text_name.text = presenter.account?.name
        text_address.text = presenter.account?.address

        button_save.click {

            if (presenter.accountNameFieldValid) {
                presenter.account?.name = edit_name.text.toString()

                val newIntent = Intent()
                newIntent.putExtra(AddressBookActivity.BUNDLE_ADDRESS_ITEM, presenter.account)
                newIntent.putExtra(AddressBookActivity.BUNDLE_POSITION, intent.getIntExtra(BUNDLE_POSITION, -1))
                setResult(Constants.RESULT_OK, newIntent)
                finish()
            }
        }

        val nameValidation = Validation(til_name)
                .and(NotEmptyRule(R.string.edit_account_new_name_validation_required_error))

        edit_name.addTextChangedListener {
            on({ s, start, before, count ->
                Validator.with(applicationContext)
                        .setMode(Mode.CONTINUOUS)
                        .validate(object : Validator.OnValidateListener {
                            override fun onValidateSuccess(values: List<String>) {
                                presenter.accountNameFieldValid = true

                                button_save.isEnabled = true
                            }

                            override fun onValidateFailed() {
                                presenter.accountNameFieldValid = false

                                button_save.isEnabled = false
                            }
                        }, nameValidation)
            })
        }

    }
}
