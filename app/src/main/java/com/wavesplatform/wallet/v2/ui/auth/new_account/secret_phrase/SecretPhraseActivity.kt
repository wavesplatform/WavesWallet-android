package com.wavesplatform.wallet.v2.ui.auth.new_account.secret_phrase

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.create.CreatePassCodeActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.profile.backup.BackupPhraseActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.activity_secret_phrase.*
import pers.victor.ext.click
import javax.inject.Inject

class SecretPhraseActivity : BaseActivity(), SecretPhraseView {

    @Inject
    @InjectPresenter
    lateinit var presenter: SecretPhrasePresenter

    @ProvidePresenter
    fun providePresenter(): SecretPhrasePresenter = presenter

    override fun configLayoutRes() = R.layout.activity_secret_phrase

    override fun askPassCode() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view)

        button_confirm.click {
            launchActivity<BackupPhraseActivity>(options = intent.extras) {
                putExtra(NewAccountActivity.KEY_INTENT_SKIP_BACKUP, false)
            }
        }

        button_do_it_later.click {
            launchActivity<CreatePassCodeActivity>(options = intent.extras) {
                putExtra(NewAccountActivity.KEY_INTENT_SKIP_BACKUP, true)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_close -> {
                launchActivity<CreatePassCodeActivity>(options = intent.extras) {
                    putExtra(NewAccountActivity.KEY_INTENT_SKIP_BACKUP, true)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_close, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }
}
