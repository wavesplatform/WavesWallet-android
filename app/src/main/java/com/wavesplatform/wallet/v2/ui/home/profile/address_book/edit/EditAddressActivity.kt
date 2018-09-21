package com.wavesplatform.wallet.v2.ui.home.profile.address_book.edit


import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mindorks.editdrawabletext.DrawablePosition
import com.mindorks.editdrawabletext.onDrawableClickListener
import com.vicpin.krealmextensions.queryAsFlowable
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity.Companion.BUNDLE_POSITION
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser
import com.wavesplatform.wallet.v2.util.makeStyled
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_edit_address.*
import pers.victor.ext.addTextChangedListener
import pers.victor.ext.click
import pyxis.uzuki.live.richutilskt.utils.toast
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class EditAddressActivity : BaseActivity(), EditAddressView {

    @Inject
    @InjectPresenter
    lateinit var presenter: EditAddressPresenter

    @ProvidePresenter
    fun providePresenter(): EditAddressPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_edit_address


    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.white)
        setupToolbar(toolbar_view,  true, getString(R.string.edit_address_toolbar_title), R.drawable.ic_toolbar_back_black)

        presenter.addressBookUser = intent.getParcelableExtra<AddressBookUser>(AddressBookActivity.BUNDLE_ADDRESS_ITEM)


        edit_address.setDrawableClickListener(object : onDrawableClickListener {
            override fun onClick(target: DrawablePosition) {
                when (target) {
                    DrawablePosition.RIGHT -> {
                        if (edit_address.tag == R.drawable.ic_deladdress_24_error_400) {
                            edit_address.setText("")
                        } else if (edit_address.tag == R.drawable.ic_qrcode_24_basic_500) {
                            toast("Open scan QR code")
                        }
                    }
                }
            }
        })

        eventSubscriptions.add(RxTextView.textChanges(edit_name)
                .skipInitialValue()
                .map({
                    return@map it.toString()
                })
                .debounce(350, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .flatMap({
                    return@flatMap queryAsFlowable<AddressBookUser> { equalTo("name", it) }.toObservable()
                })
                .map({
                    presenter.nameFieldValid = edit_name.text.isNotEmpty() && it.isEmpty()
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ name ->
                    isFieldsValid()
                }))

        if (edit_address.text.isEmpty()) edit_address.tag = R.drawable.ic_qrcode_24_basic_500
        else edit_address.tag = R.drawable.ic_deladdress_24_error_400

        button_save.click {
            presenter.editAddress(edit_address.text.toString(), edit_name.text.toString())
        }

        button_delete.click {
            val alertDialog = AlertDialog.Builder(this).create()
            alertDialog.setTitle(getString(R.string.edit_address_delete_alert_title))
            alertDialog.setMessage(getString(R.string.edit_address_delete_alert_description))
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.edit_address_delete_alert_delete),
                    DialogInterface.OnClickListener { dialog, which ->
                        presenter.deleteAddress()
                        dialog.dismiss()
                    })
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.edit_address_delete_alert_cancel),
                    DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
            alertDialog.show()
            alertDialog.makeStyled()
        }

        configureWithType()
        fillFields()
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
        }else if (type == AddressBookActivity.SCREEN_TYPE_EDITABLE){

            edit_address.addTextChangedListener {
                on({ s, start, before, count ->
                    presenter.addressFieldValid = edit_address.text.isNotEmpty()
                    isFieldsValid()
                    if (presenter.addressFieldValid) {
                        edit_address.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_deladdress_24_error_400, 0)
                        edit_address.tag = R.drawable.ic_deladdress_24_error_400
                    } else {
                        edit_address.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_qrcode_24_basic_500, 0)
                        edit_address.tag = R.drawable.ic_qrcode_24_basic_500
                    }
                })
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

}
