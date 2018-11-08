package com.wavesplatform.wallet.v2.ui.home.wallet.assets.token_burn

import android.os.Bundle
import android.support.v4.content.ContextCompat
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.token_burn.confirmation.TokenBurnConfirmationActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.activity_token_burn.*
import pers.victor.ext.addTextChangedListener
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.visiable
import javax.inject.Inject


class TokenBurnActivity : BaseActivity(), TokenBurnView {

    @Inject
    @InjectPresenter
    lateinit var presenter: TokenBurnPresenter

    @ProvidePresenter
    fun providePresenter(): TokenBurnPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_token_burn


    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.basic50)
        setupToolbar(toolbar_view, true, getString(R.string.token_burn_toolbar_title), R.drawable.ic_toolbar_back_black)

        val assetBalance = intent.getParcelableExtra<AssetBalance>(
                KEY_INTENT_ASSET_BALANCE)

        image_asset_icon.isOval = true
        image_asset_icon.setAsset(assetBalance)
        text_asset_name.text = assetBalance.getName()
        text_asset_value.text = assetBalance.getDisplayAvailableBalance()

        text_use_total_balance.click {
            edit_amount.setText(assetBalance.getDisplayAvailableBalance()
                    .replace(",", ""))
        }

        button_continue.isEnabled = true


        edit_amount.addTextChangedListener {
            on { s, start, before, count ->
                if (edit_amount.text.isNotEmpty()) {
                    horizontal_amount_suggestion.gone()
                    button_continue.isEnabled = true
                } else {
                    horizontal_amount_suggestion.visiable()
                    button_continue.isEnabled = false
                }
            }
        }

        button_continue.click {
            launchActivity<TokenBurnConfirmationActivity> {
                putExtra(KEY_INTENT_ASSET_BALANCE, assetBalance)
                putExtra(KEY_INTENT_AMOUNT, edit_amount.text.toString().toDouble())
            }
        }
    }

    companion object {
        const val KEY_INTENT_ASSET_BALANCE = "asset_balance"
        const val KEY_INTENT_AMOUNT = "amount"

    }
}
