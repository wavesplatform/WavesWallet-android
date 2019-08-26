package com.wavesplatform.wallet.v2.ui.keeper

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import com.wavesplatform.sdk.utils.Identicon
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.activity_keeper_transaction.*
import pers.victor.ext.click
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

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        setNavigationBarColor(R.color.basic50)

        setupToolbar(toolbar_view, true,
                getString(R.string.keeper_title_confirm_request), R.drawable.ic_toolbar_back_black)
        link = intent.getStringExtra(KEY_INTENT_LINK)

        val callback = "myapp"
        val appName = "My B Application"
        val iconUrl = "http://icons.iconarchive.com/icons/graphicloads/100-flat/96/home-icon.png"
        val kind = "sign"
        val type = 4

        val tx: BaseTransaction? = null

        val addressFrom = App.getAccessManager().getWallet()?.address ?: ""

        Glide.with(this)
                .load(Identicon().create(addressFrom))
                .apply(RequestOptions().circleCrop())
                .into(image_address_from)

        text_address_from.text = addressFrom

        Glide.with(this)
                .load(iconUrl)
                .apply(RequestOptions().circleCrop())
                .into(image_address_to)

        text_address_to.text = appName


        button_reject.click {
            finish()
        }

        button_approve.click {
            launchActivity<KeeperConfirmTransactionActivity>()
        }
    }

    companion object {
        const val KEY_INTENT_LINK = "key_intent_link"
    }
}