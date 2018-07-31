package com.wavesplatform.wallet.v2.ui.home.wallet.assets.token_burn

import android.os.Bundle
import android.view.View
import javax.inject.Inject

import com.arellomobile.mvp.presenter.InjectPresenter

import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity;

import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.IssueTransaction
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.token_burn.confirmation.TokenBurnConfirmationActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.activity_token_burn.*
import kotlinx.android.synthetic.main.wallet_asset_item.view.*
import pers.victor.ext.addTextChangedListener
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.visiable


class TokenBurnActivity : BaseActivity(), TokenBurnView {

    @Inject
    @InjectPresenter
    lateinit var presenter: TokenBurnPresenter

    @ProvidePresenter
    fun providePresenter(): TokenBurnPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_token_burn


    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, getString(R.string.token_burn_toolbar_title), R.drawable.ic_toolbar_back_black)

        image_asset_icon.isOval = true
        image_asset_icon.setAsset(AssetBalance(assetId = "Soblevo", issueTransaction = IssueTransaction(id = "Soblevo",name = "Soblevo")))


        edit_amount.addTextChangedListener {
            on({ s, start, before, count ->
                if (edit_amount.text.isNotEmpty()){
                    horizontal_amount_suggestion.gone()
                    button_continue.isEnabled = true
                }else{
                    horizontal_amount_suggestion.visiable()
                    button_continue.isEnabled = false
                }
            })
        }

        button_continue.click {
            launchActivity<TokenBurnConfirmationActivity> {  }
        }
    }

}
