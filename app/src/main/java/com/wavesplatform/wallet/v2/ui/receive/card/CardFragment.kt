package com.wavesplatform.wallet.v2.ui.receive.card

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
import kotlinx.android.synthetic.main.fragment_card.*
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.visiable
import pers.victor.ext.visiableIf
import javax.inject.Inject

class CardFragment :BaseFragment(),CardView{
    @Inject
    @InjectPresenter
    lateinit var presenter: CardPresenter

    @ProvidePresenter
    fun providePresenter(): CardPresenter = presenter

    override fun configLayoutRes(): Int = R.layout.fragment_card

    companion object {
        var REQUEST_SELECT_ASSET = 10003
        /**
         * @return CardFragment instance
         * */
        fun newInstance(): CardFragment {
            return CardFragment()
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
