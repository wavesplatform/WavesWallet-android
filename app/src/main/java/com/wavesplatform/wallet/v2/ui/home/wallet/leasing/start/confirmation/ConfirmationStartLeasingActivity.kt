package com.wavesplatform.wallet.v2.ui.home.wallet.leasing.start.confirmation

import android.app.Activity
import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.Constants.RESULT_SMART_ERROR
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.util.getScaledAmount
import com.wavesplatform.wallet.v2.util.makeTextHalfBold
import com.wavesplatform.wallet.v2.util.showError
import kotlinx.android.synthetic.main.activity_confirm_leasing.*
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.visiable
import javax.inject.Inject

class ConfirmationStartLeasingActivity : BaseActivity(), ConfirmationStartLeasingView {

    @Inject
    @InjectPresenter
    lateinit var presenter: ConfirmationStartLeasingPresenter

    @ProvidePresenter
    fun providePresenter(): ConfirmationStartLeasingPresenter = presenter

    override fun configLayoutRes(): Int = R.layout.activity_confirm_leasing

    override fun onCreate(savedInstanceState: Bundle?) {
        translucentStatusBar = true
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, true, getString(R.string.confirm_leasing), R.drawable.ic_toolbar_back_white)

        presenter.address = intent.getStringExtra(BUNDLE_ADDRESS)
        presenter.amount = intent.getStringExtra(BUNDLE_AMOUNT)
        presenter.recipientIsAlias = intent.getBooleanExtra(BUNDLE_RECIPIENT_IS_ALIAS, false)
        presenter.fee = intent.getLongExtra(BUNDLE_BLOCKCHAIN_COMMISSION, 0L)

        text_leasing_value.text = presenter.amount
        text_leasing_value.makeTextHalfBold()
        text_free_value.text = "${getScaledAmount(presenter.fee, 8)} ${Constants.wavesAssetInfo.name}"

        text_node_address.text = presenter.address

        text_leasing_result_value.text = getString(R.string.confirm_leasing_result_value, presenter.amount, Constants.wavesAssetInfo.name)

        button_confirm.click {
            supportActionBar?.setDisplayShowTitleEnabled(false)
            supportActionBar?.setHomeButtonEnabled(false)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)

            card_leasing_preview_info.gone()

            card_progress.visiable()

            image_loader.show()

            presenter.startLeasing()
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
        image_loader.hide()
        card_progress.gone()
        card_success.visiable()
    }

    override fun failedStartLeasing(message: String?) {
        image_loader.hide()
        card_progress.gone()
        card_leasing_preview_info.visiable()

        message?.let {
            showError(message, R.id.root)
        }
    }

    override fun onDestroy() {
        image_loader.hide()
        super.onDestroy()
    }

    override fun failedStartLeasingCauseSmart() {
        setResult(RESULT_SMART_ERROR)
        onBackPressed()
    }

    override fun needToShowNetworkMessage() = true

    override fun onNetworkConnectionChanged(networkConnected: Boolean) {
        super.onNetworkConnectionChanged(networkConnected)
        button_confirm.isEnabled = networkConnected
    }

    companion object {
        var BUNDLE_ADDRESS = "address"
        var BUNDLE_AMOUNT = "amount"
        var BUNDLE_RECIPIENT_IS_ALIAS = "recipient_is_alias"
        var BUNDLE_BLOCKCHAIN_COMMISSION = "blockchain_commission"
    }
}
