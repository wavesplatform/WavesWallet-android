package com.wavesplatform.wallet.v2.ui.new_account.secret_phrase

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.new_account.backup_info.BackupInfoActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.notAvailable
import kotlinx.android.synthetic.main.activity_secret_phrase.*
import pers.victor.ext.click
import pyxis.uzuki.live.richutilskt.utils.toast
import javax.inject.Inject


class SecretPhraseActivity : BaseActivity(), SecretPhraseView {

    @Inject
    @InjectPresenter
    lateinit var presenter: SecretPhrasePresenter

    @ProvidePresenter
    fun providePresenter(): SecretPhrasePresenter = presenter

    override fun configLayoutRes() = R.layout.activity_secret_phrase


    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view)


        button_confirm.click {
            launchActivity<BackupInfoActivity> {  }
        }

        text_do_it_later.click {
            notAvailable()
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_close -> {
                notAvailable()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_close, menu)
        return super.onCreateOptionsMenu(menu)
    }


}
