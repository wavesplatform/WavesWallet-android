package com.wavesplatform.wallet.v2.ui.home.wallet.leasing.start

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.AppCompatTextView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.google.zxing.integration.android.IntentIntegrator
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.auth.qr_scanner.QrCodeScannerActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser
import com.wavesplatform.wallet.v2.ui.home.wallet.leasing.confirmation.ConfirmationLeasingActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.showError
import kotlinx.android.synthetic.main.activity_start_leasing.*
import pers.victor.ext.*
import javax.inject.Inject

class StartLeasingActivity : BaseActivity(), StartLeasingView {

    @Inject
    @InjectPresenter
    lateinit var presenter: StartLeasingPresenter

    @ProvidePresenter
    fun providePresenter(): StartLeasingPresenter = presenter

    companion object {
        var REQUEST_CHOOSE_ADDRESS = 57
        var REQUEST_SCAN_QR_CODE = 52
        var BUNDLE_WAVES = "waves"
        var BUNDLE_AVAILABLE = "available_balance"
        var TOTAL_BALANCE = "100"
    }

    override fun configLayoutRes(): Int = R.layout.activity_start_leasing

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        setupToolbar(toolbar_view, true, getString(R.string.start_leasing_toolbar), R.drawable.ic_toolbar_back_black)

        text_choose_from_address.click {
            launchActivity<AddressBookActivity>(requestCode = REQUEST_CHOOSE_ADDRESS) {
                putExtra(AddressBookActivity.BUNDLE_SCREEN_TYPE, AddressBookActivity.AddressBookScreenType.CHOOSE.type)
            }
        }
        image_view_recipient_action.click {
            IntentIntegrator(this).setRequestCode(REQUEST_SCAN_QR_CODE)
                    .setOrientationLocked(true)
                    .setBeepEnabled(false)
                    .setCaptureActivity(QrCodeScannerActivity::class.java)
                    .initiateScan()
        }

        button_continue.click {
            launchActivity<ConfirmationLeasingActivity> { }
        }

        edit_address.addTextChangedListener {
            after {
                if (edit_address.text.isNullOrEmpty()) {
                    linear_address_suggestions.visiable()
                } else {
                    linear_address_suggestions.gone()
                }
            }
        }

        edit_amount.addTextChangedListener {
            after {
                if (edit_amount.text.isNullOrEmpty()) {
                    linear_amount_suggestions.visiable()
                } else {
                    linear_amount_suggestions.gone()
                }
            }
        }

        afterSuccessLoadWavesBalance(intent.getParcelableExtra<AssetBalance>(BUNDLE_WAVES), intent.getLongExtra(BUNDLE_AVAILABLE, 0L))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_SCAN_QR_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val result = IntentIntegrator.parseActivityResult(resultCode, data)
                    val address = result.contents
                    if (!address.isEmpty()) {
                        edit_address.setText(address)
                    } else {
                        showError(R.string.start_leasing_validation_address_is_invalid_error, R.id.root_view)
                    }
                }
            }
            REQUEST_CHOOSE_ADDRESS -> {
                if (resultCode == Activity.RESULT_OK) {
                    val addressTestObject = data?.getParcelableExtra<AddressBookUser>(AddressBookActivity.BUNDLE_ADDRESS_ITEM)
                    edit_address.setText(addressTestObject?.address)
                }
            }
        }
    }

    override fun afterSuccessLoadWavesBalance(waves: AssetBalance, availableBalance: Long) {
        text_asset_value.text = MoneyUtil.getScaledText(availableBalance, waves)

        linear_quick_balance.children.forEach { children ->
            val quickBalanceView = children as AppCompatTextView
            when (quickBalanceView.tag) {
                TOTAL_BALANCE -> {
                    quickBalanceView.click {
                        edit_amount.setText(MoneyUtil.getScaledText(availableBalance, waves))
                        edit_amount.setSelection(edit_amount.text.length)
                    }
                }
                else -> {
                    val percentBalance = (availableBalance * (quickBalanceView.tag.toString().toDouble().div(100))).toLong()
                    quickBalanceView.text = MoneyUtil.getScaledText(percentBalance, waves)
                    quickBalanceView.click {
                        edit_amount.setText(MoneyUtil.getScaledText(percentBalance, waves))
                        edit_amount.setSelection(edit_amount.text.length)
                    }
                }
            }
        }
    }

}
