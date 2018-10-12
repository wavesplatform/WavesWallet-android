package com.wavesplatform.wallet.v2.ui.home.quick_action.receive.cryptocurrency

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.ethanhua.skeleton.Skeleton
import com.ethanhua.skeleton.SkeletonScreen
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.ViewUtils
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.coinomat.GetTunnel
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.address_view.ReceiveAddressViewActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.your_assets.YourAssetsActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.fragment_cryptocurrency.*
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.visiable
import pers.victor.ext.visiableIf
import java.math.BigDecimal
import javax.inject.Inject

class CryptoCurrencyFragment : BaseFragment(), СryptocurrencyView {

    @Inject
    @InjectPresenter
    lateinit var presenter: СryptocurrencyPresenter
    private var skeletonScreen: SkeletonScreen? = null

    @ProvidePresenter
    fun providePresenter(): СryptocurrencyPresenter = presenter

    override fun configLayoutRes(): Int = R.layout.fragment_cryptocurrency

    companion object {

        var REQUEST_SELECT_ASSET = 10001

        fun newInstance(assetBalance: AssetBalance?): CryptoCurrencyFragment {
            val fragment =  CryptoCurrencyFragment()
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
            if (presenter.tunnel != null && presenter.tunnel!!.tunnel != null) {
                launchActivity<ReceiveAddressViewActivity> {
                    putExtra(YourAssetsActivity.BUNDLE_ASSET_ITEM, presenter.assetBalance)
                    putExtra(YourAssetsActivity.BUNDLE_ADDRESS,
                            presenter.tunnel!!.tunnel!!.walletFrom ?: "")
                }
            }
        }
        button_continue.isEnabled = false
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            presenter.assetBalance.notNull {
                skeletonScreen!!.show()
                setAssetBalance(it)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT_ASSET && resultCode == Activity.RESULT_OK) {
            val assetBalance = data?.getParcelableExtra<AssetBalance>(YourAssetsActivity.BUNDLE_ASSET_ITEM)
            setAssetBalance(assetBalance)
        }
    }

    override fun showTunnel(tunnel: GetTunnel?) {
        skeletonScreen!!.hide()
        if (tunnel?.tunnel == null) {
            button_continue.isEnabled = false
            return
        }

        val min = BigDecimal(tunnel.tunnel?.inMin).toPlainString()
        limits.text = getString(R.string.receive_minimum_amount,
                min, tunnel.tunnel?.currencyFrom)
        warning.text = getString(R.string.receive_warning_will_send,
                min,
                tunnel.tunnel?.currencyFrom)
        warning_crypto.text = getString(R.string.receive_warning_crypto, tunnel.tunnel?.currencyFrom)
        button_continue.isEnabled = true
    }

    override fun showError(message: String?) {
        skeletonScreen!!.hide()
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

        if (assetBalance != null) {
            presenter.getTunnel(assetBalance.assetId!!)
            skeletonScreen = Skeleton.bind(container_info)
                    .color(R.color.basic50)
                    .load(R.layout.item_skeleton_crypto_warning)
                    .show()
            container_info.visiable()
        }
    }

    private fun assetChangeEnable(enable: Boolean) {
        if (enable) {
            edit_asset.click {
                launchActivity<YourAssetsActivity>(REQUEST_SELECT_ASSET) {
                    putExtra(YourAssetsActivity.CRYPTO_CURRENCY, true)
                }
            }
            container_asset.click {
                launchActivity<YourAssetsActivity>(REQUEST_SELECT_ASSET) {
                    putExtra(YourAssetsActivity.CRYPTO_CURRENCY, true)
                }
            }
            image_change.visibility = View.VISIBLE
            ViewCompat.setElevation(edit_asset_card, ViewUtils.convertDpToPixel(4f, activity!!))
            edit_asset_layout.background = null
            edit_asset_card.setCardBackgroundColor(ContextCompat.getColor(
                    activity!!, R.color.white))
        } else {
            edit_asset.click {

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
}
