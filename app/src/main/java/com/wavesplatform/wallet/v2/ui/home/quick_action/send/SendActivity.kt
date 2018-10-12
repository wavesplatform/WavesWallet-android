package com.wavesplatform.wallet.v2.ui.home.quick_action.send

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.text.TextUtils
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.google.zxing.integration.android.IntentIntegrator
import com.jakewharton.rxbinding2.widget.RxTextView
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.ui.assets.PaymentConfirmationDetails
import com.wavesplatform.wallet.v1.util.ViewUtils
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.coinomat.XRate
import com.wavesplatform.wallet.v2.ui.auth.import_account.scan.ScanSeedFragment
import com.wavesplatform.wallet.v2.ui.auth.qr_scanner.QrCodeScannerActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.confirmation.SendConfirmationActivity
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.confirmation.SendConfirmationActivity.Companion.KEY_INTENT_SELECTED_ADDRESS
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.confirmation.SendConfirmationActivity.Companion.KEY_INTENT_SELECTED_AMOUNT
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.confirmation.SendConfirmationActivity.Companion.KEY_INTENT_SELECTED_ASSET
import com.wavesplatform.wallet.v2.ui.home.wallet.leasing.start.StartLeasingActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.your_assets.YourAssetsActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.notNull
import com.wavesplatform.wallet.v2.util.showError
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_send.*
import pers.victor.ext.addTextChangedListener
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.visiable
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class SendActivity : BaseActivity(), SendView {

    @Inject
    @InjectPresenter
    lateinit var presenter: SendPresenter

    @ProvidePresenter
    fun providePresenter(): SendPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_send

    companion object {
        var REQUEST_YOUR_ASSETS = 43
    }


    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.basic50)
        setupToolbar(toolbar_view, true, getString(R.string.send_toolbar_title), R.drawable.ic_toolbar_back_black)
        checkAddressFieldAndSetAction(edit_address.text.toString())

        text_choose_from_address.click {
            launchActivity<AddressBookActivity>(requestCode = StartLeasingActivity.REQUEST_CHOOSE_ADDRESS) {
                putExtra(AddressBookActivity.BUNDLE_SCREEN_TYPE, AddressBookActivity.AddressBookScreenType.CHOOSE.type)
            }
        }

        if (intent.hasExtra(YourAssetsActivity.BUNDLE_ASSET_ITEM)) {
            setAsset(intent.getParcelableExtra(YourAssetsActivity.BUNDLE_ASSET_ITEM))
            assetChangeEnable(false)
        } else {
            assetChangeEnable(true)
        }

        eventSubscriptions.add(RxTextView.textChanges(edit_address)
                .skipInitialValue()
                .debounce(300, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    checkAddressFieldAndSetAction(it.toString())
                })

        edit_amount.addTextChangedListener {
            on { s, _, _, _ ->
                if (edit_amount.text!!.isNotEmpty()) {
                    presenter.amount = s.toString()
                }
            }
        }

        image_view_recipient_action.click {
            if (it.tag == R.drawable.ic_deladdress_24_error_400) {
                edit_address.text = null
            } else if (it.tag == R.drawable.ic_qrcode_24_basic_500) {
                launchActivity<QrCodeScannerActivity> { }
            }
        }

        button_continue.click { presenter.sendClicked() }

        text_use_total_balance.click {
            presenter.selectedAsset.notNull {
                edit_amount.setText(it.getDisplayBalance())
            }
        }
        text_leasing_0_100.click { edit_amount.setText("0.100") }
        text_leasing_0_100000.click { edit_amount.setText("0.00100000") }
        text_leasing_0_500000.click { edit_amount.setText("0.00500000") }
    }

    override fun onShowError(res: Int) {
        showError(res, R.id.root)
    }

    override fun onShowPaymentDetails(details: PaymentConfirmationDetails) {
        launchActivity<SendConfirmationActivity> {
            putExtra(KEY_INTENT_SELECTED_ASSET, presenter.selectedAsset)
            putExtra(KEY_INTENT_SELECTED_ADDRESS, presenter.address)
            putExtra(KEY_INTENT_SELECTED_AMOUNT, presenter.amount)
        }
    }

    override fun showXRate(xRate: XRate?) {
        relative_gateway_fee.visiable()
        gateway_fee.text = getString(
                R.string.send_gateway_info_gateway_fee,
                BigDecimal(xRate?.fee).toString(),
                xRate?.toTxt)
        gateway_limits.text = getString(
                R.string.send_gateway_info_gateway_limits,
                xRate!!.toTxt,
                BigDecimal(xRate.inMin!!).toString(),
                BigDecimal(xRate.inMax!!).toString())
        gateway_warning.text = getString(R.string.send_gateway_info_gateway_warning,
                xRate.toTxt)
    }

    private fun checkAddressFieldAndSetAction(address: String) {
        if (address.isNotEmpty()) {
            presenter.address = address
            image_view_recipient_action.setImageResource(R.drawable.ic_deladdress_24_error_400)
            image_view_recipient_action.tag = R.drawable.ic_deladdress_24_error_400
            horizontal_recipient_suggestion.gone()
            presenter.selectedAsset.notNull {
                if (it.isFiatMoney) {
                    relative_gateway_fee.gone()
                    relative_fiat_fee.visiable()
                } else {
                    relative_fiat_fee.gone()
                    relative_gateway_fee.visiable()
                }
            }
        } else {
            image_view_recipient_action.setImageResource(R.drawable.ic_qrcode_24_basic_500)
            image_view_recipient_action.tag = R.drawable.ic_qrcode_24_basic_500
            horizontal_recipient_suggestion.visiable()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ScanSeedFragment.REQUEST_SCAN_QR_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val result = IntentIntegrator.parseActivityResult(resultCode, data)
                    val address = result.contents
                    if (!TextUtils.isEmpty(address)) {
                        edit_address.setText(address)
                    } else {
                        showError(R.string.enter_seed_manually_validation_seed_is_invalid_error, R.id.root_view)
                    }
                }
            }

            StartLeasingActivity.REQUEST_CHOOSE_ADDRESS -> {
                if (resultCode == Activity.RESULT_OK) {
                    val addressTestObject = data?.getParcelableExtra<AddressBookUser>(AddressBookActivity.BUNDLE_ADDRESS_ITEM)
                    edit_address.setText(addressTestObject?.address)
                }
            }

            REQUEST_YOUR_ASSETS -> {
                if (resultCode == Activity.RESULT_OK) {
                    setAsset(data?.getParcelableExtra(YourAssetsActivity.BUNDLE_ASSET_ITEM))
                }
            }
        }
    }

    private fun setAsset(asset: AssetBalance?) {
        asset.notNull {
            presenter.selectedAsset = asset

            if (AssetBalance.isGateway(it.assetId!!)) {
                presenter.loadXRate(it)
            }

            checkAddressFieldAndSetAction(edit_address.text.toString())
            relative_chosen_coin.visiable()
            text_asset_hint.gone()

            image_asset_icon.isOval = true
            image_asset_icon.setAsset(it)

            text_asset_name.text = it.getName()

            text_asset_value.text = it.getDisplayBalance()
            if (it.isFavorite) {
                image_asset_is_favourite.visiable()
            } else {
                image_asset_is_favourite.gone()
            }
        }
    }

    private fun assetChangeEnable(enable: Boolean) {
        if (enable) {
            card_asset.click {
                launchActivity<YourAssetsActivity>(requestCode = REQUEST_YOUR_ASSETS)
            }
            image_change.visibility = View.VISIBLE
            ViewCompat.setElevation(card_asset, ViewUtils.convertDpToPixel(4f, this))
            asset_layout.background = null
            card_asset.setCardBackgroundColor(ContextCompat.getColor(
                    this, R.color.white))
        } else {
            card_asset.click {

            }
            image_change.visibility = View.GONE
            ViewCompat.setElevation(card_asset, 0F)
            asset_layout.background = ContextCompat.getDrawable(
                    this, R.drawable.shape_rect_bordered_accent50)
            card_asset.setCardBackgroundColor(ContextCompat.getColor(
                    this, R.color.basic50))
        }
    }
}
