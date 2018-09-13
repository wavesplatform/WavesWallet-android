package com.wavesplatform.wallet.v2.ui.auth.choose_account.edit

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.ui.auth.choose_account.ChooseAccountActivity.Companion.KEY_INTENT_ITEM_POSITION
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity
import io.github.anderscheow.validator.Validation
import io.github.anderscheow.validator.Validator
import io.github.anderscheow.validator.constant.Mode
import io.github.anderscheow.validator.rules.common.NotEmptyRule
import kotlinx.android.synthetic.main.activity_edit_account_name.*
import pers.victor.ext.addTextChangedListener
import pers.victor.ext.click
import javax.inject.Inject

class EditAccountNameActivity : BaseActivity(), EditAccountNameView {

    @Inject
    @InjectPresenter
    lateinit var presenter: EditAccountNamePresenter
    lateinit var validator: Validator

    @ProvidePresenter
    fun providePresenter(): EditAccountNamePresenter = presenter

    override fun configLayoutRes(): Int = R.layout.activity_edit_account_name

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view,  true, getString(R.string.edit_account_name), R.drawable.ic_toolbar_back_black)
        validator = Validator.with(applicationContext).setMode(Mode.CONTINUOUS)

        presenter.account = intent.getParcelableExtra(AddressBookActivity.BUNDLE_ADDRESS_ITEM)


        text_name.text = presenter.account?.name
        text_address.text = presenter.account?.address

        button_save.click {

            if (presenter.accountNameFieldValid) {
                presenter.account?.name = edit_name.text.toString()

                val newIntent = Intent()
                newIntent.putExtra(AddressBookActivity.BUNDLE_ADDRESS_ITEM, presenter.account)
                newIntent.putExtra(AddressBookActivity.BUNDLE_POSITION, intent.getIntExtra(KEY_INTENT_ITEM_POSITION, -1))
                setResult(Constants.RESULT_OK, newIntent)
                finish()
            }
        }

        val nameValidation = Validation(til_name)
                .and(NotEmptyRule(R.string.edit_account_new_name_validation_required_error))

        edit_name.addTextChangedListener {
            on { s, start, before, count ->
                validator
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
            }
        }

    }
}
