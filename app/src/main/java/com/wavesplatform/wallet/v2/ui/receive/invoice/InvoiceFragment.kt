package com.wavesplatform.wallet.v2.ui.receive.invoice

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.receive.cryptocurrency.СryptocurrencyFragment.Companion.REQUEST_SELECT_ASSET
import com.wavesplatform.wallet.v2.ui.your_assets.YourAssetsActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.fragment_invoice.*
import pers.victor.ext.*
import javax.inject.Inject

class InvoiceFragment : BaseFragment(), InvoiceView {
    @Inject
    @InjectPresenter
    lateinit var presenter: InvoicePresenter

    @ProvidePresenter
    fun providePresenter(): InvoicePresenter = presenter

    override fun configLayoutRes(): Int = R.layout.fragment_invoice

    companion object {
        var REQUEST_SELECT_ASSET = 10002
        /**
         * @return InvoiceFragment instance
         * */
        fun newInstance(): InvoiceFragment {
            return InvoiceFragment()
        }
    }

    override fun onViewReady(savedInstanceState: Bundle?) {

        text_use_total_balance.click {
            toast("Total balance")
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

        edit_asset.click {
            launchActivity<YourAssetsActivity>(REQUEST_SELECT_ASSET) { }
        }
        container_asset.click {
            launchActivity<YourAssetsActivity>(REQUEST_SELECT_ASSET) { }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT_ASSET && resultCode == Activity.RESULT_OK) {
            val assetBalance = data?.getParcelableExtra<AssetBalance>(YourAssetsActivity.BUNDLE_ASSET_ITEM)

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
    }
}
