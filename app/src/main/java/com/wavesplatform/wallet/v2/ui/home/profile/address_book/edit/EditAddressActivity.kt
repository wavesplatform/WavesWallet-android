package com.wavesplatform.wallet.v2.ui.home.profile.address_book.edit


import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.TextView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.mindorks.editdrawabletext.DrawablePosition
import com.mindorks.editdrawabletext.onDrawableClickListener
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity.Companion.BUNDLE_POSITION
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressTestObject
import kotlinx.android.synthetic.main.activity_edit_address.*
import pers.victor.ext.addTextChangedListener
import pers.victor.ext.click
import pyxis.uzuki.live.richutilskt.utils.toast
import javax.inject.Inject


class EditAddressActivity : BaseActivity(), EditAddressView {

    @Inject
    @InjectPresenter
    lateinit var presenter: EditAddressPresenter

    @ProvidePresenter
    fun providePresenter(): EditAddressPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_edit_address


    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, getString(R.string.edit_address_toolbar_title), R.drawable.ic_toolbar_back_black)

        presenter.address = intent.getParcelableExtra<AddressTestObject>(AddressBookActivity.BUNDLE_ADDRESS_ITEM)


        edit_address.setDrawableClickListener(object : onDrawableClickListener {
            override fun onClick(target: DrawablePosition) {
                when (target) {
                    DrawablePosition.RIGHT -> {
                        if (edit_address.tag == R.drawable.ic_deladdress_24_error_400){
                            edit_address.setText("")
                        }else if(edit_address.tag == R.drawable.ic_qrcode_24_basic_500){
                            toast("Open scan QR code")
                        }
                    }
                }
            }
        })

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
        edit_name.addTextChangedListener {
            on({ s, start, before, count ->
                presenter.nameFieldValid = edit_name.text.isNotEmpty()
                isFieldsValid()
            })
        }

        fillFields()

        if (edit_address.text.isEmpty()) edit_address.tag = R.drawable.ic_qrcode_24_basic_500
        else edit_address.tag = R.drawable.ic_deladdress_24_error_400

        button_save.click {
            val newIntent = Intent()
            newIntent.putExtra(AddressBookActivity.BUNDLE_ADDRESS_ITEM, AddressTestObject(edit_address.text.toString(), edit_name.text.toString()))
            newIntent.putExtra(AddressBookActivity.BUNDLE_POSITION, intent.getIntExtra(BUNDLE_POSITION, -1))
            setResult(Constants.RESULT_OK, newIntent)
            finish()
        }

        button_delete.click {
            val alertDialog = AlertDialog.Builder(this).create()
            alertDialog.setTitle(getString(R.string.edit_address_delete_alert_title))
            alertDialog.setMessage(getString(R.string.edit_address_delete_alert_description))
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.edit_address_delete_alert_delete),
                    DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()
                        val newIntent = Intent()
                        newIntent.putExtra(AddressBookActivity.BUNDLE_POSITION, intent.getIntExtra(BUNDLE_POSITION, -1))
                        setResult(Constants.RESULT_OK_NO_RESULT, newIntent)
                        finish()
                    })
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.edit_address_delete_alert_cancel),
                    DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
            alertDialog.show()
            val titleTextView =  alertDialog?.findViewById<TextView>(R.id.alertTitle);
            titleTextView?.typeface = ResourcesCompat.getFont(this, R.font.roboto_bold)
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
        }
    }

    private fun fillFields() {
        edit_address.setText(presenter.address?.address)
        edit_name.setText(presenter.address?.name)
    }

    fun isFieldsValid() {
        button_save.isEnabled = presenter.isAllFieldsValid()
    }

}
