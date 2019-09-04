package com.wavesplatform.wallet.v2.ui.keeper

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.google.gson.Gson
import com.wavesplatform.sdk.keeper.interfaces.KeeperTransactionResponse
import com.wavesplatform.sdk.keeper.model.KeeperIntentResult
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import com.wavesplatform.sdk.model.request.node.DataTransaction
import com.wavesplatform.sdk.model.request.node.InvokeScriptTransaction
import com.wavesplatform.sdk.model.request.node.TransferTransaction
import com.wavesplatform.sdk.model.response.node.AssetsDetailsResponse
import com.wavesplatform.sdk.model.response.node.transaction.DataTransactionResponse
import com.wavesplatform.sdk.model.response.node.transaction.InvokeScriptTransactionResponse
import com.wavesplatform.sdk.model.response.node.transaction.TransferTransactionResponse
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import kotlinx.android.synthetic.main.activity_keeper_send_transaction.*
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.visiable
import java.io.Serializable
import javax.inject.Inject

class KeeperSendTransactionActivity : BaseActivity(), KeeperSendTransactionView {

    @Inject
    @InjectPresenter
    lateinit var presenter: KeeperSendTransactionPresenter

    @ProvidePresenter
    fun providePresenter(): KeeperSendTransactionPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_keeper_send_transaction

    override fun askPassCode() = true

    override fun needToShowNetworkMessage() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        translucentStatusBar = true
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        image_loader.show()

        presenter.transaction = intent.getParcelableExtra(KeeperTransactionActivity.KEY_INTENT_TRANSACTION)
        when (presenter.transaction) {
            is TransferTransaction -> {
                presenter.receiveAssetDetails((presenter.transaction as TransferTransaction).assetId)
            }
            is DataTransaction, is InvokeScriptTransaction -> {
                transaction_view.setTransaction(presenter.transaction!!)
            }
            else -> {
                onError(Throwable("Wrong transaction type!"))
            }
        }
        presenter.sendTransaction(presenter.transaction!!)
    }

    override fun onSuccessSend(transaction: KeeperTransactionResponse) {
        card_progress.gone()
        card_success.visiable()
        image_loader.hide()
        button_okay.click {
            val data = Intent()
            when (transaction) {
                is TransferTransactionResponse -> {
                    data.putExtra(KeeperTransactionActivity.KEY_INTENT_RESPONSE_TRANSACTION,
                            transaction)
                }
                is DataTransactionResponse -> {
                    data.putExtra(KeeperTransactionActivity.KEY_INTENT_RESPONSE_TRANSACTION, transaction)
                }
                is InvokeScriptTransactionResponse -> {
                    data.putExtra(KeeperTransactionActivity.KEY_INTENT_RESPONSE_TRANSACTION,
                            transaction)
                }
            }
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }

    override fun onError(error: Throwable) {
        Toast.makeText(this, error.localizedMessage, Toast.LENGTH_LONG).show()
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun onReceiveAssetDetails(assetDetails: AssetsDetailsResponse) {
        transaction_view.setTransaction(presenter.transaction!!, assetDetails)
    }
}