package com.wavesplatform.wallet.v2.ui.home.quick_action.receive.card

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.jakewharton.rxbinding2.widget.RxTextView
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.web.WebActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.fragment_card.*
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.visiable
import pers.victor.ext.visiableIf
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class CardFragment : BaseFragment(), CardView {
    @Inject
    @InjectPresenter
    lateinit var presenter: CardPresenter

    private var crypto: String? = null
    private var address: String? = null
    private var amount: String? = null
    private var fiat: String? = null

    @ProvidePresenter
    fun providePresenter(): CardPresenter = presenter

    override fun configLayoutRes(): Int = R.layout.fragment_card

    companion object {

        fun newInstance(): CardFragment {
            return CardFragment()
        }
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        crypto = "WAVES"
        address = App.getAccessManager().getWallet()!!.address
        amount = edit_amount.text.toString()
        fiat = "USD"

        edit_asset.isEnabled = false

        button_continue.click {
            val link = "https://coinomat.com/api/v2/indacoin/buy.php?" +
                    "crypto=$crypto" +
                    "&fiat=$fiat" +
                    "&address=$address" +
                    "&amount=$amount"
            launchActivity<WebActivity> {
                putExtra(WebActivity.KEY_INTENT_TITLE, "Coinomat.com")
                putExtra(WebActivity.KEY_INTENT_LINK, link)
            }
        }

        RxTextView.textChanges(edit_amount)
                .filter { charSequence -> charSequence.length > 1 }
                .debounce(300, TimeUnit.MILLISECONDS)
                .subscribe { string ->
                    amount = edit_amount.text.toString()
                    presenter.loadRate(crypto, address, fiat, amount)
                }

        presenter.loadAssets()
        presenter.loadRate(crypto, address, fiat, amount)
        presenter.loadLimits(crypto, address, fiat)
    }

    override fun showWaves(assets: List<AssetBalance>?) {
        setAssetBalance(assets?.get(0))
    }

    override fun showRate(rate: String?) {
        text_amount_in_dollar.text = "≈ $rate WAVES"
    }

    override fun showLimits(min: String?, max: String?) {
        if (min != null && max != null) {
            limits.text = getString(R.string.receive_limits, min, max)
        }
    }

    private fun setAssetBalance(assetBalance: AssetBalance?) {
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
