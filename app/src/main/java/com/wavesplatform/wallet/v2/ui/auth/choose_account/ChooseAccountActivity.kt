package com.wavesplatform.wallet.v2.ui.auth.choose_account

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.data.access.AccessState
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.ui.auth.choose_account.edit.EditAccountNameActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.EnterPasscodeActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.makeStyled
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.activit_choose_account.*
import kotlinx.android.synthetic.main.layout_empty_data.view.*
import pers.victor.ext.inflate
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

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true,
                getString(R.string.choose_account), R.drawable.ic_toolbar_back_black)

        recycle_addresses.layoutManager = LinearLayoutManager(this)
        recycle_addresses.adapter = adapter
        adapter.bindToRecyclerView(recycle_addresses)

        presenter.getAddresses()

        adapter.chooseAccountOnClickListener = this
    }

    override fun afterSuccessGetAddress(list: ArrayList<AddressBookUser>) {
        adapter.setNewData(list)

        adapter.emptyView = getEmptyView()
    }

    private fun getEmptyView(): View {
        val view = inflate(R.layout.layout_empty_data)
        view.text_empty.text = getString(R.string.choose_account_empty_state)
        return view
    }

    override fun onItemClicked(item: AddressBookUser) {
        val guid = AccessState.getInstance().findGuidBy(item.address)
        launchActivity<EnterPasscodeActivity>(requestCode = EnterPasscodeActivity.REQUEST_ENTER_PASS_CODE) {
            if (AccessState.getInstance().isGuidUseFingerPrint(guid)) {
                putExtra(EnterPasscodeActivity.KEY_INTENT_SHOW_FINGERPRINT, true)
            }
            putExtra(EnterPasscodeActivity.KEY_INTENT_GUID, guid)
        }
    }

    override fun onEditClicked(position: Int) {
        val item = adapter.getItem(position) as AddressBookUser
        launchActivity<EditAccountNameActivity>(REQUEST_EDIT_ACCOUNT_NAME) {
            putExtra(BUNDLE_ADDRESS_ITEM, item)
            putExtra(BUNDLE_POSITION, position)
        }
    }

    override fun onDeleteClicked(position: Int) {
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle(getString(R.string.choose_account_delete_title))
        alertDialog.setMessage(getString(R.string.choose_account_delete_msg))
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,
                getString(R.string.choose_account_yes)) { dialog, which ->
            dialog.dismiss()
            val item = adapter.getItem(position)
            if (item is AddressBookUser) {
                AccessState.getInstance().deleteWavesWallet(item.address)
                adapter.remove(position)
            }
            toast(getString(R.string.choose_account_deleted))
            adapter.notifyDataSetChanged()
        }
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,
                getString(R.string.choose_account_cancel)) { dialog, which ->
            dialog.dismiss()
        }
        alertDialog.show()
        alertDialog.makeStyled()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_EDIT_ACCOUNT_NAME -> {
                if (resultCode == Constants.RESULT_OK) {
                    val item = data?.getParcelableExtra<AddressBookUser>(BUNDLE_ADDRESS_ITEM)
                    val position = data?.getIntExtra(BUNDLE_POSITION, 0)
                    item.notNull {
                        adapter.setData(position!!, it)
                        AccessState.getInstance().saveWavesWalletNewName(item!!.address, item.name)
                    }
                }
            }
            EnterPasscodeActivity.REQUEST_ENTER_PASS_CODE -> {
                if (resultCode == Constants.RESULT_OK) {
                    launchActivity<MainActivity>(clear = true)
                }
            }
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
        overridePendingTransition(0, android.R.anim.fade_out)
    }

    companion object {
        var REQUEST_EDIT_ACCOUNT_NAME = 999
        var BUNDLE_ADDRESS_ITEM = "item"
        var BUNDLE_POSITION = "position"
    }
}
