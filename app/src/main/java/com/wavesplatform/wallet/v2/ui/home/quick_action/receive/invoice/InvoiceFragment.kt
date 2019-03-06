package com.wavesplatform.wallet.v2.ui.home.quick_action.receive.invoice

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.text.TextUtils
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.address_view.ReceiveAddressViewActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.your_assets.YourAssetsActivity
import com.wavesplatform.wallet.v2.util.applyFilterStartWithDot
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.fragment_invoice.*
import kotlinx.android.synthetic.main.layout_asset_card.*
import pers.victor.ext.click
import pers.victor.ext.dp2px
import pers.victor.ext.gone
import pers.victor.ext.visiable
import javax.inject.Inject

class InvoiceFragment : BaseFragment(), InvoiceView {

    @Inject
    @InjectPresenter
    lateinit var presenter: InvoicePresenter

    @ProvidePresenter
    fun providePresenter(): InvoicePresenter = presenter

    override fun configLayoutRes(): Int = R.layout.fragment_invoice

    companion object {
        const val REQUEST_SELECT_ASSET = 10001
        const val INVOICE_SCREEN = "invoice"

        fun newInstance(assetBalance: AssetBalance?): InvoiceFragment {
            val fragment = InvoiceFragment()
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
            assetChangeEnable(true)
        } else {
            val assetBalance = arguments!!.getParcelable<AssetBalance>(
                    YourAssetsActivity.BUNDLE_ASSET_ITEM)
            setAssetBalance(assetBalance)
            assetChangeEnable(false)
        }

        button_continue.click {
            launchActivity<ReceiveAddressViewActivity> {
                putExtra(YourAssetsActivity.BUNDLE_ASSET_ITEM, presenter.assetBalance)
                putExtra(YourAssetsActivity.BUNDLE_ADDRESS, App.getAccessManager().getWallet()?.address ?: "")
                putExtra(INVOICE_SCREEN, true)
                putExtra(ReceiveAddressViewActivity.KEY_INTENT_QR_DATA, createLink())
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
        if (assetBalance == null) {
            return
        }

        presenter.assetBalance = assetBalance

        image_asset_icon.setAsset(assetBalance)
        text_asset_name.text = assetBalance.getName()
        text_asset_value.text = assetBalance.getDisplayAvailableBalance()

        image_is_favourite.visibility = if (assetBalance.isFavorite) {
            View.VISIBLE
        } else {
            View.GONE
        }

        edit_amount.applyFilterStartWithDot()

        text_asset.gone()
        container_asset.visiable()

        button_continue.isEnabled = presenter.assetBalance != null
    }

    private fun createLink(): String {
        val amount = if (TextUtils.isEmpty(edit_amount.text)) {
            "0"
        } else {
            edit_amount.text.toString()
        }

        val assetId = if (presenter.assetBalance?.assetId.isNullOrEmpty()) {
            Constants.WAVES_ASSET_ID_FILLED
        } else {
            presenter.assetBalance!!.assetId!!
        }

        return "https://client.wavesplatform.com/#send/$assetId?" +
                "recipient=${presenter.address}&" +
                "amount=$amount"
    }

    private fun assetChangeEnable(enable: Boolean) {
        if (enable) {
            text_asset.click {
                launchAssets()
            }
            container_asset.click {
                launchAssets()
            }
            image_change.visibility = View.VISIBLE
            ViewCompat.setElevation(edit_asset_card, dp2px(2).toFloat())
            edit_asset_layout.background = null
            edit_asset_card.setCardBackgroundColor(ContextCompat.getColor(
                    activity!!, R.color.white))
        } else {
            text_asset.click {
            }
            container_asset.click {
            }
            image_change.visibility = View.GONE
            ViewCompat.setElevation(edit_asset_card, 0F)
            edit_asset_layout.background = ContextCompat.getDrawable(
                    activity!!, R.drawable.shape_rect_bordered_accent50)
            edit_asset_card.setCardBackgroundColor(ContextCompat.getColor(
                    activity!!, R.color.basic50))
        }
    }

    private fun launchAssets() {
        launchActivity<YourAssetsActivity>(requestCode = REQUEST_SELECT_ASSET) {
            presenter.assetBalance.notNull {
                putExtra(YourAssetsActivity.BUNDLE_ASSET_ID, it.assetId)
            }
        }
    }

    override fun onNetworkConnectionChanged(networkConnected: Boolean) {
        super.onNetworkConnectionChanged(networkConnected)
        button_continue.isEnabled = presenter.assetBalance != null && networkConnected
    }
}
