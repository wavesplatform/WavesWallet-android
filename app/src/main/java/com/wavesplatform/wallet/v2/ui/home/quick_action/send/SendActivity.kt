package com.wavesplatform.wallet.v2.ui.home.quick_action.send

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.AppCompatTextView
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
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.confirmation.SendConfirmationActivity.Companion.KEY_INTENT_BLOCKCHAIN_COMMISSION
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.confirmation.SendConfirmationActivity.Companion.KEY_INTENT_FEE_ASSET
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.confirmation.SendConfirmationActivity.Companion.KEY_INTENT_GATEWAY_COMMISSION
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.confirmation.SendConfirmationActivity.Companion.KEY_INTENT_MONERO_PAYMENT_ID
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.confirmation.SendConfirmationActivity.Companion.KEY_INTENT_SELECTED_AMOUNT
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.confirmation.SendConfirmationActivity.Companion.KEY_INTENT_SELECTED_ASSET
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.confirmation.SendConfirmationActivity.Companion.KEY_INTENT_SELECTED_RECIPIENT
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.confirmation.SendConfirmationActivity.Companion.KEY_INTENT_TYPE
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.fee.SponsoredFeeBottomSheetFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.leasing.start.StartLeasingActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.your_assets.YourAssetsActivity
import com.wavesplatform.wallet.v2.util.*
import kotlinx.android.synthetic.main.activity_send.*
import kotlinx.android.synthetic.main.layout_asset_card.*
import kotlinx.android.synthetic.main.view_commission.*
import pers.victor.ext.*
import java.math.BigDecimal
import java.net.URI
import javax.inject.Inject

class SendActivity : BaseActivity(), SendView {

    @Inject
    @InjectPresenter
    lateinit var presenter: SendPresenter

    private var xRateSkeletonView: SkeletonScreen? = null
    private var assetsSkeletonView: SkeletonScreen? = null

    @ProvidePresenter
    fun providePresenter(): SendPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_send

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

        setupCommissionBlock()

        when {
            intent.hasExtra(KEY_INTENT_ASSET_DETAILS) -> {
                setAsset(intent.getParcelableExtra(YourAssetsActivity.BUNDLE_ASSET_ITEM))
                assetEnable(false)
            }
            intent.hasExtra(KEY_INTENT_REPEAT_TRANSACTION) -> {
                val assetBalance = intent.getParcelableExtra<AssetBalance>(
                        SendActivity.KEY_INTENT_TRANSACTION_ASSET_BALANCE)
                val amount = intent
                        .getStringExtra(SendActivity.KEY_INTENT_TRANSACTION_AMOUNT).clearBalance()
                val recipientAddress = intent
                        .getStringExtra(SendActivity.KEY_INTENT_TRANSACTION_RECIPIENT)
                val attachment = intent
                        .getStringExtra(SendActivity.KEY_INTENT_TRANSACTION_ATTACHMENT)
                setAsset(assetBalance)
                assetEnable(false)
                edit_address.setText(recipientAddress)
                edit_amount.setText(amount)
                presenter.attachment = attachment
                presenter.amount = BigDecimal(amount)
            }
            else -> assetEnable(true)
        }

        eventSubscriptions.add(RxTextView.textChanges(edit_address)
                .subscribe {
                    checkRecipient(it.toString())
                })

        edit_amount.applyFilterStartWithDot()

        edit_amount.addTextChangedListener {
            on { s, _, _, _ ->
                if (s.isNotEmpty()) {
                    horizontal_amount_suggestion.visiable()
                    linear_fees_error.gone()
                }
                val amount = edit_amount.text?.toString() ?: "0"
                if (amount.isNotBlank()) {
                    if (amount == "." || amount == "-") {
                        presenter.amount = BigDecimal.ZERO
                    } else {
                        presenter.amount = BigDecimal(amount)
                    }
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
                linear_fees_error.gone()
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
                linear_fees_error.gone()
            } else if (it.tag == R.drawable.ic_qrcode_24_basic_500) {
                IntentIntegrator(this).setRequestCode(REQUEST_SCAN_MONERO)
                        .setOrientationLocked(true)
                        .setBeepEnabled(false)
                        .setCaptureActivity(QrCodeScannerActivity::class.java)
                        .initiateScan()
            }
        }

        button_continue.click { presenter.sendClicked() }

        text_use_total_balance.click { setPercent(1.0) }
        text_50_percent.click { setPercent(0.50) }
        text_10_percent.click { setPercent(0.10) }
        text_5_percent.click { setPercent(0.05) }

        setRecipientSuggestions()
        commission_card.gone()
    }

    private fun setupCommissionBlock() {
        image_arrows.visiable()
        commission_card.click {
            val dialog = SponsoredFeeBottomSheetFragment()
            dialog.configureData(presenter.feeAsset.assetId, presenter.feeWaves)
            dialog.onSelectedAssetListener = object : SponsoredFeeBottomSheetFragment.SponsoredAssetSelectedListener {
                override fun onSelected(asset: AssetBalance, fee: Long) {
                    presenter.feeAsset = asset
                    presenter.fee = fee

                    if (presenter.feeAsset.assetId.isWaves()) {
                        text_fee_transaction.text = MoneyUtil.getScaledText(fee, asset).stripZeros()
                        commission_asset_name_text.visiable()
                    } else {
                        text_fee_transaction.text = "${MoneyUtil.getScaledText(fee, asset).stripZeros()} ${asset.getName()}"
                        commission_asset_name_text.gone()
                    }

                    text_amount_fee_error.text = getString(
                            R.string.send_error_you_don_t_have_enough_funds_to_pay_the_required_fees,
                            "${getScaledAmount(presenter.fee, asset.getDecimals())} ${asset.getName()}",
                            presenter.gatewayCommission.toPlainString(),
                            presenter.selectedAsset?.getName() ?: "")
                }
            }
            dialog.show(supportFragmentManager, dialog::class.java.simpleName)
        }
    }

    private fun loadAsset(assetId: String) {
        if (assetsSkeletonView == null) {
            assetsSkeletonView = Skeleton.bind(edit_asset_layout)
                    .color(R.color.basic50)
                    .load(R.layout.item_skeleton_asset)
                    .show()
        } else {
            assetsSkeletonView!!.show()
        }
        assetEnable(false)
        button_continue.isEnabled = false
        presenter.loadAsset(assetId)
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
            val addressBookUser = prefsUtil.getAddressBookUser(address)
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

    override fun onShowPaymentDetails() {
        launchActivity<SendConfirmationActivity>(REQUEST_SEND) {
            putExtra(KEY_INTENT_SELECTED_ASSET, presenter.selectedAsset)
            putExtra(KEY_INTENT_SELECTED_RECIPIENT, presenter.recipient)
            putExtra(KEY_INTENT_SELECTED_AMOUNT, presenter.amount.toPlainString())
            putExtra(KEY_INTENT_GATEWAY_COMMISSION, presenter.gatewayCommission.toPlainString())
            if (!presenter.attachment.isNullOrEmpty()) {
                putExtra(KEY_INTENT_ATTACHMENT, presenter.attachment)
            }
            putExtra(KEY_INTENT_MONERO_PAYMENT_ID, presenter.moneroPaymentId)
            putExtra(KEY_INTENT_TYPE, presenter.type)
            putExtra(KEY_INTENT_BLOCKCHAIN_COMMISSION, presenter.fee)
            putExtra(KEY_INTENT_FEE_ASSET, presenter.feeAsset)
        }
    }

    private fun setPercent(percent: Double) {
        presenter.selectedAsset.notNull { assetBalance ->
            val amount = (assetBalance.getAvailableBalance() * percent).toLong()
            checkAndSetAmount(amount, assetBalance)
        }
    }

    private fun checkAndSetAmount(amount: Long, assetBalance: AssetBalance) {
        if (presenter.type == SendPresenter.Type.GATEWAY) {
            val total = BigDecimal.valueOf(amount,
                    assetBalance.getDecimals())
                    .minus(presenter.gatewayCommission)
            if (total.toFloat() > 0) {
                edit_amount.setText(total.toString().stripZeros())
                linear_fees_error.gone()
            } else {
                linear_fees_error.visiable()
                edit_amount.setText("")
                horizontal_amount_suggestion.gone()
                text_amount_fee_error.text = getString(
                        R.string.send_error_you_don_t_have_enough_funds_to_pay_the_required_fees,
                        "${getScaledAmount(presenter.fee, presenter.feeAsset.getDecimals())} ${presenter.feeAsset.getName()}",
                        presenter.gatewayCommission.toPlainString(),
                        assetBalance.getName() ?: "")
                presenter.amount = BigDecimal.ZERO
            }
        } else if (presenter.type == SendPresenter.Type.WAVES &&
                assetBalance.assetId.isWavesId()) {
            val total = BigDecimal.valueOf(amount - presenter.fee,
                    assetBalance.getDecimals())
            if (total.toFloat() > 0) {
                edit_amount.setText(total.toString().stripZeros())
                linear_fees_error.gone()
            } else {
                edit_amount.setText("")
                horizontal_amount_suggestion.gone()
                text_amount_error.visiable()
                presenter.amount = BigDecimal.ZERO
            }
        } else {
            val total = if (assetBalance.assetId.isWavesId()) {
                amount - presenter.fee
            } else {
                amount
            }

            if (total.toFloat() > 0) {
                edit_amount.setText(MoneyUtil.getScaledText(total, assetBalance).clearBalance())
            } else {
                edit_amount.setText("")
                text_amount_error.visiable()
                presenter.amount = BigDecimal.ZERO
            }
        }
    }

    override fun showXRate(xRate: XRate, ticker: String) {
        xRateSkeletonView!!.hide()

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
        xRateSkeletonView!!.hide()
        gateway_fee.text = getString(R.string.send_gateway_error_title)
        gateway_limits.text = getString(R.string.send_gateway_error_subtitle)
        relative_do_not_withdraw.gone()
        monero_layout.gone()
        text_amount_title.gone()
        amount_card.gone()
        horizontal_amount_suggestion.gone()
        linear_fees_error.gone()
        button_continue.isEnabled = false
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
                    presenter.recipientAssetId = SendPresenter.getAssetId(recipient, presenter.selectedAsset)
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
            text_amount_title.visiable()
            amount_card.visiable()
            button_continue.isEnabled = true
        }
    }

    private fun checkMonero(assetId: String?) {
        if (assetId == Constants.findByGatewayId("XMR")!!.assetId) {
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

    override fun showCommissionLoading() {
        progress_bar_fee_transaction.show()
        text_fee_transaction.gone()
    }

    override fun showCommissionSuccess(unscaledAmount: Long) {
        commission_card.visiable()
        text_fee_transaction.text = MoneyUtil.getScaledText(unscaledAmount, 8)
        progress_bar_fee_transaction.hide()
        text_fee_transaction.visiable()
    }

    override fun showCommissionError() {
        text_fee_transaction.text = "-"
        showError(R.string.common_error_commission_receiving, R.id.root)
        progress_bar_fee_transaction.hide()
        text_fee_transaction.visiable()
    }

    override fun showLoadAssetSuccess(assetBalance: AssetBalance) {
        assetsSkeletonView!!.hide()
        setAsset(assetBalance)
        assetEnable(false)
        showError(R.string.insufficient_funds, R.id.root)
    }

    override fun showLoadAssetError(errorMsgRes: Int) {
        assetsSkeletonView!!.hide()
        setAsset(null)
        assetEnable(false)
        showError(errorMsgRes, R.id.root)
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

            REQUEST_SEND -> {
                when (resultCode) {
                    Constants.RESULT_SMART_ERROR -> {
                        showAlertAboutScriptedAccount()
                    }
                }
            }
        }
    }

    private fun parseDataFromQr(result: String) {
        if (result.isNullOrEmpty()) {
            showError(R.string.send_error_get_data_from_qr, R.id.root_view)
            assetEnable(false)
            recipientEnable(false)
            amountEnable(false)
            return
        }

        if (result.contains("https://client.wavesplatform.com/#send/".toRegex()) ||
                result.contains("https://client.wavesplatform.com/%23send/".toRegex())) {
            val uri = URI.create(result.replace(" ", "")
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
                if (Constants.WAVES_ASSET_ID_FILLED.equalsIgnoreCase(assetId)) {
                    assetId = ""
                }
                val assetBalance = queryFirst<AssetBalance> {
                    equalTo("assetId", assetId)
                }

                if (assetBalance == null) {
                    loadAsset(assetId)
                } else {
                    setAsset(assetBalance)
                    assetEnable(false)
                }
            } catch (error: Exception) {
                showError(R.string.send_error_get_data_from_qr, R.id.root_view)
                assetEnable(false)
                recipientEnable(false)
                amountEnable(false)
                error.printStackTrace()
            }
        } else {
            if (result.contains(":")) {
                val split = result.split(":")
                edit_address.setText(split[1].trim())
            } else {
                edit_address.setText(result)
            }
        }
    }

    private fun setAsset(asset: AssetBalance?) {
        if (asset == null) {
            text_asset_error.visiable()
            presenter.selectedAsset = null
            text_asset_value.text = "-"
            container_asset.visiable()
            button_continue.isEnabled = false
            text_asset.gone()
        } else {
            text_asset_error.gone()

            presenter.selectedAsset = asset

            image_asset_icon.setAsset(asset)
            text_asset_name.text = asset.getName()
            text_asset_value.text = asset.getDisplayAvailableBalance()

            image_is_favourite.visiableIf {
                asset.isFavorite
            }

            container_asset.visiable()

            checkRecipient(edit_address.text.toString())

            text_amount_title.visiable()
            amount_card.visiable()
            button_continue.isEnabled = true

            presenter.loadCommission(presenter.selectedAsset?.assetId)

            text_asset.gone()
        }
    }

    private fun loadGatewayXRate(assetId: String) {
        if (AssetBalance.isGateway(assetId)) {
            relative_do_not_withdraw.visiable()
            relative_gateway_fee.visiable()
            if (xRateSkeletonView == null) {
                xRateSkeletonView = Skeleton.bind(relative_gateway_fee)
                        .color(R.color.basic50)
                        .load(R.layout.item_skeleton_gateway_warning)
                        .show()
            } else {
                xRateSkeletonView!!.show()
            }
            presenter.loadXRate(assetId)
        } else {
            relative_gateway_fee.gone()
        }
    }

    private fun assetEnable(enable: Boolean) {
        if (enable) {
            ViewCompat.setElevation(edit_asset_card, dp2px(2).toFloat())
            edit_asset_card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
            edit_asset_layout.background = null
            edit_asset_card.click { launchAssets() }
            image_change.visibility = View.VISIBLE
        } else {
            ViewCompat.setElevation(edit_asset_card, 0F)
            edit_asset_card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.basic50))
            edit_asset_layout.background = ContextCompat.getDrawable(
                    this, R.drawable.shape_rect_bordered_accent50)
            edit_asset_card.click { /* do nothing */ }
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

    override fun onDestroy() {
        progress_bar_fee_transaction.hide()
        super.onDestroy()
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

    companion object {
        const val REQUEST_YOUR_ASSETS = 43
        const val REQUEST_SCAN_RECEIVE = 44
        const val REQUEST_SCAN_MONERO = 45
        const val REQUEST_SEND = 46
        const val KEY_INTENT_ASSET_DETAILS = "asset_details"
        const val KEY_INTENT_REPEAT_TRANSACTION = "repeat_transaction"
        const val KEY_INTENT_TRANSACTION_ASSET_BALANCE = "transaction_asset_balance"
        const val KEY_INTENT_TRANSACTION_AMOUNT = "transaction_amount"
        const val KEY_INTENT_TRANSACTION_ATTACHMENT = "transaction_attachment"
        const val KEY_INTENT_TRANSACTION_RECIPIENT = "transaction_recipient"
    }
}
