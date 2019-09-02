package com.wavesplatform.wallet.v2.ui.keeper

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.vicpin.krealmextensions.queryAll
import com.wavesplatform.sdk.keeper.interfaces.KeeperTransaction
import com.wavesplatform.sdk.model.request.node.DataTransaction
import com.wavesplatform.sdk.model.request.node.InvokeScriptTransaction
import com.wavesplatform.sdk.model.request.node.TransferTransaction
import com.wavesplatform.sdk.model.response.node.AssetsDetailsResponse
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.db.SpamAssetDb
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
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
    private var transaction: KeeperTransaction? = null
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
                as KeeperTransaction
        assetDetails = intent.getParcelableExtra(KeeperTransactionActivity.KEY_INTENT_TRANSACTION)
                as AssetsDetailsResponse
        kind = intent.getStringExtra(KeeperTransactionActivity.KEY_INTENT_KIND)
        callback = intent.getStringExtra(KeeperTransactionActivity.KEY_INTENT_CALLBACK)

        queryAll<SpamAssetDb>().forEach {
            spam.add(it.assetId ?: "")
        }

        if (transaction != null) {
            when (transaction) {
                is TransferTransaction -> {
                    //transaction_view.setTransaction(transaction!!, transactionType, assetDetails!!)
                }
                is DataTransaction -> {
                    transaction as DataTransaction
                }
                is InvokeScriptTransaction -> {
                    transaction as InvokeScriptTransaction
                }
                else -> {
                    // do nothing
                }
            }
        }
    }
}