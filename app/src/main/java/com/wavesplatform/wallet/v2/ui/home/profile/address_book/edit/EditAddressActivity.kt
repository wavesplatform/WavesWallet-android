package com.wavesplatform.wallet.v2.ui.home.profile.address_book.edit

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.google.zxing.integration.android.IntentIntegrator
import com.mindorks.editdrawabletext.DrawablePosition
import com.mindorks.editdrawabletext.onDrawableClickListener
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.rules.AddressBookAddressRule
import com.wavesplatform.wallet.v2.data.rules.AddressBookNameRule
import com.wavesplatform.wallet.v2.data.rules.MinTrimRule
import com.wavesplatform.wallet.v2.data.rules.NotEmptyTrimRule
import com.wavesplatform.wallet.v2.ui.auth.import_account.scan.ScanSeedFragment
import com.wavesplatform.wallet.v2.ui.auth.qr_scanner.QrCodeScannerActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity.Companion.BUNDLE_POSITION
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser
import com.wavesplatform.wallet.v2.util.AddressUtil
import com.wavesplatform.wallet.v2.util.makeStyled
import com.wavesplatform.wallet.v2.util.notNull
import io.github.anderscheow.validator.Validation
import io.github.anderscheow.validator.Validator
import io.github.anderscheow.validator.constant.Mode
import io.github.anderscheow.validator.rules.common.MaxRule
import kotlinx.android.synthetic.main.activity_edit_address.*
import pers.victor.ext.addTextChangedListener
import pers.victor.ext.click
import javax.inject.Inject

class EditAddressActivity : BaseActivity(), EditAddressView {

    @Inject
    @InjectPresenter
    lateinit var presenter: EditAddressPresenter

    lateinit var validator: Validator

    @ProvidePresenter
    fun providePresenter(): EditAddressPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_edit_address

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, true, getString(R.string.edit_address_toolbar_title), R.drawable.ic_toolbar_back_black)
        validator = Validator.with(applicationContext).setMode(Mode.CONTINUOUS)

        presenter.addressBookUser = intent.getParcelableExtra(AddressBookActivity.BUNDLE_ADDRESS_ITEM)

        val nameValidation = Validation(til_name)
                .and(NotEmptyTrimRule(R.string.address_book_name_validation_required_error))
                .and(MinTrimRule(2, R.string.address_book_name_validation_min_length_error))
                .and(MaxRule(24, R.string.address_book_name_validation_max_length_error))
                .and(AddressBookNameRule(prefsUtil, R.string.address_book_name_validation_already_use_error))

        edit_address.setDrawableClickListener(object : onDrawableClickListener {
            override fun onClick(target: DrawablePosition) {
                when (target) {
                    DrawablePosition.RIGHT -> {
                        if (edit_address.tag == R.drawable.ic_deladdress_24_error_400) {
                            edit_address.setText("")
                        } else if (edit_address.tag == R.drawable.ic_qrcode_24_basic_500) {
                            IntentIntegrator(this@EditAddressActivity).setRequestCode(ScanSeedFragment.REQUEST_SCAN_QR_CODE)
                                    .setOrientationLocked(true)
                                    .setBeepEnabled(false)
                                    .setCaptureActivity(QrCodeScannerActivity::class.java)
                                    .initiateScan()
                        }
                    }
                }
            }
        })

        fillFields()
        configureWithType()

        edit_name.addTextChangedListener {
            on { s, start, before, count ->
                validator.validate(object : Validator.OnValidateListener {
                    override fun onValidateSuccess(values: List<String>) {
                        presenter.nameFieldValid = true
                        isFieldsValid()
                    }

                    override fun onValidateFailed() {
                        presenter.nameFieldValid = false
                        isFieldsValid()
                    }
                }, nameValidation)
            }
        }

        if (edit_address.text.isEmpty()) edit_address.tag = R.drawable.ic_qrcode_24_basic_500
        else edit_address.tag = R.drawable.ic_deladdress_24_error_400

        button_save.click {
            presenter.editAddress(edit_address.text.toString(), edit_name.text.toString())
        }

        button_delete.click {
            val alertDialog = AlertDialog.Builder(this).create()
            alertDialog.setTitle(getString(R.string.edit_address_delete_alert_title))
            alertDialog.setMessage(getString(R.string.edit_address_delete_alert_description))
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.edit_address_delete_alert_delete)) { dialog, which ->
                presenter.deleteAddress()
                dialog.dismiss()
            }
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.edit_address_delete_alert_cancel)) { dialog, which ->
                dialog.dismiss()
            }
            alertDialog.show()
            alertDialog.makeStyled()
        }
    }

    private fun configureWithType() {
        val type = intent.getIntExtra(AddressBookActivity.BUNDLE_TYPE, -1)
        if (type == AddressBookActivity.SCREEN_TYPE_NOT_EDITABLE) {
            edit_address.setBackgroundColor(Color.TRANSPARENT)
            edit_address.isFocusable = false
            edit_address.isClickable = false
            edit_address.isCursorVisible = false
            edit_address.isFocusableInTouchMode = false
            edit_address.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)

            edit_address.setText(intent.getParcelableExtra<AddressBookUser>(
                    AddressBookActivity.BUNDLE_ADDRESS_ITEM).address)
            presenter.addressFieldValid = edit_address.text.isNotEmpty()
        } else if (type == AddressBookActivity.SCREEN_TYPE_EDITABLE) {
            val addressValidation = Validation(til_address)
                    .and(NotEmptyTrimRule(R.string.address_book_address_validation_required_error))
                    .and(AddressBookAddressRule(prefsUtil,
                            R.string.address_book_address_validation_already_use_error))

            edit_address.addTextChangedListener {
                on { s, start, before, count ->
                    validator.validate(object : Validator.OnValidateListener {
                        override fun onValidateSuccess(values: List<String>) {
                            presenter.addressFieldValid = true
                            isFieldsValid()
                        }

                        override fun onValidateFailed() {
                            presenter.addressFieldValid = false
                            isFieldsValid()
                        }
                    }, addressValidation)
                    if (edit_address.text.isNotEmpty()) {
                        edit_address.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_deladdress_24_error_400, 0)
                        edit_address.tag = R.drawable.ic_deladdress_24_error_400
                    } else {
                        edit_address.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_qrcode_24_basic_500, 0)
                        edit_address.tag = R.drawable.ic_qrcode_24_basic_500
                    }
                }
            }
        }
    }

    private fun fillFields() {
        edit_address.setText(presenter.addressBookUser?.address)
        edit_name.setText(presenter.addressBookUser?.name)
    }

    fun isFieldsValid() {
        button_save.isEnabled = presenter.isAllFieldsValid()
    }

    override fun successEditAddress(addressBookUser: AddressBookUser?) {
        val newIntent = Intent()
        newIntent.putExtra(AddressBookActivity.BUNDLE_ADDRESS_ITEM, addressBookUser)
        newIntent.putExtra(AddressBookActivity.BUNDLE_POSITION, intent.getIntExtra(BUNDLE_POSITION, -1))
        setResult(Constants.RESULT_OK, newIntent)
        finish()
    }

    override fun successDeleteAddress() {
        val newIntent = Intent()
        newIntent.putExtra(AddressBookActivity.BUNDLE_POSITION, intent.getIntExtra(BUNDLE_POSITION, -1))
        setResult(Constants.RESULT_OK_NO_RESULT, newIntent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ScanSeedFragment.REQUEST_SCAN_QR_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val result = IntentIntegrator.parseActivityResult(resultCode, data)
                    result.contents.replace(AddressUtil.WAVES_PREFIX, "").notNull {
                        edit_address.setText(it.trim())
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }
}
