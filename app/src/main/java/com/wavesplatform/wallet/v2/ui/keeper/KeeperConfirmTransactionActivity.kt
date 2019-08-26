package com.wavesplatform.wallet.v2.ui.keeper

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import javax.inject.Inject

class KeeperConfirmTransactionActivity : BaseActivity(), KeeperConfirmTransactionView {

    @Inject
    @InjectPresenter
    lateinit var presenter: KeeperConfirmTransactionPresenter

    @ProvidePresenter
    fun providePresenter(): KeeperConfirmTransactionPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_keeper_confirm_transaction

    override fun askPassCode() = false

    var link = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        translucentStatusBar = true
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        /*setupToolbar(toolbar_view, true,
                getString(R.string.send_confirmation_toolbar_title),
                R.drawable.ic_toolbar_back_white)*/

        //link = intent.getStringExtra(KEY_INTENT_LINK)

        val callback = "myapp"
        val appName = "My B Application"
        val iconUrl = "http://icons.iconarchive.com/icons/graphicloads/100-flat/96/home-icon.png"
        val kind = "sign"
        val type = 4

    }

    companion object {
        const val KEY_INTENT_LINK = "key_intent_link"
    }
}