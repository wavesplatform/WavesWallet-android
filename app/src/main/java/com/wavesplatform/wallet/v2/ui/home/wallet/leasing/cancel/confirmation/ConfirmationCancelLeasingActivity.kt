package com.wavesplatform.wallet.v2.ui.home.wallet.leasing.cancel.confirmation

import android.app.Activity
import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.util.makeTextHalfBold
import com.wavesplatform.wallet.v2.util.showError
import kotlinx.android.synthetic.main.activity_confirm_cancel_leasing.*
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.visiable
import javax.inject.Inject

class ConfirmationCancelLeasingActivity : BaseActivity(), ConfirmationCancelLeasingView {

    @Inject
    @InjectPresenter
    lateinit var presenter: ConfirmationCancelLeasingPresenter

    @ProvidePresenter
    fun providePresenter(): ConfirmationCancelLeasingPresenter = presenter

    override fun configLayoutRes(): Int = R.layout.activity_confirm_cancel_leasing

    override fun onCreate(savedInstanceState: Bundle?) {
        translucentStatusBar = true
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, true, getString(R.string.confirm_leasing), R.drawable.ic_toolbar_back_white)

        presenter.address = intent.getStringExtra(BUNDLE_ADDRESS)
        presenter.amount = intent.getStringExtra(BUNDLE_AMOUNT)
        presenter.transactionId = intent.getStringExtra(
                BUNDLE_CANCEL_CONFIRMATION_LEASING_TX)

        text_leasing_value.text = presenter.amount
        text_leasing_value.makeTextHalfBold()
        text_tx_value.text = presenter.transactionId

        text_leasing_result_value.text = getString(R.string.confirm_cancel_leasing_result_value)

        button_confirm.click {
            supportActionBar?.setDisplayShowTitleEnabled(false)
            supportActionBar?.setHomeButtonEnabled(false)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)

            card_leasing_preview_info.gone()

            card_progress.visiable()

            image_loader.show()

            presenter.cancelLeasing()
        }

        button_okay.click {
            setResult(Activity.RESULT_OK)
            onBackPressed()
        }

        presenter.loadCommission()
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }

    override fun successCancelLeasing() {
        image_loader.hide()
        card_progress.gone()
        card_success.visiable()
    }

    override fun failedCancelLeasing(message: String?) {
        image_loader.hide()
        card_progress.gone()
        card_leasing_preview_info.visiable()

        message?.let {
            showError(message, R.id.root)
        }
    }

    override fun failedCancelLeasingCauseSmart() {
        setResult(Constants.RESULT_SMART_ERROR)
        onBackPressed()
    }

    override fun needToShowNetworkMessage() = true

    override fun onNetworkConnectionChanged(networkConnected: Boolean) {
        super.onNetworkConnectionChanged(networkConnected)
        button_confirm.isEnabled = networkConnected
    }

    override fun showCommissionLoading() {
        button_okay.isEnabled = false
        progress_bar_fee_transaction.show()
        text_fee_value.gone()
    }

    override fun showCommissionSuccess(unscaledAmount: Long) {
        text_fee_value.text = "${MoneyUtil.getScaledText(unscaledAmount, 8)} ${Constants.wavesAssetInfo.name}"
        button_okay.isEnabled = true
        progress_bar_fee_transaction.hide()
        text_fee_value.visiable()
    }

    override fun showCommissionError() {
        text_fee_value.text = "-"
        button_okay.isEnabled = false
        progress_bar_fee_transaction.hide()
        text_fee_value.visiable()
        showError(R.string.common_error_commission_receiving, R.id.root)
    }

    override fun onDestroy() {
        image_loader.hide()
        progress_bar_fee_transaction.hide()
        super.onDestroy()
    }

    companion object {
        var REQUEST_CANCEL_LEASING_CONFIRMATION = 60
        var BUNDLE_CANCEL_CONFIRMATION_LEASING_TX = "cancel_confirmation_leasing_tx"
        var BUNDLE_ADDRESS = "address"
        var BUNDLE_AMOUNT = "amount"
    }
}
