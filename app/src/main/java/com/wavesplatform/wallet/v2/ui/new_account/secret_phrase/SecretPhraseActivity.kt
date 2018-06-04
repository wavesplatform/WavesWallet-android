package com.wavesplatform.wallet.v2.ui.new_account.secret_phrase

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.util.setSystemBarTheme
import kotlinx.android.synthetic.main.activity_secret_phrase.*
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
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_close -> {
                toast("skip")
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_close, menu)
        return super.onCreateOptionsMenu(menu)
    }


}
