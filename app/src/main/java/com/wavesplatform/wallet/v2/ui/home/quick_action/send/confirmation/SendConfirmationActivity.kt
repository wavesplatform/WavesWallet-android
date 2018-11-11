package com.wavesplatform.wallet.v2.ui.home.quick_action.send.confirmation

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.view.animation.AnimationUtils
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.jakewharton.rxbinding2.widget.RxTextView
import com.vicpin.krealmextensions.queryFirst
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.request.TransactionsBroadcastRequest
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.EnterPassCodeActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.add.AddAddressActivity
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.SendPresenter
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.showError
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_send_confirmation.*
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.invisiable
import pers.victor.ext.visiable
import javax.inject.Inject


class SendConfirmationActivity : BaseActivity(), SendConfirmationView {


    @Inject
    @InjectPresenter
    lateinit var presenter: SendConfirmationPresenter

    @ProvidePresenter
    fun providePresenter(): SendConfirmationPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_send_confirmation

    override fun onCreate(savedInstanceState: Bundle?) {
        translucentStatusBar = true
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, true,
                getString(R.string.send_confirmation_toolbar_title),
                R.drawable.ic_toolbar_back_white)

        if (intent == null || intent.extras == null) {
            finish()
        }

        presenter.selectedAsset = intent!!.extras!!.getParcelable(KEY_INTENT_SELECTED_ASSET)
        presenter.recipient = intent!!.extras!!.getString(KEY_INTENT_SELECTED_RECIPIENT)
        presenter.amount = intent!!.extras!!.getString(KEY_INTENT_SELECTED_AMOUNT)
        presenter.moneroPaymentId = intent!!.extras!!.getString(KEY_INTENT_MONERO_PAYMENT_ID)
        presenter.assetInfo = queryFirst { equalTo("id", presenter.selectedAsset!!.assetId) }
        presenter.type = intent!!.extras!!.getSerializable(KEY_INTENT_TYPE) as SendPresenter.Type

        text_sum.text = "- ${presenter.amount}"
        text_tag.text = presenter.selectedAsset!!.getName()
        text_sent_to_address.text = presenter.recipient
        presenter.getAddressName(presenter.recipient!!)
        text_fee_value.text = "${Constants.WAVES_FEE / 100_000_000F} ${Constants.CUSTOM_FEE_ASSET_NAME}"

        button_confirm.click { requestPassCode() }

        eventSubscriptions.add(RxTextView.textChanges(edit_optional_message)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    presenter.attachment = it.toString()
                })

        if (intent.hasExtra(KEY_INTENT_ATTACHMENT)) {
            edit_optional_message.setText(intent!!.extras!!.getString(KEY_INTENT_ATTACHMENT))
        }
    }

    override fun onShowTransactionSuccess(signed: TransactionsBroadcastRequest) {
        completeTransactionProcessing()
        text_leasing_result_value.text = getString(
                R.string.send_success_you_have_sent_sum,
                MoneyUtil.getScaledText(signed.amount, presenter.selectedAsset),
                presenter.getTicker())
        button_okay.click {
            launchActivity<MainActivity>(clear = true)
        }
        setSaveAddress(signed)
    }

    private fun completeTransactionProcessing() {
        image_loader.clearAnimation()
        card_progress.gone()
        relative_success.visiable()
    }

    private fun showTransactionProcessing() {
        toolbar_view.invisiable()
        card_content.gone()
        card_progress.visiable()
        val rotation = AnimationUtils
                .loadAnimation(this@SendConfirmationActivity, R.anim.rotate)
        rotation.fillAfter = true
        image_loader.startAnimation(rotation)
    }

    private fun cancelTransactionProcessing() {
        image_loader.clearAnimation()
        toolbar_view.visiable()
        card_content.visiable()
        card_progress.gone()
        relative_success.gone()
    }

    private fun setSaveAddress(signed: TransactionsBroadcastRequest) {
        val addressBookUser = queryFirst<AddressBookUser> {
            equalTo("address", signed.recipient)
        }
        if (addressBookUser == null) {
            sent_to_address.text = signed.recipient
            add_address.visiable()
            add_address.click {
                launchActivity<AddAddressActivity>(AddressBookActivity.REQUEST_ADD_ADDRESS) {
                    putExtra(AddressBookActivity.BUNDLE_TYPE, AddressBookActivity.SCREEN_TYPE_NOT_EDITABLE)
                    putExtra(AddressBookActivity.BUNDLE_ADDRESS_ITEM, AddressBookUser(signed.recipient, ""))
                }
            }
        } else {
            add_address.gone()
        }
    }

    override fun showAddressBookUser(name: String) {
        if (!TextUtils.isEmpty(name)) {
            text_sent_to_name.text = name
            text_sent_to_name.visiable()
        } else {
            text_sent_to_name.invisiable()
        }
    }

    override fun hideAddressBookUser() {
        text_sent_to_name.invisiable()
    }

    override fun requestPassCode() {
        launchActivity<EnterPassCodeActivity>(
                requestCode = EnterPassCodeActivity.REQUEST_ENTER_PASS_CODE)
    }

    override fun onShowError(res: Int) {
        cancelTransactionProcessing()
        showError(res, R.id.relative_root)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            EnterPassCodeActivity.REQUEST_ENTER_PASS_CODE -> {
                if (resultCode == Constants.RESULT_OK) {
                    showTransactionProcessing()
                    presenter.confirmSend()
                } else {
                    setResult(Constants.RESULT_CANCELED)
                    finish()
                }
            }
        }
    }

    companion object {
        const val KEY_INTENT_SELECTED_ASSET = "intent_selected_asset"
        const val KEY_INTENT_SELECTED_RECIPIENT = "intent_selected_recipient"
        const val KEY_INTENT_SELECTED_AMOUNT = "intent_selected_amount"
        const val KEY_INTENT_ATTACHMENT = "intent_attachment"
        const val KEY_INTENT_MONERO_PAYMENT_ID = "intent_monero_payment_id"
        const val KEY_INTENT_TYPE = "intent_type"
    }
}
