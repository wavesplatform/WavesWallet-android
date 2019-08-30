package com.wavesplatform.wallet.v2.ui.keeper

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.vicpin.krealmextensions.queryAll
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import com.wavesplatform.sdk.model.response.node.AssetsDetailsResponse
import com.wavesplatform.sdk.model.response.node.transaction.BaseTransactionResponse
import com.wavesplatform.sdk.model.response.node.transaction.DataTransactionResponse
import com.wavesplatform.sdk.model.response.node.transaction.InvokeScriptTransactionResponse
import com.wavesplatform.sdk.model.response.node.transaction.TransferTransactionResponse
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.db.SpamAssetDb
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import kotlinx.android.synthetic.main.activity_keeper_confirm_transaction.*
import javax.inject.Inject

class KeeperConfirmTransactionActivity : BaseActivity(), KeeperConfirmTransactionView {

    @Inject
    @InjectPresenter
    lateinit var presenter: KeeperConfirmTransactionPresenter

    @ProvidePresenter
    fun providePresenter(): KeeperConfirmTransactionPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_keeper_confirm_transaction

    override fun askPassCode() = false

    private var kind = ""
    private var callback = ""
    private var transaction: BaseTransactionResponse? = null
    private var assetDetails: AssetsDetailsResponse? = null

    private var spam: HashSet<String> = hashSetOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        translucentStatusBar = true
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {

    }

    override fun onResume() {
        super.onResume()
        val transactionType = intent.getIntExtra(KeeperTransactionActivity.KEY_INTENT_TRANSACTION_TYPE, 0)
        transaction = intent.getParcelableExtra(KeeperTransactionActivity.KEY_INTENT_TRANSACTION)
                as BaseTransactionResponse
        assetDetails = intent.getParcelableExtra(KeeperTransactionActivity.KEY_INTENT_TRANSACTION)
                as AssetsDetailsResponse
        kind = intent.getStringExtra(KeeperTransactionActivity.KEY_INTENT_KIND)
        callback = intent.getStringExtra(KeeperTransactionActivity.KEY_INTENT_CALLBACK)

        queryAll<SpamAssetDb>().forEach {
            spam.add(it.assetId ?: "")
        }

        if (transaction != null) {
            when {
                transaction!!.type == BaseTransaction.TRANSFER -> {
                    transaction as TransferTransactionResponse
                    transaction_view.setTransaction(transaction!!, transactionType, assetDetails!!)
                }
                transaction!!.type == BaseTransaction.DATA -> {
                    transaction as DataTransactionResponse
                }
                transaction!!.type == BaseTransaction.SCRIPT_INVOCATION -> {
                    transaction as InvokeScriptTransactionResponse
                }
                else -> {
                    // do nothing
                }
            }
        }
    }
}