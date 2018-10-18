package com.wavesplatform.wallet.v2.ui.home.wallet.leasing.start

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.AppCompatTextView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import com.jakewharton.rxbinding2.widget.RxTextView
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.Alias
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.rules.AliasRule
import com.wavesplatform.wallet.v2.ui.auth.qr_scanner.QrCodeScannerActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser
import com.wavesplatform.wallet.v2.ui.home.wallet.leasing.confirmation.ConfirmationLeasingActivity
import com.wavesplatform.wallet.v2.util.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_start_leasing.*
import pers.victor.ext.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class StartLeasingActivity : BaseActivity(), StartLeasingView {

    @Inject
    @InjectPresenter
    lateinit var presenter: StartLeasingPresenter

    @ProvidePresenter
    fun providePresenter(): StartLeasingPresenter = presenter

    companion object {
        var REQUEST_CHOOSE_ADDRESS = 57
        var REQUEST_LEASEING_CONFIRMATION = 59
        var REQUEST_SCAN_QR_CODE = 52
        var BUNDLE_WAVES = "waves"
        var BUNDLE_AVAILABLE = "available_balance"
        var TOTAL_BALANCE = "100"
    }

    override fun configLayoutRes(): Int = R.layout.activity_start_leasing

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.basic50)
        setupToolbar(toolbar_view, true, getString(R.string.start_leasing_toolbar), R.drawable.ic_toolbar_back_black)


        presenter.wavesAsset = intent.getParcelableExtra(BUNDLE_WAVES)
        presenter.availableBalance = intent.getLongExtra(BUNDLE_AVAILABLE, 0L)

        text_choose_from_address.click {
            launchActivity<AddressBookActivity>(requestCode = REQUEST_CHOOSE_ADDRESS) {
                putExtra(AddressBookActivity.BUNDLE_SCREEN_TYPE, AddressBookActivity.AddressBookScreenType.CHOOSE.type)
            }
        }

        image_view_recipient_action.click {
            IntentIntegrator(this).setRequestCode(REQUEST_SCAN_QR_CODE)
                    .setOrientationLocked(true)
                    .setBeepEnabled(false)
                    .setCaptureActivity(QrCodeScannerActivity::class.java)
                    .initiateScan()
        }

        button_continue.click {
            launchActivity<ConfirmationLeasingActivity>(REQUEST_LEASEING_CONFIRMATION) {
                putExtra(ConfirmationLeasingActivity.BUNDLE_ADDRESS, edit_address.text.toString())
                putExtra(ConfirmationLeasingActivity.BUNDLE_AMOUNT, edit_amount.text.toString())
                putExtra(ConfirmationLeasingActivity.BUNDLE_RECIPIENT_IS_ALIAS, presenter.recipientIsAlias)
            }
        }

        eventSubscriptions.add(RxTextView.textChanges(edit_address)
                .skipInitialValue()
                .map(CharSequence::toString)
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map {
                    if (it.isNotEmpty()) {
                        linear_address_suggestions.gone()
                        text_address_error.text = ""
                        text_address_error.gone()
                    } else {
                        linear_address_suggestions.visiable()
                        text_address_error.text = getString(R.string.start_leasing_validation_is_required_error)
                        text_address_error.visiable()
                    }
                    makeButtonEnableIfValid()
                    return@map it
                }
                .filter {
                    it.isNotEmpty()
                }
                .map {
                    val isValid = it.isValidAddress() && it != App.getAccessManager().getWallet()?.address
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
                        return@flatMap presenter.apiDataManager.loadAlias(it.second)
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
                        return@flatMap Observable.empty<Alias>()
                    }
                }
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ isValid ->
                    makeButtonEnableIfValid()
                }, {
                    it.printStackTrace()
                }))


        eventSubscriptions.add(RxTextView.textChanges(edit_amount)
                .skipInitialValue()
                .map(CharSequence::toString)
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map {
                    if (it.isNotEmpty()) {
                        linear_amount_suggestions.gone()
                        text_amount_error.text = ""
                        text_amount_error.gone()
                    } else {
                        linear_amount_suggestions.visiable()
                        text_amount_error.text = getString(R.string.start_leasing_validation_is_required_error)
                        text_amount_error.visiable()
                    }
                    makeButtonEnableIfValid()
                    return@map it
                }
                .filter {
                    it.isNotEmpty()
                }
                .map {
                    val feeValue = MoneyUtil.getScaledText(Constants.WAVES_FEE, presenter.wavesAsset).toBigDecimal()
                    val currentValueWithFee = it.toBigDecimal() + feeValue
                    val isValid = currentValueWithFee <= MoneyUtil.getScaledText(presenter.availableBalance, presenter.wavesAsset).toBigDecimal() && currentValueWithFee > feeValue
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
                }
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ isValid ->
                    makeButtonEnableIfValid()
                }, {
                    it.printStackTrace()
                }))

        presenter.wavesAsset.notNull {
            afterSuccessLoadWavesBalance(it, presenter.availableBalance)
        }
    }


    fun makeButtonEnableIfValid() {
        button_continue.isEnabled = presenter.isAllFieldsValid()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_SCAN_QR_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val result = IntentIntegrator.parseActivityResult(resultCode, data)
                    val address = result.contents
                    if (!address.isEmpty()) {
                        edit_address.setText(address)
                    } else {
                        showError(R.string.start_leasing_validation_address_is_invalid_error, R.id.root_view)
                    }
                }
            }
            REQUEST_CHOOSE_ADDRESS -> {
                if (resultCode == Activity.RESULT_OK) {
                    val addressTestObject = data?.getParcelableExtra<AddressBookUser>(AddressBookActivity.BUNDLE_ADDRESS_ITEM)
                    addressTestObject?.address.notNull {
                        edit_address.setText(it)
                        edit_address.setSelection(it.length)
                    }
                }
            }
            REQUEST_LEASEING_CONFIRMATION -> {
                if (resultCode == Activity.RESULT_OK) {
                    finish()
                }
            }
        }
    }

    override fun afterSuccessLoadWavesBalance(waves: AssetBalance, availableBalance: Long) {
        text_asset_value.text = MoneyUtil.getScaledText(availableBalance, waves)

        linear_quick_balance.children.forEach { children ->
            val quickBalanceView = children as AppCompatTextView
            when (quickBalanceView.tag) {
                TOTAL_BALANCE -> {
                    quickBalanceView.click {
                        edit_amount.setText((MoneyUtil.getScaledText(availableBalance, waves).toBigDecimal() - MoneyUtil.getScaledText(Constants.WAVES_FEE, presenter.wavesAsset).toBigDecimal()).toString())
                        edit_amount.setSelection(edit_amount.text.length)
                    }
                }
                else -> {
                    val percentBalance = (availableBalance * (quickBalanceView.tag.toString().toDouble().div(100))).toLong()
                    quickBalanceView.text = MoneyUtil.getScaledText(percentBalance, waves)
                    quickBalanceView.click {
                        edit_amount.setText(MoneyUtil.getScaledText(percentBalance, waves))
                        edit_amount.setSelection(edit_amount.text.length)
                    }
                }
            }
        }
    }

}
