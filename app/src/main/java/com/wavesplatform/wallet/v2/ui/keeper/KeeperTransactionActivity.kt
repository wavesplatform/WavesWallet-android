package com.wavesplatform.wallet.v2.ui.keeper

import android.app.Activity
import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.vicpin.krealmextensions.queryAll
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import com.wavesplatform.sdk.model.request.node.TransferTransaction
import com.wavesplatform.sdk.model.response.node.transaction.BaseTransactionResponse
import com.wavesplatform.sdk.model.response.node.transaction.TransferTransactionResponse
import com.wavesplatform.sdk.utils.Identicon
import com.wavesplatform.sdk.utils.SignUtil
import com.wavesplatform.sdk.utils.WavesConstants
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.db.SpamAssetDb
import com.wavesplatform.wallet.v2.data.model.local.TransactionType
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.util.*
import kotlinx.android.synthetic.main.activity_keeper_transaction.*
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

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        setNavigationBarColor(R.color.white)
        setupToolbar(toolbar_view, true,
                getString(R.string.keeper_title_confirm_request), R.drawable.ic_toolbar_back_black)

        if (!App.getAccessManager().isAuthenticated()) {
            return
        }

        button_reject.click {
            finish()
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


        val transaction = TransferTransaction(
                assetId = WavesConstants.WAVES_ASSET_ID_EMPTY,
                recipient = "3PNaua1fMrQm4TArqeTuakmY1u985CgMRk6",
                amount = 1,
                fee = WavesConstants.WAVES_MIN_FEE,
                attachment = SignUtil.textToBase58("Hello-!"),
                feeAssetId = WavesConstants.WAVES_ASSET_ID_EMPTY
        )

        transaction.senderPublicKey = "B3f8VFh6T2NGT26U7rHk2grAxn5zi9iLkg4V9uxG6C8q"
        transaction.timestamp = System.currentTimeMillis()


        val spamSet = hashSetOf<String>()
        queryAll<SpamAssetDb>().forEach {
            spamSet.add(it.assetId ?: "")
        }


        val addressFrom = App.getAccessManager().getWallet()?.address ?: ""

        Glide.with(this)
                .load(Identicon().create(addressFrom))
                .apply(RequestOptions().circleCrop())
                .into(image_address_from)

        text_address_from.text = addressFrom

        Glide.with(this)
                .load(iconUrl)
                .apply(RequestOptions().circleCrop())
                .into(image_address_to)

        text_address_to.text = appName


        val transactionType = getTransactionType(transaction, WavesWallet.getAddress(), spamSet)
        val txType = TransactionType.getTypeById(transactionType)
        image_transaction.setImageResource(txType.image)
        text_transaction_name.text = getText(txType.title)


        if (kind == "sign") {
            button_approve.text = getText(R.string.keeper_sign)
            button_approve.click {
                presenter.signTransaction(transaction)
            }
        } else {
            button_approve.text = getText(R.string.keeper_send)
            button_approve.click {
                presenter.sendTransaction(transaction)
            }
        }
    }

    override fun onSuccessSend(transaction: BaseTransactionResponse) {
        launchActivity<KeeperConfirmTransactionActivity> {
            putExtra(KEY_INTENT_TRANSACTION, transaction)
            putExtra(KEY_INTENT_KIND, kind)
            putExtra(KEY_INTENT_CALLBACK, callback)
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
        const val KEY_INTENT_LINK = "key_intent_link"
        const val KEY_INTENT_TRANSACTION = "key_intent_transaction"
        const val KEY_INTENT_KIND = "key_intent_kind"
        const val KEY_INTENT_CALLBACK = "key_intent_callback"
    }
}