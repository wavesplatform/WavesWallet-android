package com.wavesplatform.wallet.v2.ui.home.quick_action.send

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.AppCompatTextView
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.ethanhua.skeleton.Skeleton
import com.ethanhua.skeleton.SkeletonScreen
import com.google.zxing.integration.android.IntentIntegrator
import com.jakewharton.rxbinding2.widget.RxTextView
import com.vicpin.krealmextensions.queryFirst
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.ui.assets.PaymentConfirmationDetails
import com.wavesplatform.wallet.v1.util.AddressUtil
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v1.util.ViewUtils
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.coinomat.XRate
import com.wavesplatform.wallet.v2.ui.auth.qr_scanner.QrCodeScannerActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.confirmation.SendConfirmationActivity
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.confirmation.SendConfirmationActivity.Companion.KEY_INTENT_ATTACHMENT
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.confirmation.SendConfirmationActivity.Companion.KEY_INTENT_GATEWAY_COMMISSION
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.confirmation.SendConfirmationActivity.Companion.KEY_INTENT_MONERO_PAYMENT_ID
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.confirmation.SendConfirmationActivity.Companion.KEY_INTENT_SELECTED_AMOUNT
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.confirmation.SendConfirmationActivity.Companion.KEY_INTENT_SELECTED_ASSET
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.confirmation.SendConfirmationActivity.Companion.KEY_INTENT_SELECTED_RECIPIENT
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.confirmation.SendConfirmationActivity.Companion.KEY_INTENT_TYPE
import com.wavesplatform.wallet.v2.ui.home.wallet.leasing.start.StartLeasingActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.your_assets.YourAssetsActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.notNull
import com.wavesplatform.wallet.v2.util.showError
import com.wavesplatform.wallet.v2.util.stripZeros
import kotlinx.android.synthetic.main.activity_send.*
import pers.victor.ext.*
import java.math.BigDecimal
import java.net.URI
import javax.inject.Inject


class SendActivity : BaseActivity(), SendView {

    @Inject
    @InjectPresenter
    lateinit var presenter: SendPresenter

    private var skeletonView: SkeletonScreen? = null

    @ProvidePresenter
    fun providePresenter(): SendPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_send

    companion object {
        const val REQUEST_YOUR_ASSETS = 43
        const val REQUEST_SCAN_RECEIVE = 44
        const val REQUEST_SCAN_MONERO = 45
        const val KEY_INTENT_ASSET_DETAILS = "asset_details"
        const val KEY_INTENT_REPEAT_TRANSACTION = "repeat_transaction"
        const val KEY_INTENT_TRANSACTION_ASSET_BALANCE = "transaction_asset_balance"
        const val KEY_INTENT_TRANSACTION_AMOUNT = "transaction_amount"
        const val KEY_INTENT_TRANSACTION_ATTACHMENT = "transaction_attachment"
        const val KEY_INTENT_TRANSACTION_RECIPIENT = "transaction_recipient"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        setNavigationBarColor(R.color.basic50)
        setupToolbar(toolbar_view, true, getString(R.string.send_toolbar_title),
                R.drawable.ic_toolbar_back_black)
        checkRecipient(edit_address.text.toString())

        when {
            intent.hasExtra(KEY_INTENT_ASSET_DETAILS) -> {
                setAsset(intent.getParcelableExtra(YourAssetsActivity.BUNDLE_ASSET_ITEM))
                assetEnable(false)
            }
            intent.hasExtra(KEY_INTENT_REPEAT_TRANSACTION) -> {
                val assetBalance = intent.getParcelableExtra<AssetBalance>(
                        SendActivity.KEY_INTENT_TRANSACTION_ASSET_BALANCE)
                val amount = intent
                        .getStringExtra(SendActivity.KEY_INTENT_TRANSACTION_AMOUNT)
                val recipientAddress = intent
                        .getStringExtra(SendActivity.KEY_INTENT_TRANSACTION_RECIPIENT)
                val attachment = intent
                        .getStringExtra(SendActivity.KEY_INTENT_TRANSACTION_ATTACHMENT)
                setAsset(assetBalance)
                assetEnable(false)
                edit_address.setText(recipientAddress)
                edit_amount.setText(amount)
                presenter.attachment = attachment
                presenter.amount = amount.toFloat()
            }
            else -> assetEnable(true)
        }

        eventSubscriptions.add(RxTextView.textChanges(edit_address)
                .subscribe {
                    checkRecipient(it.toString())
                })

        edit_amount.addTextChangedListener {
            on { s, _, _, _ ->
                if (edit_amount.text!!.isNotEmpty()) {
                    presenter.amount = s.toString().toFloat()
                }
            }
        }

        image_view_recipient_action.click {
            if (it.tag == R.drawable.ic_deladdress_24_error_400) {
                edit_address.text = null
                text_recipient_error.gone()
                assetEnable(true)
                recipientEnable(true)
                amountEnable(true)
            } else if (it.tag == R.drawable.ic_qrcode_24_basic_500) {
                IntentIntegrator(this).setRequestCode(REQUEST_SCAN_RECEIVE)
                        .setOrientationLocked(true)
                        .setBeepEnabled(false)
                        .setCaptureActivity(QrCodeScannerActivity::class.java)
                        .initiateScan()
            }
        }
        image_view_monero_action.click {
            if (it.tag == R.drawable.ic_deladdress_24_error_400) {
                edit_monero_payment_id.text = null
            } else if (it.tag == R.drawable.ic_qrcode_24_basic_500) {
                IntentIntegrator(this).setRequestCode(REQUEST_SCAN_MONERO)
                        .setOrientationLocked(true)
                        .setBeepEnabled(false)
                        .setCaptureActivity(QrCodeScannerActivity::class.java)
                        .initiateScan()
            }
        }

        button_continue.click { presenter.sendClicked() }

        text_use_total_balance.click {
            if (presenter.type == SendPresenter.Type.GATEWAY) {
                presenter.selectedAsset.notNull { assetBalance ->
                    assetBalance.balance.notNull { balance ->
                        if (presenter.type == SendPresenter.Type.GATEWAY) {
                            val total = BigDecimal.valueOf(balance,
                                    assetBalance.getDecimals())
                                    .minus(presenter.gatewayCommission)
                            if (total.toFloat() > 0) {
                                edit_amount.setText(total.toString().stripZeros())
                            }
                        }
                    }
                }
            } else {
                setPercent(1.0)
            }
        }
        text_50_percent.click { setPercent(0.50) }
        text_10_percent.click { setPercent(0.10) }
        text_5_percent.click { setPercent(0.05) }

        setRecipientSuggestions()
    }

    private fun setRecipientSuggestions() {
        val addressBook = layoutInflater
                .inflate(R.layout.view_text_tag, null) as AppCompatTextView
        addressBook.text = getText(R.string.send_choose_from_address_book)
        addressBook.click {
            launchActivity<AddressBookActivity>(
                    requestCode = StartLeasingActivity.REQUEST_CHOOSE_ADDRESS) {
                putExtra(AddressBookActivity.BUNDLE_SCREEN_TYPE,
                        AddressBookActivity.AddressBookScreenType.CHOOSE.type)
            }
        }

        val parameters = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        addressBook.layoutParams = parameters

        linear_recipient_suggestion.addView(addressBook)

        val addresses = prefsUtil.getGlobalValueList(PrefsUtil.KEY_LAST_SENT_ADDRESSES)

        val parametersForAddress = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        parametersForAddress.marginStart = ViewUtils.convertDpToPixel(4F, this).toInt()
        for (address in addresses) {
            val lastRecipient = layoutInflater
                    .inflate(R.layout.view_text_tag, null) as AppCompatTextView
            val addressBookUser = queryFirst<AddressBookUser> {
                equalTo("address", address)
            }
            lastRecipient.text = addressBookUser?.name ?: address
            lastRecipient.click {
                edit_address.setText(address)
            }
            lastRecipient.layoutParams = parametersForAddress
            linear_recipient_suggestion.addView(lastRecipient)
        }
    }

    override fun onShowError(res: Int) {
        showError(res, R.id.root)
    }

    override fun onShowPaymentDetails(details: PaymentConfirmationDetails) {
        launchActivity<SendConfirmationActivity> {
            putExtra(KEY_INTENT_SELECTED_ASSET, presenter.selectedAsset)
            putExtra(KEY_INTENT_SELECTED_RECIPIENT, presenter.recipient)
            putExtra(KEY_INTENT_SELECTED_AMOUNT, presenter.amount)
            putExtra(KEY_INTENT_GATEWAY_COMMISSION, presenter.gatewayCommission.toFloat())
            if (!presenter.attachment.isNullOrEmpty()) {
                putExtra(KEY_INTENT_ATTACHMENT, presenter.attachment)
            }
            putExtra(KEY_INTENT_MONERO_PAYMENT_ID, presenter.moneroPaymentId)
            putExtra(KEY_INTENT_TYPE, presenter.type)
        }
    }

    private fun setPercent(percent: Double) {
        presenter.selectedAsset.notNull { assetBalance ->
            assetBalance.balance.notNull { balance ->
                val amount = (balance * percent).toLong()
                edit_amount.setText(MoneyUtil.getScaledText(amount, assetBalance))
            }
        }
    }

    override fun showXRate(xRate: XRate, ticker: String) {
        skeletonView!!.hide()

        val fee = if (xRate.feeOut == null) {
            "-"
        } else {
            BigDecimal(xRate.feeOut).toString()
        }

        val inMin = if (xRate.inMin == null) {
            "-"
        } else {
            BigDecimal(xRate.inMin).toString()
        }

        val inMax = if (xRate.inMax == null) {
            "-"
        } else {
            BigDecimal(xRate.inMax).toString()
        }

        gateway_fee.text = getString(R.string.send_gateway_info_gateway_fee,
                fee, ticker)
        gateway_limits.text = getString(R.string.send_gateway_info_gateway_limits,
                ticker, inMin, inMax)
        gateway_warning.text = getString(R.string.send_gateway_info_gateway_warning,
                ticker)
        setRecipientValid(presenter.isRecipientValid())
    }

    override fun showXRateError() {
        skeletonView!!.hide()
        relative_gateway_fee.gone()
        onShowError(R.string.receive_error_network)
    }

    private fun checkRecipient(recipient: String) {
        if (recipient.isNotEmpty()) {
            presenter.recipient = recipient

            when {
                recipient.length in 4..30 -> {
                    presenter.recipientAssetId = ""
                    presenter.checkAlias(recipient)
                    relative_gateway_fee.gone()
                }
                SendPresenter.isWavesAddress(recipient) -> {
                    presenter.recipientAssetId = ""
                    presenter.type = SendPresenter.Type.WAVES
                    setRecipientValid(true)
                    relative_gateway_fee.gone()
                }
                else -> {
                    presenter.recipientAssetId = SendPresenter.getAssetId(recipient)
                    if (presenter.recipientAssetId.isNullOrEmpty()) {
                        presenter.type = SendPresenter.Type.UNKNOWN
                        setRecipientValid(false)
                        monero_layout.gone()
                    } else {
                        if (presenter.recipientAssetId == presenter.selectedAsset?.assetId) {
                            setRecipientValid(true)
                            checkMonero(presenter.recipientAssetId)
                            loadGatewayXRate(presenter.recipientAssetId!!)
                        } else {
                            setRecipientValid(false)
                        }
                    }
                }
            }

            image_view_recipient_action.setImageResource(R.drawable.ic_deladdress_24_error_400)
            image_view_recipient_action.tag = R.drawable.ic_deladdress_24_error_400
            horizontal_recipient_suggestion.gone()
        } else {
            image_view_recipient_action.setImageResource(R.drawable.ic_qrcode_24_basic_500)
            image_view_recipient_action.tag = R.drawable.ic_qrcode_24_basic_500
            horizontal_recipient_suggestion.visiable()
            relative_gateway_fee.gone()
            monero_layout.gone()
        }
    }

    private fun checkMonero(assetId: String?) {
        if (assetId == Constants.MONERO_ASSET_ID) {
            monero_layout.visiable()
            eventSubscriptions.add(RxTextView.textChanges(edit_monero_payment_id)
                    .subscribe { paymentId ->
                        presenter.moneroPaymentId = paymentId.toString()
                        if (paymentId.isNullOrEmpty()) {
                            image_view_monero_action.setImageResource(
                                    R.drawable.ic_qrcode_24_basic_500)
                            image_view_monero_action.tag = R.drawable.ic_qrcode_24_basic_500
                        } else {
                            image_view_monero_action.setImageResource(
                                    R.drawable.ic_deladdress_24_error_400)
                            image_view_monero_action.tag = R.drawable.ic_deladdress_24_error_400
                        }
                    })
        } else {
            monero_layout.gone()
            presenter.moneroPaymentId = null
        }
    }

    override fun setRecipientValid(valid: Boolean?) {
        if (valid == null || valid) {
            text_recipient_error.gone()
        } else {
            text_recipient_error.visiable()
            relative_gateway_fee.gone()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_SCAN_RECEIVE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val result = IntentIntegrator.parseActivityResult(resultCode, data)
                            .contents
                            .replace(AddressUtil.WAVES_PREFIX, "")
                    parseDataFromQr(result)
                }
            }

            REQUEST_SCAN_MONERO -> {
                if (resultCode == Activity.RESULT_OK) {
                    val result = IntentIntegrator.parseActivityResult(resultCode, data)
                            .contents
                            .replace(AddressUtil.WAVES_PREFIX, "")
                    edit_monero_payment_id.setText(result)
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

    private fun parseDataFromQr(result: String) {
        if (result.isNullOrEmpty()) {
            showError(R.string.send_error_get_data_from_qr, R.id.root_view)
            return
        }

        if (result.contains("https://client.wavesplatform.com/#send/".toRegex()) ||
                result.contains("https://client.wavesplatform.com/%23send/".toRegex())) {
            val uri = URI.create(result
                    .replace("/#send/", "/send/")
                    .replace("/%23send/", "/send/"))
            try {
                val params = uri.query.split("&")
                for (parameter in params) {
                    if (parameter.contains("recipient=")) {
                        val recipient = parameter.replace("recipient=", "")
                        edit_address.setText(recipient)
                        recipientEnable(false)
                    }
                    if (parameter.contains("amount=")) {
                        val amount = parameter.replace("amount=", "")
                        if (amount.toDouble() > 0) {
                            edit_amount.setText(amount)
                            amountEnable(false)
                        }
                    }
                }

                var assetId = uri.path.split("/")[2]
                if ("waves".equalsIgnoreCase(assetId)) {
                    assetId = ""
                }
                val assetBalance = queryFirst<AssetBalance> {
                    equalTo("assetId", assetId)
                }

                if (assetBalance != null) {
                    setAsset(assetBalance)
                    assetEnable(false)
                }
            } catch (error: Exception) {
                showError(R.string.send_error_get_data_from_qr, R.id.root_view)
                error.printStackTrace()
            }
        } else {
            edit_address.setText(result)

            if (!TextUtils.isEmpty(result)) {
                edit_address.setText(result)
            } else {
            }
        }
    }

    private fun setAsset(asset: AssetBalance?) {
        asset.notNull {
            presenter.selectedAsset = asset

            relative_chosen_coin.visiable()
            text_asset_hint.gone()

            image_asset_icon.isOval = true
            image_asset_icon.setAsset(it)

            text_asset_name.text = it.getName()

            text_asset_value.text = it.getDisplayAvailableBalance()
            if (it.isFavorite) {
                image_asset_is_favourite.visiable()
            } else {
                image_asset_is_favourite.gone()
            }
            checkRecipient(edit_address.text.toString())
        }
    }

    private fun loadGatewayXRate(assetId: String) {
        if (AssetBalance.isGateway(assetId)) {
            relative_gateway_fee.visiable()
            if (skeletonView == null) {
                skeletonView = Skeleton.bind(relative_gateway_fee)
                        .color(R.color.basic50)
                        .load(R.layout.item_skeleton_gateway_warning)
                        .show()
            } else {
                skeletonView!!.show()
            }
            presenter.loadXRate(assetId)
        } else {
            relative_gateway_fee.gone()
        }
    }

    private fun assetEnable(enable: Boolean) {
        if (enable) {
            ViewCompat.setElevation(card_asset, dp2px(2).toFloat())
            card_asset.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
            asset_layout.background = null
            card_asset.click { launchAssets() }
            image_change.visibility = View.VISIBLE
        } else {
            ViewCompat.setElevation(card_asset, 0F)
            card_asset.setCardBackgroundColor(ContextCompat.getColor(this, R.color.basic50))
            asset_layout.background = ContextCompat.getDrawable(
                    this, R.drawable.shape_rect_bordered_accent50)
            card_asset.click { /* do nothing */ }
            image_change.visibility = View.GONE
        }
    }

    private fun recipientEnable(enable: Boolean) {
        if (enable) {
            ViewCompat.setElevation(recipient_card, dp2px(2).toFloat())
            recipient_card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
            recipient_layout.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            edit_address.isEnabled = true
        } else {
            ViewCompat.setElevation(recipient_card, 0F)
            recipient_card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.basic50))
            recipient_layout.background = ContextCompat.getDrawable(this,
                    R.drawable.shape_rect_bordered_accent50)
            edit_address.isEnabled = false
        }
    }

    private fun amountEnable(enable: Boolean) {
        if (enable) {
            ViewCompat.setElevation(amount_card, dp2px(2).toFloat())
            amount_card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
            amount_layout.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            edit_amount.isEnabled = true
            horizontal_amount_suggestion.visiable()
        } else {
            ViewCompat.setElevation(amount_card, 0F)
            amount_card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.basic50))
            amount_layout.background = ContextCompat.getDrawable(this,
                    R.drawable.shape_rect_bordered_accent50)
            edit_amount.isEnabled = false
            horizontal_amount_suggestion.gone()
        }
    }

    private fun launchAssets() {
        launchActivity<YourAssetsActivity>(requestCode = REQUEST_YOUR_ASSETS) {
            presenter.selectedAsset.notNull {
                putExtra(YourAssetsActivity.BUNDLE_ASSET_ID, it.assetId)
            }
        }
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }

    override fun needToShowNetworkMessage(): Boolean = true

    override fun onNetworkConnectionChanged(networkConnected: Boolean) {
        super.onNetworkConnectionChanged(networkConnected)
        button_continue.isEnabled = networkConnected
    }
}
