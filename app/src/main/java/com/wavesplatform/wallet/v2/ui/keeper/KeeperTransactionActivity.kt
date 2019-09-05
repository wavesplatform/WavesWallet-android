package com.wavesplatform.wallet.v2.ui.keeper

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.wavesplatform.sdk.WavesSdk
import com.wavesplatform.sdk.crypto.WavesCrypto
import com.wavesplatform.sdk.keeper.interfaces.KeeperTransaction
import com.wavesplatform.sdk.keeper.interfaces.KeeperTransactionResponse
import com.wavesplatform.sdk.keeper.model.KeeperActionType
import com.wavesplatform.sdk.keeper.model.KeeperIntentResult
import com.wavesplatform.sdk.model.request.node.DataTransaction
import com.wavesplatform.sdk.model.request.node.InvokeScriptTransaction
import com.wavesplatform.sdk.model.request.node.TransferTransaction
import com.wavesplatform.sdk.model.response.node.AssetsDetailsResponse
import com.wavesplatform.sdk.utils.*
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.helpers.KeeperIntentHelper
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.util.WavesWallet
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.showError
import kotlinx.android.synthetic.main.activity_keeper_transaction.*
import pers.victor.ext.*
import javax.inject.Inject

class KeeperTransactionActivity : BaseActivity(), KeeperTransactionView {

    @Inject
    @InjectPresenter
    lateinit var presenter: KeeperTransactionPresenter

    @ProvidePresenter
    fun providePresenter(): KeeperTransactionPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_keeper_transaction

    override fun askPassCode() = true

    override fun needToShowNetworkMessage() = true

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.white)
        setNavigationBarColor(R.color.white)
        setupToolbar(toolbar_view, true,
                getString(R.string.keeper_title_confirm_request), R.drawable.ic_toolbar_back_black)

        presenter.actionType = WavesSdk.keeper().keeperDataHolder()?.processData?.actionType
                ?: KeeperActionType.SIGN
        presenter.transaction = WavesSdk.keeper().keeperDataHolder()?.processData?.transaction
        presenter.dApp = WavesSdk.keeper().keeperDataHolder()?.processData?.dApp

        if (App.getAccessManager().isAuthenticated() && presenter.transaction != null) {
            init(presenter.transaction!!)
        } else {
            KeeperIntentHelper.exitToRootWithResult(this, failResult())
            finish()
        }
    }

    override fun onBackPressed() {
        KeeperIntentHelper.exitToRootWithResult(this, KeeperIntentResult.RejectedResult)
        finish()
    }

    private fun failResult(message: String = "Fail"): KeeperIntentResult {
        return if (presenter.actionType == KeeperActionType.SIGN) {
            KeeperIntentResult.ErrorSignResult("ErrorSignResult: $message")
        } else {
            KeeperIntentResult.ErrorSendResult("ErrorSendResult: $message")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_KEEPER_TX_ACTION -> {
                val result = if (resultCode == Activity.RESULT_OK
                        && data != null && presenter.transaction != null) {
                    KeeperIntentResult.SuccessSendResult(
                            data.getParcelableExtra(KEY_INTENT_RESPONSE_TRANSACTION))
                } else {
                    failResult()
                }

                KeeperIntentHelper.exitToRootWithResult(this, result)
                finish()
            }
        }
    }


    override fun onSuccessSign(transaction: KeeperTransaction) {
        KeeperIntentHelper.exitToRootWithResult(this, KeeperIntentResult.SuccessSignResult(transaction))
        finish()
    }

    override fun onError(error: Throwable) {
        showProgressBar(false)
        showError(error.localizedMessage, R.id.content)
    }

    override fun onReceiveTransactionData(transaction: KeeperTransaction?,
                                          dAppAddress: String,
                                          assetDetails: HashMap<String, AssetsDetailsResponse>) {
        when (transaction) {
            is InvokeScriptTransaction -> {
                setInvokeTransaction(transaction, dAppAddress, assetDetails)
            }
            is DataTransaction -> {
                setDataTransaction(transaction)
            }
            is TransferTransaction -> {
                setTransferTransaction(assetDetails, transaction)
            }
        }
        button_approve.isEnabled = true
        showProgressBar(false)
    }

    private fun init(transaction: KeeperTransaction) {
        showProgressBar(true)
        presenter.receiveTransactionData(transaction, WavesWallet.getAddress())

        Glide.with(this)
                .load(Identicon().create(App.getAccessManager().getWallet()?.address ?: ""))
                .apply(RequestOptions().circleCrop())
                .into(image_address_from)
        text_address_from.text = App.getAccessManager().getWalletName(
                App.getAccessManager().getLoggedInGuid())

        presenter.dApp.notNull {
            Glide.with(this)
                    .load(it.iconUrl)
                    .apply(RequestOptions().circleCrop())
                    .into(image_address_to)
            text_address_to.text = it.name
        }

        when (presenter.actionType) {
            KeeperActionType.SEND -> {
                button_approve.text = getText(R.string.keeper_send)
                button_approve.click {
                    launchActivity<KeeperSendTransactionActivity>(
                            requestCode = REQUEST_KEEPER_TX_ACTION) {
                        when (presenter.transaction) {
                            is TransferTransaction -> {
                                putExtra(KEY_INTENT_TRANSACTION, transaction)
                            }
                            is DataTransaction -> {
                                putExtra(KEY_INTENT_TRANSACTION, transaction)
                            }
                            is InvokeScriptTransaction -> {
                                putExtra(KEY_INTENT_TRANSACTION, transaction)
                            }
                        }
                    }
                }
            }
            KeeperActionType.SIGN -> {
                button_approve.text = getText(R.string.keeper_sign)
                button_approve.click {
                    presenter.transaction.notNull {
                        presenter.signTransaction(it)
                        button_approve.isEnabled = true
                    }
                }
            }
        }

        button_reject.click { onBackPressed() }
    }

    private fun setInvokeTransaction(transaction: InvokeScriptTransaction, dAppAddress: String, assetDetails: HashMap<String, AssetsDetailsResponse>) {
        transaction.timestamp = currentTimeMillis

        transaction_view.setTransaction(transaction)
        
        transaction.sign(App.getAccessManager().getWallet()?.seedStr ?: "")

        val txId = WavesCrypto.base58encode(WavesCrypto.blake2b(transaction.toBytes()))
        text_transaction_txid.text = txId

        text_transaction_fee_value.text = ">=" + MoneyUtil.getScaledText(
                transaction.fee, WavesConstants.WAVES_ASSET_INFO).stripZeros()

        text_transaction_time.text = transaction.timestamp.date(Constants.DATE_TIME_PATTERN)

        scriptInvokeLayout.visiable()
        text_transaction_scriptAddress.text = dAppAddress
        text_transaction_function.text = transaction.call!!.function
        transaction.payment.forEach {
            val view = inflate(R.layout.item_invoke_script_payment)
            val payment = view.findViewById<TextView>(R.id.text_transaction_payment)
            val assetId = view.findViewById<TextView>(R.id.payment_text_tag)

            val assetDetail = assetDetails[it.assetId]

            payment.text = MoneyUtil.getScaledText(
                    it.amount, assetDetail?.decimals ?: 8).stripZeros()
            assetId.text = assetDetail?.name ?: WavesConstants.WAVES_ASSET_INFO.name

            scriptInvokeLayout.addView(view)
        }
    }

    private fun setDataTransaction(transaction: DataTransaction) {
        transaction.timestamp = currentTimeMillis

        transaction_view.setTransaction(transaction)

        transaction.sign(App.getAccessManager().getWallet()?.seedStr ?: "")

        val txId = WavesCrypto.base58encode(WavesCrypto.blake2b(transaction.toBytes()))
        text_transaction_txid.text = txId

        text_transaction_fee_value.text = MoneyUtil.getScaledText(
                presenter.fee, WavesConstants.WAVES_ASSET_INFO).stripZeros()

        text_transaction_time.text = transaction.timestamp.date(Constants.DATE_TIME_PATTERN)
    }

    private fun setTransferTransaction(assetDetails: HashMap<String, AssetsDetailsResponse>, transaction: TransferTransaction) {
        val assetDetail = assetDetails.values.firstOrNull()

        transaction.timestamp = currentTimeMillis

        transaction_view.setTransaction(transaction, assetDetail)

        transaction.sign(App.getAccessManager().getWallet()?.seedStr ?: "")

        val txId = WavesCrypto.base58encode(WavesCrypto.blake2b(transaction.toBytes()))
        text_transaction_txid.text = txId

        text_transaction_fee_value.text = MoneyUtil.getScaledText(
                presenter.fee, assetDetail?.decimals ?: 8).stripZeros()

        text_transaction_time.text = transaction.timestamp.date(Constants.DATE_TIME_PATTERN)
    }

    companion object {
        const val REQUEST_KEEPER_TX_ACTION = 1000
        const val KEY_INTENT_TRANSACTION = "key_intent_transaction"
        const val KEY_INTENT_RESPONSE_TRANSACTION = "key_intent_response_transaction"
    }
}