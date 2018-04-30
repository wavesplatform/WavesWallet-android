package com.wavesplatform.wallet.v2.ui.welcome

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseDrawerActivity
import com.wavesplatform.wallet.v2.ui.language.change.ChangeLanguageActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.activity_welcome.*
import javax.inject.Inject


class WelcomeActivity : BaseDrawerActivity(), WelcomeView {

    @Inject
    @InjectPresenter
    lateinit var presenter: WelcomePresenter

    @ProvidePresenter
    fun providePresenter(): WelcomePresenter = presenter

    override fun configLayoutRes() = R.layout.activity_welcome


    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_change_language -> {
                launchActivity<ChangeLanguageActivity>()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_language, menu)
        return super.onCreateOptionsMenu(menu)
    }

}
