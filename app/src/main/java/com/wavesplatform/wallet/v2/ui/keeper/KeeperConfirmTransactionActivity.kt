package com.wavesplatform.wallet.v2.ui.keeper

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.vicpin.krealmextensions.queryAll
import com.wavesplatform.sdk.keeper.interfaces.KeeperTransaction
import com.wavesplatform.sdk.keeper.interfaces.KeeperTransactionResponse
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.db.SpamAssetDb
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import kotlinx.android.synthetic.main.activity_keeper_confirm_transaction.*
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.visiable
import javax.inject.Inject

class KeeperConfirmTransactionActivity : BaseActivity(), KeeperConfirmTransactionView {

    @Inject
    @InjectPresenter
    lateinit var presenter: KeeperConfirmTransactionPresenter
    private var transaction: KeeperTransaction? = null
    private var spam: HashSet<String> = hashSetOf()

    @ProvidePresenter
    fun providePresenter(): KeeperConfirmTransactionPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_keeper_confirm_transaction

    override fun askPassCode() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        translucentStatusBar = true
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {

    }

    override fun onResume() {
        super.onResume()
        transaction = intent.getParcelableExtra(KeeperTransactionActivity.KEY_INTENT_TRANSACTION)
                as KeeperTransaction

        queryAll<SpamAssetDb>().forEach {
            spam.add(it.assetId ?: "")
        }

        if (transaction == null) {
            onError(Throwable(getString(R.string.common_server_error)))
        } else {
            image_loader.show()
            // transaction_view.setTransaction(transaction)
            presenter.sendTransaction(transaction!!)
        }
    }

    override fun onSuccessSend(transaction: KeeperTransactionResponse) {
        card_progress.gone()
        card_success.visiable()
        image_loader.hide()
        button_okay.click {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    override fun onError(error: Throwable) {
        Toast.makeText(this, error.localizedMessage, Toast.LENGTH_LONG).show()
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}