package com.wavesplatform.wallet.v2.ui.keeper

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.vicpin.krealmextensions.queryAll
import com.wavesplatform.sdk.model.request.node.*
import com.wavesplatform.sdk.model.response.node.AssetsDetailsResponse
import com.wavesplatform.sdk.model.response.node.HistoryTransactionResponse
import com.wavesplatform.sdk.model.response.node.TransferResponse
import com.wavesplatform.sdk.model.response.node.transaction.BaseTransactionResponse
import com.wavesplatform.sdk.model.response.node.transaction.DataTransactionResponse
import com.wavesplatform.sdk.model.response.node.transaction.InvokeScriptTransactionResponse
import com.wavesplatform.sdk.model.response.node.transaction.TransferTransactionResponse
import com.wavesplatform.sdk.utils.*
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.db.SpamAssetDb
import com.wavesplatform.wallet.v2.data.model.local.TransactionType
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.util.*
import kotlinx.android.synthetic.main.activity_keeper_transaction.*
import kotlinx.android.synthetic.main.view_keeper_transaction.*

import pers.victor.ext.click
import javax.inject.Inject

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

    var transaction: TransferTransaction? = null
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
        link = intent.getStringExtra(KEY_INTENT_LINK)


        getTransaction()


        if (transaction == null) {
            finish()
            return
        }


        presenter.receiveAsset(transaction!!.assetId)




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


        val transactionType = getTransactionType(transaction!!, WavesWallet.getAddress(), spam)
        val txType = TransactionType.getTypeById(transactionType)
        image_transaction.setImageResource(txType.image)
        text_transaction_name.text = getText(txType.title)


        if (kind == "sign") {
            button_approve.text = getText(R.string.keeper_sign)
            button_approve.click {
                presenter.signTransaction(transaction!!)
            }
        } else {
            button_approve.text = getText(R.string.keeper_send)
            button_approve.click {
                presenter.sendTransaction(transaction!!)
            }
        }

        button_reject.click {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun getTransaction() {
        transaction = TransferTransaction(
                assetId = WavesConstants.WAVES_ASSET_ID_EMPTY,
                recipient = "3PNaua1fMrQm4TArqeTuakmY1u985CgMRk6",
                amount = 1,
                fee = WavesConstants.WAVES_MIN_FEE,
                attachment = SignUtil.textToBase58("Hello-!"),
                feeAssetId = WavesConstants.WAVES_ASSET_ID_EMPTY
        )
        transaction!!.senderPublicKey = "B3f8VFh6T2NGT26U7rHk2grAxn5zi9iLkg4V9uxG6C8q"
        transaction!!.timestamp = System.currentTimeMillis()
    }

    override fun onSuccessSend(transaction: BaseTransactionResponse) {
        when {
            transaction.type == BaseTransaction.TRANSFER -> {
                transaction as TransferTransactionResponse
                launchActivity<KeeperConfirmTransactionActivity> {
                    putExtra(KEY_INTENT_TRANSACTION, transaction)
                    putExtra(KEY_INTENT_KIND, kind)
                    putExtra(KEY_INTENT_CALLBACK, callback)
                }
            }
            transaction.type == BaseTransaction.DATA -> {
                transaction as DataTransactionResponse
                launchActivity<KeeperConfirmTransactionActivity> {
                    putExtra(KEY_INTENT_TRANSACTION, transaction)
                    putExtra(KEY_INTENT_KIND, kind)
                    putExtra(KEY_INTENT_CALLBACK, callback)
                }
            }
            transaction.type == BaseTransaction.SCRIPT_INVOCATION -> {
                transaction as InvokeScriptTransactionResponse
                launchActivity<KeeperConfirmTransactionActivity> {
                    putExtra(KEY_INTENT_TRANSACTION, transaction)
                    putExtra(KEY_INTENT_KIND, kind)
                    putExtra(KEY_INTENT_CALLBACK, callback)
                }
            }
            else -> {
                // do nothing
            }
        }
    }

    override fun onReceivedAsset(asset: AssetsDetailsResponse) {
        setTransactionView(transaction!!, asset, spam)
    }

    override fun onSuccessSign(transaction: BaseTransaction) {
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onError(error: Throwable) {
        showError(error.localizedMessage, R.id.content)
    }
    
    private fun setTransactionView(transaction: BaseTransaction,
                                   asset: AssetsDetailsResponse? = null,
                                   spams: HashSet<String>? = null) {
        image_transaction
        text_transaction_name
        text_transaction_title

        val transactionType = getTransactionType(transaction, WavesWallet.getAddress(), spams)
        val txType = TransactionType.getTypeById(transactionType)
        image_transaction.setImageResource(txType.image)
        text_transaction_name.text = getText(txType.title)


        val decimals = 8
        
        when (txType) {
            TransactionType.SENT_TYPE -> {
                transaction as TransferTransaction
                transaction.amount.notNull {
                    text_transaction_title.text =
                            "-${getScaledAmount(it, decimals)}"
                }
            }
            TransactionType.RECEIVED_TYPE -> {
                transaction as TransferTransaction
                transaction.amount.notNull {
                    text_transaction_title.text =
                            "+${getScaledAmount(it, decimals)}"
                }
            }
            TransactionType.RECEIVE_SPONSORSHIP_TYPE -> {
                transaction as SponsorshipTransaction
                transaction.fee.notNull {
                    asset.notNull { assetNonNull ->
                        text_transaction_title.text =
                                "+${getScaledAmount(it, assetNonNull.decimals)} ${assetNonNull.name}"
                    }
                }
            }
            TransactionType.MASS_SPAM_RECEIVE_TYPE,
            TransactionType.MASS_RECEIVE_TYPE,
            TransactionType.MASS_SEND_TYPE -> {
                transaction as MassTransferTransaction

                val list = mutableListOf<TransferResponse>()
                transaction.transfers.forEach {
                    list.add(TransferResponse(
                            recipient = it.recipient,
                            amount = it.amount))
                }

                val massTransaction = HistoryTransactionResponse(
                        type = transaction.type,
                        amount = transaction.totalAmount ?: 0,
                        transfers = list)

                text_transaction_title.text = getTransactionAmount(
                        transaction = massTransaction, decimals = decimals)
            }
            TransactionType.CREATE_ALIAS_TYPE -> {
                transaction as AliasTransaction
                text_transaction_title.text = transaction.alias
                text_transaction_title.setTypeface(null, Typeface.BOLD)
            }
            TransactionType.EXCHANGE_TYPE -> {
                //setExchangeItem(item.data, view)
            }
            TransactionType.CANCELED_LEASING_TYPE -> {
                transaction as LeaseCancelTransaction
                asset.notNull {
                   // text_transaction_title.text = getScaledAmount(transaction., it.decimals)
                }
            }
            TransactionType.TOKEN_BURN_TYPE -> {
                transaction as BurnTransaction
                transaction.quantity.notNull {
                    text_transaction_title.text =
                            "-${getScaledAmount(transaction.quantity, decimals)}"
                }
            }
            TransactionType.TOKEN_GENERATION_TYPE,
            TransactionType.TOKEN_REISSUE_TYPE -> {
                transaction as ReissueTransaction
                val quantity = getScaledAmount(transaction.quantity, decimals)
                text_transaction_title.text = "+$quantity"
            }
            TransactionType.DATA_TYPE,
            TransactionType.SET_ADDRESS_SCRIPT_TYPE,
            TransactionType.CANCEL_ADDRESS_SCRIPT_TYPE,
            TransactionType.SCRIPT_INVOCATION_TYPE,
            TransactionType.UPDATE_ASSET_SCRIPT_TYPE -> {
                text_transaction_name.text = getString(R.string.history_data_type_title)
                text_transaction_title.text = getString(txType.title)
                text_transaction_title.setTypeface(null, Typeface.BOLD)
            }
            TransactionType.SET_SPONSORSHIP_TYPE,
            TransactionType.CANCEL_SPONSORSHIP_TYPE -> {
                transaction as SponsorshipTransaction
                asset.notNull {
                    text_transaction_title.text = it.name
                }
            }
            else -> {
                text_transaction_name.text = getString(R.string.history_data_type_title)
                text_transaction_title.text = getString(txType.title)
                text_transaction_title.setTypeface(null, Typeface.BOLD)
            }
        }
    }

    companion object {
        const val REQUEST_KEEPER_TX_ACTION = 1000
        const val KEY_INTENT_LINK = "key_intent_link"
        const val KEY_INTENT_TRANSACTION = "key_intent_transaction"
        const val KEY_INTENT_KIND = "key_intent_kind"
        const val KEY_INTENT_CALLBACK = "key_intent_callback"
    }
}