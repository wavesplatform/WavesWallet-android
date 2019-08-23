package com.wavesplatform.wallet.v2.ui.keeper

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.Language
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.tutorial.TutorialActivity.Companion.BUNDLE_LANG
import kotlinx.android.synthetic.main.activity_choose_account.*
import javax.inject.Inject

class KeeperTransactionActivity : BaseActivity(), KeeperTransactionView {

    @Inject
    @InjectPresenter
    lateinit var presenter: KeeperTransactionPresenter

    @ProvidePresenter
    fun providePresenter(): KeeperTransactionPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_keeper_transaction

    override fun askPassCode() = false

    var link = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLanguage(Language.getLocale(intent?.getStringExtra(BUNDLE_LANG)))
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        setNavigationBarColor(R.color.basic50)

        setupToolbar(toolbar_view, true,
                getString(R.string.keeper_title_confirm_request), R.drawable.ic_toolbar_back_black)
        link = intent.getStringExtra(KEY_INTENT_LINK)
    }

    companion object {
        const val KEY_INTENT_LINK = "key_intent_link"
    }
}