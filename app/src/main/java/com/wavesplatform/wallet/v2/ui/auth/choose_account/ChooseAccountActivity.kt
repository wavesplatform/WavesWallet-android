package com.wavesplatform.wallet.v2.ui.auth.choose_account

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.ui.auth.choose_account.edit.EditAccountNameActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.EnterPasscodeActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressTestObject
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.activit_choose_account.*
import pers.victor.ext.toast
import javax.inject.Inject

class ChooseAccountActivity : BaseActivity(), ChooseAccountView, ChooseAccountOnClickListener {


    @Inject
    @InjectPresenter
    lateinit var presenter: ChooseAccountPresenter

    @ProvidePresenter
    fun providePresenter(): ChooseAccountPresenter = presenter

    @Inject
    lateinit var adapter: ChooseAccountAdapter

    override fun configLayoutRes(): Int = R.layout.activit_choose_account

    companion object {
        var REQUEST_EDIT_ACCOUNT_NAME = 999
        var REQUEST_ENTER_PASSCODE = 555
        var BUNDLE_ADDRESS_ITEM = "item"
        var BUNDLE_POSITION = "position"
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, getString(R.string.choose_account), R.drawable.ic_toolbar_back_white)

        recycle_addresses.layoutManager = LinearLayoutManager(this)
        recycle_addresses.adapter = adapter
        adapter.bindToRecyclerView(recycle_addresses)

        presenter.getAddresses()

        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            launchActivity<EnterPasscodeActivity>(requestCode = REQUEST_ENTER_PASSCODE) {  }
        }

        adapter.chooseAccountOnClickListener = this
    }

    override fun afterSuccessGetAddress(list: ArrayList<AddressTestObject>) {
        adapter.setNewData(list)

        adapter.setEmptyView(R.layout.choose_account_empty_state)
    }

    override fun onEditClicked(position: Int) {
        val item = adapter.getItem(position) as AddressTestObject
        launchActivity<EditAccountNameActivity>(REQUEST_EDIT_ACCOUNT_NAME) {
            putExtra(BUNDLE_ADDRESS_ITEM, item)
            putExtra(BUNDLE_POSITION, position)
        }
    }

    override fun onDeleteClicked() {
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle(getString(R.string.choose_account_delete_title))
        alertDialog.setMessage(getString(R.string.choose_account_delete_msg))
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.choose_account_yes),
                DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                    toast("Deleted")
                })
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.choose_account_cancel),
                DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
        alertDialog.show()
        val titleTextView = alertDialog?.findViewById<TextView>(R.id.alertTitle);
        titleTextView?.typeface = ResourcesCompat.getFont(this, R.font.roboto_bold)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_EDIT_ACCOUNT_NAME -> {
                if (resultCode == Constants.RESULT_OK) {
                    val item = data?.getParcelableExtra<AddressTestObject>(BUNDLE_ADDRESS_ITEM)
                    val position = data?.getIntExtra(BUNDLE_POSITION, 0)
                    item.notNull {
                        adapter.setData(position!!, it)
                    }
                }
            }
            REQUEST_ENTER_PASSCODE -> {
                launchActivity<MainActivity>(clear = true){ }
            }
        }
    }
}
