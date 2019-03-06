package com.wavesplatform.wallet.v2.ui.home.wallet.assets.token_burn.confirmation

import android.os.Bundle
import android.view.animation.AnimationUtils
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.request.BurnRequest
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.token_burn.TokenBurnActivity.Companion.KEY_INTENT_AMOUNT
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.token_burn.TokenBurnActivity.Companion.KEY_INTENT_ASSET_BALANCE
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.token_burn.TokenBurnActivity.Companion.KEY_INTENT_BLOCKCHAIN_FEE
import com.wavesplatform.wallet.v2.util.getScaledAmount
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.makeTextHalfBold
import com.wavesplatform.wallet.v2.util.showError
import kotlinx.android.synthetic.main.activity_token_burn_confirmation.*
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.invisiable
import pers.victor.ext.visiable
import javax.inject.Inject

class TokenBurnConfirmationActivity : BaseActivity(), TokenBurnConfirmationView {

    @Inject
    @InjectPresenter
    lateinit var presenter: TokenBurnConfirmationPresenter

    @ProvidePresenter
    fun providePresenter(): TokenBurnConfirmationPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_token_burn_confirmation

    override fun onCreate(savedInstanceState: Bundle?) {
        translucentStatusBar = true
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, true, getString(R.string.token_burn_confirmation_toolbar_title), R.drawable.ic_toolbar_back_white)

        presenter.assetBalance = intent.getParcelableExtra(KEY_INTENT_ASSET_BALANCE)
        presenter.fee = intent.getLongExtra(KEY_INTENT_BLOCKCHAIN_FEE, 0L)
        val stringAmount = intent.getStringExtra(KEY_INTENT_AMOUNT)
        presenter.amount = stringAmount.toDouble()

        text_id_value.text = presenter.assetBalance!!.assetId
        text_sum.text = "-$stringAmount ${presenter.assetBalance!!.getName()}"
        text_sum.makeTextHalfBold()
        text_type_value.text = if (presenter.assetBalance!!.reissuable == true) {
            getString(R.string.token_burn_confirmationt_reissuable)
        } else {
            getString(R.string.token_burn_confirmationt_not_reissuable)
        }
        text_description.text = presenter.assetBalance!!.getDescription()

        text_fee_value.text = "${getScaledAmount(presenter.fee, 8)} " +
                "${Constants.CUSTOM_FEE_ASSET_NAME}"

        button_confirm.click {
            presenter.burn()
            toolbar_view.invisiable()
            card_content.gone()
            card_progress.visiable()
            val rotation = AnimationUtils.loadAnimation(this@TokenBurnConfirmationActivity, R.anim.rotate)
            rotation.fillAfter = true
            image_loader.startAnimation(rotation)
        }

        text_leasing_result_value.text = getString(
                R.string.token_burn_confirmation_you_have_burned,
                stringAmount, presenter.assetBalance!!.getName()
        )
    }

    override fun onShowBurnSuccess(tx: BurnRequest?, totalBurn: Boolean) {
        completeBurnProcessing()
        relative_success.visiable()

        button_okay.click {
            if (totalBurn) {
                launchActivity<MainActivity>(clear = true)
            } else {
                setResult(Constants.RESULT_OK)
                finish()
            }
        }
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }

    override fun onShowError(errorMessageRes: String) {
        completeBurnProcessing()
        toolbar_view.visiable()
        card_content.visiable()
        showError(errorMessageRes, R.id.root)
    }

    override fun failedTokenBurnCauseSmart() {
        setResult(Constants.RESULT_SMART_ERROR)
        onBackPressed()
    }

    private fun completeBurnProcessing() {
        image_loader.clearAnimation()
        card_progress.gone()
    }

    override fun needToShowNetworkMessage() = true

    override fun onNetworkConnectionChanged(networkConnected: Boolean) {
        super.onNetworkConnectionChanged(networkConnected)
        button_confirm.isEnabled = networkConnected
    }
}
