package com.wavesplatform.wallet.v2.ui.home.wallet.leasing.confirmation

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
import kotlinx.android.synthetic.main.activity_confirm_leasing.*
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.visiable
import javax.inject.Inject

class ConfirmationLeasingActivity : BaseActivity(), ConfirmationLeasingView {

    @Inject
    @InjectPresenter
    lateinit var presenter: ConfirmationLeasingPresenter

    @ProvidePresenter
    fun providePresenter(): ConfirmationLeasingPresenter = presenter

    override fun configLayoutRes(): Int = R.layout.activity_confirm_leasing

    override fun onCreate(savedInstanceState: Bundle?) {
        translucentStatusBar = true
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    companion object {
        var BUNDLE_CANCEL_CONFIRMATION_LEASING = "cancel_confirmation_leasing"
        var BUNDLE_CANCEL_CONFIRMATION_LEASING_TX = "cancel_confirmation_leasing_tx"
        var BUNDLE_ADDRESS = "address"
        var BUNDLE_AMOUNT = "amount"
        var BUNDLE_RECIPIENT_IS_ALIAS = "recipient_is_alias"
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, true, getString(R.string.confirm_leasing), R.drawable.ic_toolbar_back_white)

        presenter.address = intent.getStringExtra(BUNDLE_ADDRESS)
        presenter.amount = intent.getStringExtra(BUNDLE_AMOUNT)
        presenter.recipientIsAlias = intent.getBooleanExtra(BUNDLE_RECIPIENT_IS_ALIAS, false)

        text_leasing_value.text = presenter.amount
        text_leasing_value.makeTextHalfBold()
        text_free_value.text = "${MoneyUtil.getScaledText(Constants.WAVES_FEE, 8).stripZeros()} ${Constants.wavesAssetInfo.name}"
        text_node_address.text = presenter.address


        presenter.startLeasing = !intent.hasExtra(
                ConfirmationLeasingActivity.BUNDLE_CANCEL_CONFIRMATION_LEASING)

        if (presenter.startLeasing) {
            text_leasing_result_value.text = getString(R.string.confirm_leasing_result_value, presenter.amount, Constants.wavesAssetInfo.name)
        } else {
            text_leasing_result_value.text = getString(R.string.confirm_cancel_leasing_result_value)
        }

        button_confirm.click {
            supportActionBar?.setDisplayShowTitleEnabled(false)
            supportActionBar?.setHomeButtonEnabled(false)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)

            card_leasing_preview_info.gone()

            card_progress.visiable()
            val rotation = AnimationUtils.loadAnimation(this, R.anim.rotate)
            rotation.fillAfter = true
            image_loader.startAnimation(rotation)

            if (presenter.startLeasing) {
                presenter.startLeasing()
            } else {
                val transactionId = intent.getStringExtra(
                        ConfirmationLeasingActivity.BUNDLE_CANCEL_CONFIRMATION_LEASING_TX)
                presenter.cancelLeasing(transactionId)
            }
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

    override fun successStartLeasing() {
        image_loader.clearAnimation()
        card_progress.gone()
        card_success.visiable()
    }

    override fun failedStartLeasing() {
        image_loader.clearAnimation()
        card_progress.gone()
        card_leasing_preview_info.visiable()
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
