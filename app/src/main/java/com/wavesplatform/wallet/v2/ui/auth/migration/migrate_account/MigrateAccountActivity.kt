/*
 * Created by Eduard Zaydel on 30/9/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.auth.migration.migrate_account

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.wavesplatform.sdk.WavesSdk
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.db.userdb.AddressBookUserDb
import com.wavesplatform.wallet.v2.data.model.local.MigrateAccountItem
import com.wavesplatform.wallet.v2.ui.auth.choose_account.ChooseAccountActivity.Companion.KEY_INTENT_ITEM_ADDRESS
import com.wavesplatform.wallet.v2.ui.auth.choose_account.ChooseAccountActivity.Companion.KEY_INTENT_ITEM_POSITION
import com.wavesplatform.wallet.v2.ui.auth.choose_account.ChooseAccountActivity.Companion.REQUEST_EDIT_ACCOUNT_NAME
import com.wavesplatform.wallet.v2.ui.auth.new_account.secret_phrase.SecretPhrasePresenter
import com.wavesplatform.wallet.v2.ui.auth.new_account.secret_phrase.SecretPhraseView
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.EnterPassCodeActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.ui.keeper.KeeperTransactionActivity
import com.wavesplatform.wallet.v2.util.MonkeyTest
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.activity_migrate_account.*
import kotlinx.android.synthetic.main.activity_new_level_of_security.*
import kotlinx.android.synthetic.main.activity_new_level_of_security.button_confirm
import kotlinx.android.synthetic.main.activity_new_level_of_security.toolbar_view
import pers.victor.ext.click
import javax.inject.Inject

class MigrateAccountActivity : BaseActivity(), MigrateAccountView {

    @Inject
    @InjectPresenter
    lateinit var presenter: MigrateAccountPresenter

    @Inject
    lateinit var adapter: MigrateAccountAdapter

    @ProvidePresenter
    fun providePresenter(): MigrateAccountPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_migrate_account

    override fun askPassCode() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        setNavigationBarColor(R.color.basic50)
        setupToolbar(toolbar_view)

        recycle_accounts.layoutManager = LinearLayoutManager(this)
        adapter.bindToRecyclerView(recycle_accounts)

        button_confirm.click {
            // TODO: Multi account logic here
        }

        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val type = adapter.getItemViewType(position)
            if (type == MigrateAccountAdapter.TYPE_ACCOUNT) {
                this.adapter.getItem(position)?.let { item ->
                    val account = item.data as AddressBookUserDb

                    val guid = App.getAccessManager().findGuidBy(account.address)
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
            }

        }

        presenter.getAddresses()
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }

    override fun afterSuccessGetAddress(accounts: MutableList<MigrateAccountItem>) {
        adapter.setNewData(accounts)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            EnterPassCodeActivity.REQUEST_ENTER_PASS_CODE -> {
                if (resultCode == Constants.RESULT_OK) {
                    // TODO: Multi account logic here
//                  adapter.addUnlockedAccount(item, position)
                }
            }
        }
    }

}
