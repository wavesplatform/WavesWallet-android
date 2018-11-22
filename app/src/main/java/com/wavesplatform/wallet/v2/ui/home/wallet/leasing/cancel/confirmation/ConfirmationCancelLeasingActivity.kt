package com.wavesplatform.wallet.v2.ui.home.wallet.leasing.cancel.confirmation

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import android.view.animation.AnimationUtils
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.util.makeTextHalfBold
import com.wavesplatform.wallet.v2.util.stripZeros
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

    companion object {
        var REQUEST_CANCEL_LEASING_CONFIRMATION = 60
        var BUNDLE_CANCEL_CONFIRMATION_LEASING_TX = "cancel_confirmation_leasing_tx"
        var BUNDLE_ADDRESS = "address"
        var BUNDLE_AMOUNT = "amount"
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, true, getString(R.string.confirm_leasing), R.drawable.ic_toolbar_back_white)

        presenter.address = intent.getStringExtra(BUNDLE_ADDRESS)
        presenter.amount = intent.getStringExtra(BUNDLE_AMOUNT)
        presenter.transactionId = intent.getStringExtra(
                BUNDLE_CANCEL_CONFIRMATION_LEASING_TX)

        text_leasing_value.text = presenter.amount
        text_leasing_value.makeTextHalfBold()
        text_free_value.text = "${MoneyUtil.getScaledText(Constants.WAVES_FEE, 8).stripZeros()} ${Constants.wavesAssetInfo.name}"
        text_tx_value.text = presenter.transactionId

        text_leasing_result_value.text = getString(R.string.confirm_cancel_leasing_result_value)

        button_confirm.click {
            supportActionBar?.setDisplayShowTitleEnabled(false)
            supportActionBar?.setHomeButtonEnabled(false)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)

            card_leasing_preview_info.gone()

            card_progress.visiable()
            val rotation = AnimationUtils.loadAnimation(this, R.anim.rotate)
            rotation.fillAfter = true
            image_loader.startAnimation(rotation)

            presenter.cancelLeasing()
        }

        button_okay.click {
            setResult(Activity.RESULT_OK)
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }

    override fun successCancelLeasing() {
        image_loader.clearAnimation()
        card_progress.gone()
        card_success.visiable()
    }

    override fun failedCancelLeasing() {
        image_loader.clearAnimation()
        card_progress.gone()
        card_leasing_preview_info.visiable()
    }

}
