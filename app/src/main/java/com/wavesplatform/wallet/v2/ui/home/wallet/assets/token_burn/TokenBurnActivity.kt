package com.wavesplatform.wallet.v2.ui.home.wallet.assets.token_burn

import android.content.Intent
import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.jakewharton.rxbinding2.widget.RxTextView
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.token_burn.confirmation.TokenBurnConfirmationActivity
import com.wavesplatform.wallet.v2.util.*
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_token_burn.*
import kotlinx.android.synthetic.main.view_commission.*
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.isNetworkConnected
import pers.victor.ext.visiable
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TokenBurnActivity : BaseActivity(), TokenBurnView {

    @Inject
    @InjectPresenter
    lateinit var presenter: TokenBurnPresenter

    @ProvidePresenter
    fun providePresenter(): TokenBurnPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_token_burn

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        setNavigationBarColor(R.color.basic50)
        setupToolbar(toolbar_view, true, getString(R.string.token_burn_toolbar_title), R.drawable.ic_toolbar_back_black)

        presenter.assetBalance = intent.getParcelableExtra(
                KEY_INTENT_ASSET_BALANCE)

        presenter.loadWavesBalance()

        image_asset_icon.setAsset(presenter.assetBalance)
        text_asset_name.text = presenter.assetBalance.getName()
        text_asset_value.text = presenter.assetBalance.getDisplayAvailableBalance()

        text_use_total_balance.click {
            edit_amount.setText(presenter.assetBalance.getDisplayAvailableBalance().clearBalance())
        }

        edit_amount.applyFilterStartWithDot()

        eventSubscriptions.add(RxTextView.textChanges(edit_amount)
                .skipInitialValue()
                .map(CharSequence::toString)
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map {
                    if (it.isNotEmpty()) {
                        presenter.quantityValidation = true
                        horizontal_amount_suggestion.gone()
                        text_quantity_error.text = ""
                        text_quantity_error.gone()
                    } else {
                        presenter.quantityValidation = false
                        horizontal_amount_suggestion.visiable()
                        text_quantity_error.text = getString(R.string.token_burn_validation_is_required_error)
                        text_quantity_error.visiable()
                    }
                    return@map it
                }
                .filter {
                    it.isNotEmpty()
                }
                .map {
                    if (it.toDouble() != 0.0) {
                        val isValid = it.toBigDecimal() <= presenter.assetBalance.getDisplayAvailableBalance().clearBalance().toBigDecimal() && it.toBigDecimal() != BigDecimal.ZERO
                        presenter.quantityValidation = isValid

                        if (isValid) {
                            text_quantity_error.text = ""
                            text_quantity_error.gone()
                        } else {
                            text_quantity_error.text = getString(R.string.token_burn_validation_amount_insufficient_error)
                            text_quantity_error.visiable()
                        }

                        makeButtonEnableIfValid()
                        return@map Pair(isValid, it)
                    } else {
                        presenter.quantityValidation = false
                        return@map Pair(false, it)
                    }
                }
                .map {
                    if (it.first) {
                        presenter.wavesBalance.getAvailableBalance().notNull { wavesBalance ->
                            if (wavesBalance < presenter.fee) {
                                presenter.quantityValidation = false
                                linear_fees_error.visiable()
                            } else {
                                presenter.quantityValidation = true
                                linear_fees_error.gone()
                            }
                        }
                    }
                }
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ isValid ->
                    makeButtonEnableIfValid()
                }, {
                    it.printStackTrace()
                }))

        button_continue.click {
            launchActivity<TokenBurnConfirmationActivity>(REQUEST_BURN_CONFIRM) {
                putExtra(KEY_INTENT_ASSET_BALANCE, presenter.assetBalance)
                putExtra(KEY_INTENT_BLOCKCHAIN_FEE, presenter.fee)
                putExtra(KEY_INTENT_AMOUNT, edit_amount.text.toString())
            }
        }

        presenter.loadCommission(presenter.assetBalance.assetId)
    }

    override fun needToShowNetworkMessage() = true

    override fun onNetworkConnectionChanged(networkConnected: Boolean) {
        super.onNetworkConnectionChanged(networkConnected)
        if (networkConnected) {
            // enable quick action tab
            button_continue.isEnabled = presenter.isAllFieldsValid()
        } else {
            // disable quick action tab
            button_continue.isEnabled = false
        }
    }

    private fun makeButtonEnableIfValid() {
        button_continue.isEnabled = presenter.isAllFieldsValid() && isNetworkConnected()
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_BURN_CONFIRM -> {
                when (resultCode) {
                    Constants.RESULT_OK -> {
                        finish()
                    }
                    Constants.RESULT_SMART_ERROR -> {
                        showAlertAboutScriptedAccount()
                    }
                }
            }
        }
    }

    override fun showCommissionLoading() {
        progress_bar_fee_transaction.show()
        text_fee_transaction.gone()
        button_continue.isEnabled = false
    }

    override fun showCommissionSuccess(unscaledAmount: Long) {
        text_fee_transaction.text = MoneyUtil.getScaledText(unscaledAmount, 8)
        progress_bar_fee_transaction.hide()
        text_fee_transaction.visiable()
        makeButtonEnableIfValid()
    }

    override fun showCommissionError() {
        text_fee_transaction.text = "-"
        showError(R.string.common_error_commission_receiving, R.id.root)
        progress_bar_fee_transaction.hide()
        text_fee_transaction.visiable()
        makeButtonEnableIfValid()
    }

    override fun onDestroy() {
        progress_bar_fee_transaction.hide()
        super.onDestroy()
    }

    companion object {
        const val KEY_INTENT_ASSET_BALANCE = "asset_balance"
        const val KEY_INTENT_AMOUNT = "amount"
        const val KEY_INTENT_BLOCKCHAIN_FEE = "blockchain_fee"
        const val REQUEST_BURN_CONFIRM = 10001
    }
}
