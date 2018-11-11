package com.wavesplatform.wallet.v2.ui.home.profile.address_book.add

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.google.zxing.integration.android.IntentIntegrator
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mindorks.editdrawabletext.DrawablePosition
import com.mindorks.editdrawabletext.onDrawableClickListener
import com.vicpin.krealmextensions.queryAsFlowable
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.AddressUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.ui.auth.import_account.scan.ScanSeedFragment
import com.wavesplatform.wallet.v2.ui.auth.qr_scanner.QrCodeScannerActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser
import com.wavesplatform.wallet.v2.util.notNull
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_add_address.*
import pers.victor.ext.addTextChangedListener
import pers.victor.ext.click
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class AddAddressActivity : BaseActivity(), AddAddressView {
    override fun successSaveAddress(addressBookUser: AddressBookUser) {
        val intent = Intent()
        intent.putExtra(AddressBookActivity.BUNDLE_ADDRESS_ITEM, addressBookUser)
        setResult(Constants.RESULT_OK, intent)
        finish()
    }

    @Inject
    @InjectPresenter
    lateinit var presenter: AddAddressPresenter

    @ProvidePresenter
    fun providePresenter(): AddAddressPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_add_address


    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, true, getString(R.string.add_address_toolbar_title), R.drawable.ic_toolbar_back_black)

        if (edit_address.text.isEmpty()) edit_address.tag = R.drawable.ic_qrcode_24_basic_500
        else edit_address.tag = R.drawable.ic_deladdress_24_error_400

        edit_address.setDrawableClickListener(object : onDrawableClickListener {
            override fun onClick(target: DrawablePosition) {
                when (target) {
                    DrawablePosition.RIGHT -> {
                        if (edit_address.tag == R.drawable.ic_deladdress_24_error_400) {
                            edit_address.setText("")
                        } else if (edit_address.tag == R.drawable.ic_qrcode_24_basic_500) {
                            IntentIntegrator(this@AddAddressActivity).setRequestCode(ScanSeedFragment.REQUEST_SCAN_QR_CODE)
                                    .setOrientationLocked(true)
                                    .setBeepEnabled(false)
                                    .setCaptureActivity(QrCodeScannerActivity::class.java)
                                    .initiateScan()
                        }
                    }
                }
            }
        })

        eventSubscriptions.add(RxTextView.textChanges(edit_name)
                .skipInitialValue()
                .map {
                    return@map it.toString()
                }
                .debounce(350, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .flatMap {
                    return@flatMap queryAsFlowable<AddressBookUser> { equalTo("name", it) }.toObservable()
                }
                .map {
                    presenter.nameFieldValid = edit_name?.text?.isNotEmpty() == true && it.isEmpty()
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { name ->
                    isFieldsValid()
                })

        button_save.click {
            presenter.saveAddress(edit_address.text.toString(), edit_name.text.toString())
        }

        configureWithType()
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

            edit_address.setText(intent.getParcelableExtra<AddressBookUser>(AddressBookActivity.BUNDLE_ADDRESS_ITEM).address)
            presenter.addressFieldValid = edit_address.text.isNotEmpty()
        } else if (type == AddressBookActivity.SCREEN_TYPE_EDITABLE) {

            edit_address.addTextChangedListener {
                on { s, start, before, count ->
                    presenter.addressFieldValid = edit_address.text.isNotEmpty()
                    isFieldsValid()
                    if (presenter.addressFieldValid) {
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

    private fun isFieldsValid() {
        button_save.isEnabled = presenter.isAllFieldsValid()
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
}
