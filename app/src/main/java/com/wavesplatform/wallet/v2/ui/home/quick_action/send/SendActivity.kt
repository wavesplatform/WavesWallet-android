package com.wavesplatform.wallet.v2.ui.home.quick_action.send

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.google.zxing.integration.android.IntentIntegrator
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressTestObject
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.confirmation.SendConfirmationActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.leasing.start.StartLeasingActivity
import com.wavesplatform.wallet.v2.ui.import_account.ImportAccountActivity
import com.wavesplatform.wallet.v2.ui.qr_scanner.QrCodeScannerActivity
import com.wavesplatform.wallet.v2.ui.your_assets.YourAssetsActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.activity_send.*
import pers.victor.ext.*
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
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, getString(R.string.send_toolbar_title), R.drawable.ic_toolbar_back_black)
        checkAddressFieldAndSetAction()

        text_choose_from_address.click {
            launchActivity<AddressBookActivity>(requestCode = StartLeasingActivity.REQUEST_CHOOSE_ADDRESS) {
                putExtra(AddressBookActivity.BUNDLE_SCREEN_TYPE, AddressBookActivity.AddressBookScreenType.CHOOSE.type)
            }
        }

        card_asset.click {
            launchActivity<YourAssetsActivity>(requestCode = REQUEST_YOUR_ASSETS)
        }

        edit_address.addTextChangedListener {
            on({ s, start, before, count ->
                checkAddressFieldAndSetAction()
            })
        }

        edit_amount.addTextChangedListener {
            on({ s, start, before, count ->
                if (edit_amount.text.isNotEmpty()){
                    horizontal_amount_suggestion.gone()
                }else{
                    horizontal_amount_suggestion.visiable()
                }
            })
        }

        image_view_recipient_action.click {
            if (it.tag == R.drawable.ic_deladdress_24_error_400){
                edit_address.text = null
            }else if(it.tag == R.drawable.ic_qrcode_24_basic_500){
                launchActivity<QrCodeScannerActivity> { }
            }
        }

        button_continue.click {
            launchActivity<SendConfirmationActivity> {  }
        }


    }

    private fun checkAddressFieldAndSetAction() {
        if (edit_address.text.isNotEmpty()) {
            image_view_recipient_action.setImageResource(R.drawable.ic_deladdress_24_error_400)
            image_view_recipient_action.tag = R.drawable.ic_deladdress_24_error_400
            horizontal_recipient_suggestion.gone()
            presenter.selectedAsset.notNull {
                if (it.isFlatMoney){
                    relative_gateway_fee.gone()
                    relative_fiat_fee.visiable()
                }else{
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
            ImportAccountActivity.REQUEST_SCAN_QR_CODE -> {
                val result = IntentIntegrator.parseActivityResult(resultCode, data)

                if (result.contents == null) {
                    Log.d("MainActivity", "Cancelled scan")
                } else {
                    Log.d("MainActivity", "Scanned")
                    Toast.makeText(this, "Scanned: " + result.contents, Toast.LENGTH_LONG).show()
                    // TODO: Change to real scanned address
                }
            }
            StartLeasingActivity.REQUEST_CHOOSE_ADDRESS -> {
                if (resultCode == Activity.RESULT_OK) {
                    val addressTestObject = data?.getParcelableExtra<AddressTestObject>(AddressBookActivity.BUNDLE_ADDRESS_ITEM)
                    edit_address.setText(addressTestObject?.address)
                }
            }
            REQUEST_YOUR_ASSETS -> {
                if (resultCode == Activity.RESULT_OK) {
                    val asset = data?.getParcelableExtra<AssetBalance>(YourAssetsActivity.BUNDLE_ASSET_ITEM)
                    asset.notNull {
                        presenter.selectedAsset = asset
                        checkAddressFieldAndSetAction()
                        relative_chosen_coin.visiable()
                        text_asset_hint.gone()

                        image_asset_icon.isOval = true
                        image_asset_icon.setAsset(it)

                        text_asset_name.text = it.getName()

                        text_asset_value.text = it.getDisplayBalance()
                        if (it.isFavorite){
                            image_asset_is_favourite.visiable()
                        }else{
                            image_asset_is_favourite.gone()
                        }
                    }
                }
            }
        }
    }

}
