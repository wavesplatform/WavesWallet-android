package com.wavesplatform.wallet.v2.ui.home.profile.address_book.edit

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import javax.inject.Inject

import com.arellomobile.mvp.presenter.InjectPresenter


import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity;

import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity.Companion.BUNDLE_POSITION
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressTestObject
import kotlinx.android.synthetic.main.activity_edit_address.*
import pers.victor.ext.addTextChangedListener
import pers.victor.ext.click


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

        edit_address.setOnTouchListener(View.OnTouchListener { v, event ->
            val DRAWABLE_RIGHT = 2

            if (edit_address.compoundDrawables[DRAWABLE_RIGHT] != null) {
                if (event.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= edit_address.right - edit_address.compoundDrawables[DRAWABLE_RIGHT].bounds.width()) {
                        edit_address.setText("")

                        return@OnTouchListener true
                    }
                }
            }

            false
        })


        edit_address.addTextChangedListener {
            on({ s, start, before, count ->
                presenter.addressFieldValid = edit_address.text.isNotEmpty()
                isFieldsValid()
                if (presenter.addressFieldValid){
                    edit_address.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_deladdress_24_error_400, 0)
                }else{
                    edit_address.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_qrcode_24_basic_500, 0)
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

        button_save.click {
            val newIntent = Intent()
            newIntent.putExtra(AddressBookActivity.BUNDLE_ADDRESS_ITEM, AddressTestObject(edit_address.text.toString(), edit_name.text.toString()))
            newIntent.putExtra(AddressBookActivity.BUNDLE_POSITION, intent.getIntExtra(BUNDLE_POSITION, -1))
            setResult(Constants.RESULT_OK, newIntent)
            finish()
        }

        button_delete.click {
            val newIntent = Intent()
            newIntent.putExtra(AddressBookActivity.BUNDLE_POSITION, intent.getIntExtra(BUNDLE_POSITION, -1))
            setResult(Constants.RESULT_OK_NO_RESULT, newIntent)
            finish()
        }

        configureWithType()
    }

    private fun configureWithType() {
        val type = intent.getIntExtra(AddressBookActivity.BUNDLE_TYPE, -1)
        if (type == AddressBookActivity.SCREEN_TYPE_NOT_EDITABLE){
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
