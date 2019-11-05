/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.leasing.start

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.AppCompatTextView
import com.google.zxing.integration.android.IntentIntegrator
import com.jakewharton.rxbinding3.widget.textChanges
import com.wavesplatform.sdk.model.response.node.transaction.AliasTransactionResponse
import com.wavesplatform.sdk.utils.*
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.analytics.AnalyticEvents
import com.wavesplatform.wallet.v2.data.analytics.analytics
import com.wavesplatform.wallet.v2.data.model.db.userdb.AddressBookUserDb
import com.wavesplatform.wallet.v2.data.rules.AliasRule
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.leasing.start.confirmation.ConfirmationStartLeasingActivity
import com.wavesplatform.wallet.v2.util.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_start_leasing.*
import kotlinx.android.synthetic.main.content_commission.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import pers.victor.ext.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class StartLeasingActivity : BaseActivity(), StartLeasingView {

    @Inject
    @InjectPresenter
    lateinit var presenter: StartLeasingPresenter

    @ProvidePresenter
    fun providePresenter(): StartLeasingPresenter = presenter

    override fun configLayoutRes(): Int = R.layout.activity_start_leasing

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        setNavigationBarColor(R.color.basic50)
        setupToolbar(toolbar_view, true, getString(R.string.start_leasing_toolbar), R.drawable.ic_toolbar_back_black)

        presenter.wavesAssetBalance = intent.getLongExtra(BUNDLE_WAVES, 0L)

        text_choose_from_address.click {
            launchActivity<AddressBookActivity>(requestCode = REQUEST_CHOOSE_ADDRESS) {
                putExtra(AddressBookActivity.BUNDLE_SCREEN_TYPE, AddressBookActivity.AddressBookScreenType.CHOOSE.type)
            }
        }

        image_view_generator_action.click {
            if (edit_address.tag == R.drawable.ic_deladdress_24_error_400) {
                edit_address.setText("")
            } else if (edit_address.tag == R.drawable.ic_qrcode_24_basic_500) {
                launchQrCodeScanner()
            }
        }

        button_continue.click {
            analytics.trackEvent(AnalyticEvents.LeasingSendTapEvent)
            launchActivity<ConfirmationStartLeasingActivity>(REQUEST_LEASING_CONFIRMATION) {
                putExtra(ConfirmationStartLeasingActivity.BUNDLE_ADDRESS, edit_address.text.toString())
                putExtra(ConfirmationStartLeasingActivity.BUNDLE_AMOUNT, edit_amount.text.toString())
                putExtra(ConfirmationStartLeasingActivity.BUNDLE_RECIPIENT_IS_ALIAS, presenter.recipientIsAlias)
                putExtra(ConfirmationStartLeasingActivity.BUNDLE_BLOCKCHAIN_COMMISSION, presenter.fee)
            }
        }

        edit_amount.filters = arrayOf(filterStartWithDot, DecimalDigitsInputFilter(
                WavesConstants.WAVES_ASSET_INFO.getMaxDigitsBeforeZero(),
                WavesConstants.WAVES_ASSET_INFO.precision,
                Double.MAX_VALUE))

        eventSubscriptions.add(edit_address.textChanges()
                .skipInitialValue()
                .map(CharSequence::toString)
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map {
                    if (it.isNotEmpty()) {
                        text_address_error.text = ""
                        text_address_error.gone()
                        image_view_generator_action.setImageResource(R.drawable.ic_deladdress_24_error_400)
                        edit_address.tag = R.drawable.ic_deladdress_24_error_400
                    } else {
                        presenter.nodeAddressValidation = false
                        text_address_error.text = getString(R.string.start_leasing_validation_is_required_error)
                        text_address_error.visiable()
                        image_view_generator_action.setImageResource(R.drawable.ic_qrcode_24_basic_500)
                        edit_address.tag = R.drawable.ic_qrcode_24_basic_500
                    }
                    makeButtonEnableIfValid()
                    return@map it
                }
                .filter {
                    it.isNotEmpty()
                }
                .map {
                    val isValid = it.isValidWavesAddress() && it != App.getAccessManager().getWallet()?.address
                    presenter.nodeAddressValidation = isValid
                    if (isValid) {
                        text_address_error.text = ""
                        text_address_error.gone()
                    } else {
                        text_address_error.text = getString(R.string.start_leasing_validation_address_is_invalid_error)
                        text_address_error.visiable()
                    }
                    presenter.recipientIsAlias = false
                    makeButtonEnableIfValid()
                    return@map Pair(isValid, it)
                }
                .filter { !it.first }
                .observeOn(Schedulers.io())
                .flatMap {
                    if (it.second.matches(Regex(AliasRule.ALIAS_REGEX))) {
                        return@flatMap presenter.dataServiceManager.loadAlias(it.second)
                                .observeOn(AndroidSchedulers.mainThread())
                                .map {
                                    if (!it.own) {
                                        presenter.recipientIsAlias = true
                                        presenter.nodeAddressValidation = true
                                        text_address_error.text = ""
                                        text_address_error.gone()
                                    } else {
                                        presenter.recipientIsAlias = false
                                        presenter.nodeAddressValidation = false
                                        text_address_error.text = getString(R.string.start_leasing_validation_address_is_invalid_error)
                                        text_address_error.visiable()
                                    }
                                    return@map it
                                }
                                .doOnError {
                                    presenter.recipientIsAlias = false
                                    presenter.nodeAddressValidation = false
                                    text_address_error.text = getString(R.string.start_leasing_validation_address_is_invalid_error)
                                    text_address_error.visiable()
                                }
                                .onErrorResumeNext(Observable.empty())
                    } else {
                        presenter.recipientIsAlias = false
                        presenter.nodeAddressValidation = false
                        text_address_error.text = getString(R.string.start_leasing_validation_address_is_invalid_error)
                        text_address_error.visiable()
                        return@flatMap Observable.empty<AliasTransactionResponse>()
                    }
                }
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ isValid ->
                    makeButtonEnableIfValid()
                }, {
                    it.printStackTrace()
                }))

        eventSubscriptions.add(edit_amount.textChanges()
                .skipInitialValue()
                .map(CharSequence::toString)
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map {
                    if (it.isNotEmpty()) {
                        text_amount_error.text = ""
                        text_amount_error.gone()
                    } else {
                        text_amount_error.text = getString(R.string.start_leasing_validation_is_required_error)
                        text_amount_error.visiable()
                    }
                    return@map it
                }
                .filter {
                    it.isNotEmpty()
                }
                .map {
                    if (it.toDouble() != 0.0) {
                        val feeValue = MoneyUtil.getScaledText(presenter.fee, WavesConstants.WAVES_ASSET_INFO).toBigDecimal()
                        val currentValueWithFee = it.toBigDecimal() + feeValue
                        val isValid = currentValueWithFee <= MoneyUtil.getScaledText(presenter.wavesAssetBalance, WavesConstants.WAVES_ASSET_INFO).clearBalance().toBigDecimal() && currentValueWithFee > feeValue
                        presenter.amountValidation = isValid

                        if (isValid) {
                            text_amount_error.text = ""
                            text_amount_error.gone()
                        } else {
                            text_amount_error.text = getString(R.string.start_leasing_validation_amount_insufficient_error)
                            text_amount_error.visiable()
                        }
                        makeButtonEnableIfValid()
                        return@map Pair(isValid, it)
                    } else {
                        presenter.amountValidation = false
                        return@map Pair(false, it)
                    }
                }
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ isValid ->
                    makeButtonEnableIfValid()
                }, {
                    it.printStackTrace()
                }))

        presenter.wavesAssetBalance.notNull {
            WavesConstants.WAVES_ASSET_INFO
            text_asset_value.text = MoneyUtil.getScaledText(it, WavesConstants.WAVES_ASSET_INFO)
            image_asset_icon.setAsset(WavesConstants.WAVES_ASSET_INFO)

            presenter.loadCommission(it)
        }
    }

    private fun makeButtonEnableIfValid() {
        val valid = presenter.isAllFieldsValid() && isNetworkConnected()
        button_continue.isEnabled = valid
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_SCAN_QR_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val result = IntentIntegrator.parseActivityResult(resultCode, data)
                    val address = result.contents.replace(WAVES_PREFIX, "")
                    if (address.isNotEmpty()) {
                        edit_address.setText(address)
                    } else {
                        showError(R.string.start_leasing_validation_address_is_invalid_error, R.id.root_view)
                    }
                }
            }
            REQUEST_CHOOSE_ADDRESS -> {
                if (resultCode == Activity.RESULT_OK) {
                    val addressTestObject = data?.getParcelableExtra<AddressBookUserDb>(AddressBookActivity.BUNDLE_ADDRESS_ITEM)
                    addressTestObject?.address.notNull {
                        edit_address.setText(it)
                        edit_address.setSelection(it.length)
                    }
                }
            }
            REQUEST_LEASING_CONFIRMATION -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        onBackPressed()
                    }
                    com.wavesplatform.wallet.v2.data.Constants.RESULT_SMART_ERROR -> {
                        showAlertAboutScriptedAccount()
                    }
                }
            }
        }
    }

    override fun afterSuccessLoadWavesBalance(waves: Long) {
        linear_quick_balance.children.forEach { children ->
            val quickBalanceView = children as AppCompatTextView
            when (quickBalanceView.tag) {
                TOTAL_BALANCE -> {
                    quickBalanceView.click {
                        val balance = if (waves < presenter.fee) {
                            0
                        } else {
                            waves.minus(presenter.fee)
                        }
                        edit_amount.setText(MoneyUtil.getScaledText(balance, WavesConstants.WAVES_ASSET_INFO).clearBalance().toBigDecimal().toString())
                        edit_amount.setSelection(edit_amount.text.length)
                    }
                }
                else -> {
                    val percentBalance = (waves.times((quickBalanceView.tag.toString().toDouble().div(100)))).toLong()
                    quickBalanceView.click {
                        edit_amount.setText(MoneyUtil.getScaledText(percentBalance, WavesConstants.WAVES_ASSET_INFO).clearBalance().toBigDecimal().toString())
                        edit_amount.setSelection(edit_amount.text.length)
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }

    override fun needToShowNetworkMessage() = true

    override fun onNetworkConnectionChanged(networkConnected: Boolean) {
        super.onNetworkConnectionChanged(networkConnected)
        button_continue.isEnabled = presenter.isAllFieldsValid() && networkConnected
    }

    override fun showCommissionLoading() {
        progress_bar_fee_transaction.show()
        text_fee_transaction.gone()
        button_continue.isEnabled = false
    }

    override fun showCommissionSuccess(unscaledAmount: Long) {
        text_fee_transaction.text = MoneyUtil.getScaledText(unscaledAmount, 8).stripZeros()
        progress_bar_fee_transaction.hide()
        text_fee_transaction.visiable()
        makeButtonEnableIfValid()
    }

    override fun showCommissionError() {
        text_fee_transaction.text = "-"
        showError(R.string.common_error_commission_receiving, R.id.root_view)
        progress_bar_fee_transaction.hide()
        text_fee_transaction.visiable()
        makeButtonEnableIfValid()
    }

    override fun onDestroy() {
        progress_bar_fee_transaction.hide()
        super.onDestroy()
    }

    companion object {
        var REQUEST_CHOOSE_ADDRESS = 57
        var REQUEST_LEASING_CONFIRMATION = 59
        var REQUEST_CANCEL_LEASING_CONFIRMATION = 60
        var BUNDLE_WAVES = "waves_balance"
        var TOTAL_BALANCE = "100"
    }
}
