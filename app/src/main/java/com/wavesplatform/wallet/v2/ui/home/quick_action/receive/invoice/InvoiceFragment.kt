package com.wavesplatform.wallet.v2.ui.home.quick_action.receive.invoice

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.jakewharton.rxbinding2.widget.RxTextView
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.address_view.ReceiveAddressViewActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.your_assets.YourAssetsActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.notNull
import com.wavesplatform.wallet.v2.util.showError
import kotlinx.android.synthetic.main.fragment_invoice.*
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.visiable
import pers.victor.ext.visiableIf
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class InvoiceFragment : BaseFragment(), InvoiceView {

    @Inject
    @InjectPresenter
    lateinit var presenter: InvoicePresenter

    @ProvidePresenter
    fun providePresenter(): InvoicePresenter = presenter

    override fun configLayoutRes(): Int = R.layout.fragment_invoice

    override fun showRate(rate: String?) {
        text_amount_in_dollar.text = "≈ $rate USD"
    }

    override fun showError(message: String) {
        showError(message, R.id.content)
    }

    companion object {
        const val REQUEST_SELECT_ASSET = 10001
        const val INVOICE_SCREEN = "invoice"

        fun newInstance(assetBalance: AssetBalance?): InvoiceFragment {
            val fragment =  InvoiceFragment()
            if (assetBalance == null) {
                return fragment
            }
            val args = Bundle()
            args.putParcelable(YourAssetsActivity.BUNDLE_ASSET_ITEM, assetBalance)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        if (arguments == null) {
            edit_asset.click {
                launchActivity<YourAssetsActivity>(REQUEST_SELECT_ASSET) { }
            }
            container_asset.click {
                launchActivity<YourAssetsActivity>(REQUEST_SELECT_ASSET) { }
            }
        } else {
            val assetBalance = arguments!!.getParcelable<AssetBalance>(
                    YourAssetsActivity.BUNDLE_ASSET_ITEM)
            setAssetBalance(assetBalance)
        }

        text_use_total_balance.click {
            presenter.assetBalance.notNull {
                edit_amount.setText(it.getDisplayBalance())
            }
        }
        text_leasing_0_100.click {
            edit_amount.setText("0.100")
        }
        text_leasing_0_100000.click {
            edit_amount.setText("0.00100000")
        }
        text_leasing_0_500000.click {
            edit_amount.setText("0.00500000")
        }

        button_continue.click {
            launchActivity<ReceiveAddressViewActivity> {
                putExtra(YourAssetsActivity.BUNDLE_ASSET_ITEM, presenter.assetBalance)
                putExtra(INVOICE_SCREEN, true)
                putExtra(ReceiveAddressViewActivity.KEY_INTENT_QR_DATA, createLink())
            }
        }

        RxTextView.textChanges(edit_amount)
                .debounce(300, TimeUnit.MILLISECONDS)
                .subscribe { string ->
                    if (TextUtils.isEmpty(string)) {
                        //showRate("0")
                    } else {
                        val value = string.toString()
                        if (value.toDouble() > 0) {
                            presenter.amountChanged(value)
                        } else {
                            //showRate("0")
                        }
                    }
                }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        presenter.assetBalance.notNull {
            setAssetBalance(it)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT_ASSET && resultCode == Activity.RESULT_OK) {
            val assetBalance = data?.getParcelableExtra<AssetBalance>(YourAssetsActivity.BUNDLE_ASSET_ITEM)
            setAssetBalance(assetBalance)
        }
    }

    private fun setAssetBalance(assetBalance: AssetBalance?) {
        presenter.assetBalance = assetBalance

        image_asset_icon.isOval = true
        image_asset_icon.setAsset(assetBalance)
        text_asset_name.text = assetBalance?.getName()
        text_asset_value.text = assetBalance?.getDisplayBalance()

        image_is_favourite.visiableIf {
            assetBalance?.isFavorite!!
        }

        edit_asset.gone()
        container_asset.visiable()

        button_continue.isEnabled = true
    }

    private fun createLink(): String {
        return "https//client.wavesplatform.com/#send/${presenter.assetBalance!!.assetId}?" +
                "recipient=${presenter.address}&" +
                "amount=${presenter.amount}"
    }
}
