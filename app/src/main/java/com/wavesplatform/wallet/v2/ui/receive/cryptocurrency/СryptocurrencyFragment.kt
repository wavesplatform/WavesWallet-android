package com.wavesplatform.wallet.v2.ui.receive.cryptocurrency

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.your_assets.YourAssetsActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.fragment_cryptocurrency.*
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.visiable
import pers.victor.ext.visiableIf
import javax.inject.Inject

class СryptocurrencyFragment : BaseFragment(), СryptocurrencyView {

    @Inject
    @InjectPresenter
    lateinit var presenter: СryptocurrencyPresenter

    @ProvidePresenter
    fun providePresenter(): СryptocurrencyPresenter = presenter

    override fun configLayoutRes(): Int = R.layout.fragment_cryptocurrency

    companion object {

        var REQUEST_SELECT_ASSET = 10001

        /**
         * @return СryptocurrencyFragment instance
         * */
        fun newInstance(): СryptocurrencyFragment {
            return СryptocurrencyFragment()
        }
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
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

            image_asset_icon.isOval = true
            image_asset_icon.setAsset(assetBalance)
            text_asset_name.text = assetBalance?.getName()
            text_asset_value.text = assetBalance?.getDisplayBalance()

            image_is_favourite.visiableIf {
                assetBalance?.isFavorite!!
            }

            edit_asset.gone()
            container_asset.visiable()
            container_info.visiable()

            button_continue.isEnabled = true
        }
    }


}
