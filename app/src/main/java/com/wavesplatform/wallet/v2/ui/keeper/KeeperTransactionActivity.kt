package com.wavesplatform.wallet.v2.ui.keeper

import android.app.Activity
import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.vicpin.krealmextensions.queryAll
import com.wavesplatform.sdk.crypto.WavesCrypto
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import com.wavesplatform.sdk.model.request.node.DataTransaction
import com.wavesplatform.sdk.model.request.node.InvokeScriptTransaction
import com.wavesplatform.sdk.model.request.node.TransferTransaction
import com.wavesplatform.sdk.model.response.node.AssetsDetailsResponse
import com.wavesplatform.sdk.model.response.node.transaction.BaseTransactionResponse
import com.wavesplatform.sdk.model.response.node.transaction.DataTransactionResponse
import com.wavesplatform.sdk.model.response.node.transaction.InvokeScriptTransactionResponse
import com.wavesplatform.sdk.model.response.node.transaction.TransferTransactionResponse
import com.wavesplatform.sdk.utils.*
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.db.SpamAssetDb
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.util.WavesWallet
import com.wavesplatform.wallet.v2.util.getTransactionType
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.showError
import kotlinx.android.synthetic.main.activity_keeper_transaction.*
import pers.victor.ext.click
import pers.victor.ext.date
import pers.victor.ext.visiable
import javax.inject.Inject
import com.wavesplatform.sdk.keeper.interfaces.KeeperTransaction as KeeperTransaction

class KeeperTransactionActivity : BaseActivity(), KeeperTransactionView {

    @Inject
    @InjectPresenter
    lateinit var presenter: KeeperTransactionPresenter

    @ProvidePresenter
    fun providePresenter(): KeeperTransactionPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_keeper_transaction

    override fun askPassCode() = true

    var link = ""
    var callback = "myapp"
    var appName = "My B Application"
    var iconUrl = "http://icons.iconarchive.com/icons/graphicloads/100-flat/96/home-icon.png"
    var kind = "send"

    var transaction: KeeperTransaction? = null
    var spam: HashSet<String> = hashSetOf()

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        setNavigationBarColor(R.color.white)
        setupToolbar(toolbar_view, true,
                getString(R.string.keeper_title_confirm_request), R.drawable.ic_toolbar_back_black)

        if (!App.getAccessManager().isAuthenticated()) {
            return
        }
    }

    override fun onResume() {
        super.onResume()
        if (App.getAccessManager().isAuthenticated()) {
            setTransaction()
        }
    }

    private fun setTransaction() {
        transaction = takeTransaction()

        if (transaction == null) {
            finish()
            return
        }


        presenter.receiveTransactionData(transaction!!, WavesWallet.getAddress())




        queryAll<SpamAssetDb>().forEach {
            spam.add(it.assetId ?: "")
        }


        val addressFrom = App.getAccessManager().getWallet()?.address ?: ""
        val accountName = App.getAccessManager().getWalletName(
                App.getAccessManager().getLoggedInGuid())

        Glide.with(this)
                .load(Identicon().create(addressFrom))
                .apply(RequestOptions().circleCrop())
                .into(image_address_from)

        text_address_from.text = accountName

        Glide.with(this)
                .load(iconUrl)
                .apply(RequestOptions().circleCrop())
                .into(image_address_to)

        text_address_to.text = appName

        if (kind == "sign") {
            button_approve.text = getText(R.string.keeper_sign)
            button_approve.click {
                presenter.signTransaction(transaction!! as BaseTransaction)
            }
        } else {
            button_approve.text = getText(R.string.keeper_send)
            button_approve.click {
                presenter.sendTransaction(transaction!! as BaseTransaction)
            }
        }

        button_reject.click {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun takeTransaction(): KeeperTransaction {
        /*val tx = TransferTransaction(
                assetId = WavesConstants.WAVES_ASSET_ID_EMPTY,
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

    override fun onSuccessSend(transaction: BaseTransactionResponse) {
        val transactionType = getTransactionType(
                this@KeeperTransactionActivity.transaction!! as BaseTransaction, WavesWallet.getAddress(), spam)
        when {
            transaction.type == BaseTransaction.TRANSFER -> {
                transaction as TransferTransactionResponse
                launchActivity<KeeperConfirmTransactionActivity> {
                    putExtra(KEY_INTENT_TRANSACTION_TYPE, transactionType)
                    putExtra(KEY_INTENT_TRANSACTION, transaction)
                    // putExtra(KEY_INTENT_ASSET_DETAILS, presenter.assetsDetails)
                    putExtra(KEY_INTENT_KIND, kind)
                    putExtra(KEY_INTENT_CALLBACK, callback)
                }
            }
            transaction.type == BaseTransaction.DATA -> {
                transaction as DataTransactionResponse
                launchActivity<KeeperConfirmTransactionActivity> {
                    putExtra(KEY_INTENT_TRANSACTION_TYPE, transactionType)
                    putExtra(KEY_INTENT_TRANSACTION, transaction)
                    // putExtra(KEY_INTENT_ASSET_DETAILS, presenter.assetsDetails)
                    putExtra(KEY_INTENT_KIND, kind)
                    putExtra(KEY_INTENT_CALLBACK, callback)
                }
            }
            transaction.type == BaseTransaction.SCRIPT_INVOCATION -> {
                transaction as InvokeScriptTransactionResponse
                launchActivity<KeeperConfirmTransactionActivity> {
                    putExtra(KEY_INTENT_TRANSACTION_TYPE, transactionType)
                    putExtra(KEY_INTENT_TRANSACTION, transaction)
                   //  putExtra(KEY_INTENT_ASSET_DETAILS, presenter.assetsDetails)
                    putExtra(KEY_INTENT_KIND, kind)
                    putExtra(KEY_INTENT_CALLBACK, callback)
                }
            }
            else -> {
                // do nothing
            }
        }
    }

    override fun onReceiveTransactionData(type: Byte, transaction: KeeperTransaction, fee: Long,
                                          dAppAddress: String,
                                          assetDetails: HashMap<String, AssetsDetailsResponse>) {

        when (type) {
            BaseTransaction.SCRIPT_INVOCATION -> {
                scriptInvokeLayout.visiable()
                val invokeTransaction = transaction as InvokeScriptTransaction
                /*text_transaction_payment.text = invokeTransaction.payment[0].amount
                payment_text_tag.text = invokeTransaction.payment[0].amount*/
                text_transaction_scriptAddress.text = dAppAddress
                text_transaction_function.text = invokeTransaction.call!!.function

                val tempTransaction = transaction
                tempTransaction.sign(App.getAccessManager().getWallet()?.seedStr ?: "")
                val id = WavesCrypto.base58encode(WavesCrypto.blake2b(tempTransaction.toBytes()))
                text_transaction_txid.text = id

                transaction_view.setTransaction(transaction as BaseTransaction, null, spam)
            }
            BaseTransaction.DATA -> {
                transaction as DataTransaction
                transaction_view.setTransaction(transaction = transaction)
                val tempTransaction = transaction
                tempTransaction.sign(App.getAccessManager().getWallet()?.seedStr ?: "")
                val id = WavesCrypto.base58encode(WavesCrypto.blake2b(tempTransaction.toBytes()))
                text_transaction_txid.text = id

                text_tag.visiable()
                text_transaction_fee_value.text = MoneyUtil.getScaledText(
                        presenter.fee, WavesConstants.WAVES_ASSET_INFO).stripZeros()
                text_transaction_time.text = transaction.timestamp.date(Constants.DATE_TIME_PATTERN)
            }
            BaseTransaction.TRANSFER -> {
                transaction as TransferTransaction

                val assetDetail = assetDetails.values.toList()[0]

                transaction_view.setTransaction(transaction, assetDetail, spam)
                val tempTransaction = transaction
                tempTransaction.sign(App.getAccessManager().getWallet()?.seedStr ?: "")
                val id = WavesCrypto.base58encode(WavesCrypto.blake2b(tempTransaction.toBytes()))
                text_transaction_txid.text = id

                text_tag.visiable()
                text_transaction_fee_value.text = MoneyUtil.getScaledText(presenter.fee, assetDetail.decimals).stripZeros()
                text_transaction_time.text = transaction.timestamp.date(Constants.DATE_TIME_PATTERN)
            }
        }
    }

    override fun onSuccessSign(transaction: BaseTransaction) {
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onError(error: Throwable) {
        showError(error.localizedMessage, R.id.content)
    }

    companion object {
        const val REQUEST_KEEPER_TX_ACTION = 1000
        const val KEY_INTENT_LINK = "key_intent_link"
        const val KEY_INTENT_TRANSACTION_TYPE = "key_intent_transaction_type"
        const val KEY_INTENT_TRANSACTION = "key_intent_transaction"
        const val KEY_INTENT_ASSET_DETAILS = "key_intent_asset_details"
        const val KEY_INTENT_KIND = "key_intent_kind"
        const val KEY_INTENT_CALLBACK = "key_intent_callback"
    }
}