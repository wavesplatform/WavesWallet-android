/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.auth.choose_account

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.sdk.WavesSdk
import com.wavesplatform.sdk.utils.notNull
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.analytics.AnalyticEvents
import com.wavesplatform.wallet.v2.data.analytics.analytics
import com.wavesplatform.wallet.v2.data.helpers.KeeperIntentHelper
import com.wavesplatform.wallet.v2.data.model.db.userdb.AddressBookUserDb
import com.wavesplatform.wallet.v2.data.model.local.NewAccountDialogItem
import com.wavesplatform.wallet.v2.ui.auth.choose_account.edit.EditAccountNameActivity
import com.wavesplatform.wallet.v2.ui.auth.import_account.ImportAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.EnterPassCodeActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.ui.keeper.KeeperTransactionActivity
import com.wavesplatform.wallet.v2.ui.welcome.WelcomeActivity
import com.wavesplatform.wallet.v2.ui.widget.configuration.options.OptionsBottomSheetFragment
import com.wavesplatform.wallet.v2.util.*
import kotlinx.android.synthetic.main.activity_choose_account.*
import kotlinx.android.synthetic.main.content_empty_data.view.*
import pers.victor.ext.inflate
import javax.inject.Inject


class ChooseAccountActivity : BaseActivity(), ChooseAccountView, ChooseAccountOnClickListener {

    @Inject
    @InjectPresenter
    lateinit var presenter: ChooseAccountPresenter

    @ProvidePresenter
    fun providePresenter(): ChooseAccountPresenter = presenter

    @Inject
    lateinit var adapter: ChooseAccountAdapter

    override fun configLayoutRes(): Int = R.layout.activity_choose_account

    override fun askPassCode() = false

    private var link = ""

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        setNavigationBarColor(R.color.basic50)
        setupToolbar(toolbar_view, true,
                getString(R.string.choose_account), R.drawable.ic_toolbar_back_black)

        presenter.checkKeeperIntent(intent)

        recycle_addresses.layoutManager = LinearLayoutManager(this)
        recycle_addresses.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                appbar_layout.isSelected = recycle_addresses.canScrollVertically(-1)
            }
        })
        adapter.bindToRecyclerView(recycle_addresses)
        adapter.chooseAccountOnClickListener = this

        presenter.getAddresses()
    }

    override fun afterSuccessGetAddress(list: ArrayList<AddressBookUserDb>) {
        analytics.trackEvent(AnalyticEvents.StartAccountCounter(list.size))

        adapter.setNewData(list)
        adapter.emptyView = getEmptyView()
    }

    private fun getEmptyView(): View {
        val view = inflate(R.layout.content_empty_data)
        view.text_empty.text = getString(R.string.choose_account_empty_state)
        return view
    }

    override fun onItemClicked(item: AddressBookUserDb) {
        val guid = App.getAccessManager().findGuidBy(item.address)
        if (MonkeyTest.isTurnedOn()) {
            MonkeyTest.startIfNeed()
        } else {
            launchActivity<EnterPassCodeActivity>(
                    requestCode = EnterPassCodeActivity.REQUEST_ENTER_PASS_CODE) {
                putExtra(EnterPassCodeActivity.KEY_INTENT_GUID, guid)
                putExtra(EnterPassCodeActivity.KEY_INTENT_USE_BACK_FOR_EXIT, true)
            }
        }
    }

    override fun onEditClicked(position: Int) {
        analytics.trackEvent(AnalyticEvents.StartAccountEditEvent)

        val item = adapter.getItem(position) as AddressBookUserDb
        launchActivity<EditAccountNameActivity>(REQUEST_EDIT_ACCOUNT_NAME) {
            putExtra(KEY_INTENT_ITEM_ADDRESS, item)
            putExtra(KEY_INTENT_ITEM_POSITION, position)
        }
    }

    override fun onDeleteClicked(position: Int) {
        analytics.trackEvent(AnalyticEvents.StartAccountDeleteEvent)

        val item = adapter.getItem(position) as AddressBookUserDb
        val guid = App.getAccessManager().findGuidBy(item.address)

        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle(getString(R.string.choose_account_delete_title))
        alertDialog.setMessage(getString(R.string.choose_account_delete_msg))
        if (prefsUtil.getGuidValue(guid, PrefsUtil.KEY_SKIP_BACKUP, true)) {
            alertDialog.setView(inflate(R.layout.content_delete_account_warning_layout, null))
        }
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,
                getString(R.string.choose_account_yes)) { dialog, which ->
            dialog.dismiss()

            App.getAccessManager().deleteWavesWallet(item.address)
            adapter.remove(position)

            showSuccess(R.string.choose_account_deleted, R.id.content)
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
                    val item = data?.getParcelableExtra<AddressBookUserDb>(KEY_INTENT_ITEM_ADDRESS)
                    val position = data?.getIntExtra(KEY_INTENT_ITEM_POSITION, 0)
                    item.notNull {
                        adapter.setData(position!!, it)
                        App.getAccessManager()
                                .storeWalletName(item!!.address, item.name)
                    }
                }
            }
            EnterPassCodeActivity.REQUEST_ENTER_PASS_CODE -> {
                if (resultCode == Constants.RESULT_OK) {
                    if (WavesSdk.keeper().isKeeperIntent(intent)) {
                        launchActivity<KeeperTransactionActivity>()
                    } else {
                        launchActivity<MainActivity>(clear = true)
                    }
                }
            }
            KeeperTransactionActivity.REQUEST_KEEPER_TX_ACTION -> {
                if (resultCode == Constants.RESULT_OK) {
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val keeperIntentResult = KeeperIntentHelper.parseIntentResult(intent)
        if (keeperIntentResult != null) {
            KeeperIntentHelper.exitToDAppWithResult(this, keeperIntentResult, WavesSdk.keeper())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_choose_account, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> {
                showCreateAccountDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCreateAccountDialog() {
        val optionBottomSheetFragment = OptionsBottomSheetFragment<NewAccountDialogItem>()

        val options = NewAccountDialogItem.values().toMutableList()

        optionBottomSheetFragment.configureDialog(options,
                getString(R.string.choose_account_new_account_dialog_title),
                NO_POSITION)

        optionBottomSheetFragment.onOptionSelectedListener = object : OptionsBottomSheetFragment.OnSelectedListener<NewAccountDialogItem> {
            override fun onSelected(data: NewAccountDialogItem) {
                when (data) {
                    NewAccountDialogItem.CREATE_ACCOUNT -> {
                        launchActivity<NewAccountActivity>(requestCode = WelcomeActivity.REQUEST_NEW_ACCOUNT)
                        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
                    }
                    NewAccountDialogItem.IMPORT_ACCOUNT -> {
                        launchActivity<ImportAccountActivity>(WelcomeActivity.REQUEST_IMPORT_ACC)
                        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
                    }
                }
            }
        }

        optionBottomSheetFragment.show(supportFragmentManager, optionBottomSheetFragment::class.java.simpleName)
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }

    companion object {
        const val REQUEST_EDIT_ACCOUNT_NAME = 999
        const val NO_POSITION = -1
        const val KEY_INTENT_ITEM_ADDRESS = "intent_item_address"
        const val KEY_INTENT_ITEM_POSITION = "intent_item_position"
    }
}
