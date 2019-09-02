package com.wavesplatform.wallet.v2.ui.keeper

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.wavesplatform.sdk.WavesSdk
import com.wavesplatform.sdk.crypto.WavesCrypto
import com.wavesplatform.sdk.keeper.interfaces.KeeperTransaction
import com.wavesplatform.sdk.keeper.model.KeeperActionType
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import com.wavesplatform.sdk.model.request.node.DataTransaction
import com.wavesplatform.sdk.model.request.node.InvokeScriptTransaction
import com.wavesplatform.sdk.model.request.node.TransferTransaction
import com.wavesplatform.sdk.model.response.node.AssetsDetailsResponse
import com.wavesplatform.sdk.utils.Identicon
import com.wavesplatform.sdk.utils.MoneyUtil
import com.wavesplatform.sdk.utils.WavesConstants
import com.wavesplatform.sdk.utils.stripZeros
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.helpers.KeeperIntentHelper
import com.wavesplatform.wallet.v2.data.model.local.KeeperIntentResult
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.util.WavesWallet
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.showError
import kotlinx.android.synthetic.main.activity_keeper_transaction.*
import pers.victor.ext.click
import pers.victor.ext.date
import pers.victor.ext.inflate
import pers.victor.ext.visiable
import javax.inject.Inject

class KeeperTransactionActivity : BaseActivity(), KeeperTransactionView {

    // todo temp
    private var callback = "myapp"
    private var appName = "My B Application"
    private var iconUrl = "http://icons.iconarchive.com/icons/graphicloads/100-flat/96/home-icon.png"
    private var actionType = KeeperActionType.SEND

    @Inject
    @InjectPresenter
    lateinit var presenter: KeeperTransactionPresenter

    @ProvidePresenter
    fun providePresenter(): KeeperTransactionPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_keeper_transaction

    override fun askPassCode() = true

    override fun needToShowNetworkMessage() = true

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        setNavigationBarColor(R.color.white)
        setupToolbar(toolbar_view, true,
                getString(R.string.keeper_title_confirm_request), R.drawable.ic_toolbar_back_black)

        if (App.getAccessManager().isAuthenticated()) {
            init(takeTransaction())
        } else {
            KeeperIntentHelper.exitToDAppWithResult(this, failResult(), WavesSdk.keeper())
            finish()
        }
    }

    override fun onBackPressed() {
        KeeperIntentHelper.exitToDAppWithResult(
                this, failResult("User Reject"), WavesSdk.keeper())
        finish()
    }

    private fun failResult(message: String = "Fail"): KeeperIntentResult {
        return if (actionType == KeeperActionType.SIGN) {
            KeeperIntentResult.ErrorSignResult("ErrorSignResult: $message")
        } else {
            KeeperIntentResult.ErrorSendResult("ErrorSendResult: $message")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_KEEPER_TX_ACTION -> {
                val result = if (resultCode == Activity.RESULT_OK && data != null) {
                    if (actionType == KeeperActionType.SIGN) {
                        KeeperIntentResult.SuccessSignResult(presenter.transaction)
                    } else {
                        KeeperIntentResult.SuccessSendResult(
                                data.getParcelableExtra(KEY_INTENT_RESPONSE_TRANSACTION))
                    }
                } else {
                    failResult()
                }

                KeeperIntentHelper.exitToDAppWithResult(this, result, WavesSdk.keeper())
                finish()
            }
        }
    }


    override fun onSuccessSign(transaction: BaseTransaction) {
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onError(error: Throwable) {
        showProgressBar(false)
        showError(error.localizedMessage, R.id.content)
        setResult(Activity.RESULT_CANCELED)
    }

    override fun onReceiveTransactionData(type: Byte, transaction: KeeperTransaction, fee: Long,
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

        Glide.with(this)
                .load(iconUrl)
                .apply(RequestOptions().circleCrop())
                .into(image_address_to)
        text_address_to.text = appName

        when (actionType) {
            KeeperActionType.SEND -> {
                button_approve.text = getText(R.string.keeper_send)
                button_approve.click {
                    launchActivity<KeeperConfirmTransactionActivity>(
                            requestCode = REQUEST_KEEPER_TX_ACTION) {
                        when (presenter.transaction) {
                            is TransferTransaction -> {
                                putExtra(KEY_INTENT_TRANSACTION, presenter.transaction as TransferTransaction)
                            }
                            is DataTransaction -> {
                                putExtra(KEY_INTENT_TRANSACTION, presenter.transaction as DataTransaction)
                            }
                            is InvokeScriptTransaction -> {
                                putExtra(KEY_INTENT_TRANSACTION, presenter.transaction as InvokeScriptTransaction)
                            }
                        }
                    }
                }
            }
            KeeperActionType.SIGN -> {
                button_approve.text = getText(R.string.keeper_sign)
                button_approve.click {
                    presenter.signTransaction(presenter.transaction)
                    button_approve.isEnabled = true
                }
            }
        }

        button_reject.click { onBackPressed() }
    }

    private fun setInvokeTransaction(transaction: InvokeScriptTransaction, dAppAddress: String, assetDetails: HashMap<String, AssetsDetailsResponse>) {
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

        transaction_view.setTransaction(transaction, assetDetail)
        transaction.sign(App.getAccessManager().getWallet()?.seedStr ?: "")

        val txId = WavesCrypto.base58encode(WavesCrypto.blake2b(transaction.toBytes()))
        text_transaction_txid.text = txId

        text_transaction_fee_value.text = MoneyUtil.getScaledText(
                presenter.fee, assetDetail?.decimals ?: 8).stripZeros()

        text_transaction_time.text = transaction.timestamp.date(Constants.DATE_TIME_PATTERN)
    }

    private fun takeTransaction(): KeeperTransaction {
        // todo get from intent
        /*val tx = TransferTransaction(
                assetId = "Ft8X1v1LTa1ABafufpaCWyVj8KkaxUWE6xBhW6sNFJck",
                recipient = "3P8ys7s9r61Dapp8wZ94NBJjhmPHcBVBkMf",
                amount = 1,
                fee = WavesConstants.WAVES_MIN_FEE,
                attachment = SignUtil.textToBase58("Hello-!"),
                feeAssetId = WavesConstants.WAVES_ASSET_ID_EMPTY
        )
        tx.senderPublicKey = "B3f8VFh6T2NGT26U7rHk2grAxn5zi9iLkg4V9uxG6C8q"
        tx.timestamp = System.currentTimeMillis()*/

        val tx = DataTransaction(mutableListOf(
                DataTransaction.Data("key0", "string", "This is Data TX"),
                DataTransaction.Data("key1", "integer", 100),
                DataTransaction.Data("key2", "integer", -100),
                DataTransaction.Data("key3", "boolean", true),
                DataTransaction.Data("key4", "boolean", false),
                DataTransaction.Data("key5", "binary", "SGVsbG8h") // base64 binary string
        ))
        tx.senderPublicKey = "B3f8VFh6T2NGT26U7rHk2grAxn5zi9iLkg4V9uxG6C8q"
        tx.timestamp = System.currentTimeMillis()


        /*val args = mutableListOf(
                InvokeScriptTransaction.Arg("string", "Some string!"),
                InvokeScriptTransaction.Arg("integer", 128L),
                InvokeScriptTransaction.Arg("integer", -127L),
                InvokeScriptTransaction.Arg("boolean", true),
                InvokeScriptTransaction.Arg("boolean", false),
                InvokeScriptTransaction.Arg("binary", "base64:VGVzdA=="))

        val call = InvokeScriptTransaction.Call(
                function = "testarg",
                args = args
        )

        val payment = mutableListOf(
                InvokeScriptTransaction.Payment(
                        assetId = null,
                        amount = 1L))

        val tx = InvokeScriptTransaction(
                dApp = "3Mv9XDntij4ZRE1XiNZed6J74rncBpiYNDV",
                call = call,
                payment = payment)

        tx.fee = 500000L
        tx.senderPublicKey = "B3f8VFh6T2NGT26U7rHk2grAxn5zi9iLkg4V9uxG6C8q"
        tx.timestamp = System.currentTimeMillis()*/

        return tx
    }

    companion object {
        const val REQUEST_KEEPER_TX_ACTION = 1000
        const val KEY_INTENT_TRANSACTION = "key_intent_transaction"
        const val KEY_INTENT_RESPONSE_TRANSACTION = "key_intent_response_transaction"
    }
}