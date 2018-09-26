package com.wavesplatform.wallet.v2.ui.home.quick_action.receive.card

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.widget.RadioButton
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.jakewharton.rxbinding2.widget.RxTextView
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.web.WebActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.makeStyled
import com.wavesplatform.wallet.v2.util.notNull
import com.wavesplatform.wallet.v2.util.showError
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

    @ProvidePresenter
    fun providePresenter(): CardPresenter = presenter

    override fun configLayoutRes(): Int = R.layout.fragment_card

    override fun onViewReady(savedInstanceState: Bundle?) {
        edit_asset.isEnabled = false
        button_continue.click {
            if (presenter.isValid()) {
                launchActivity<WebActivity> {
                    putExtra(WebActivity.KEY_INTENT_TITLE, "Coinomat.com")
                    putExtra(WebActivity.KEY_INTENT_LINK, presenter.createLink())
                }
            } else {
                showError(getString(R.string.receive_error_amount))
            }
        }

        RxTextView.textChanges(edit_amount)
                .debounce(300, TimeUnit.MILLISECONDS)
                .subscribe { string ->
                    presenter.amountChanged(string.toString())
                }

        fiat_change.click {
            showDialogFiatChange()
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState == null) {
            presenter.loadAssets()
            setFiat(USD)
        } else {
            presenter.invalidate()
        }
    }

    override fun showWaves(asset: AssetBalance?) {
        asset.notNull { setAssetBalance(it) }
    }

    override fun showRate(rate: String?) {
        text_amount_in_dollar.text = "≈ $rate WAVES"
    }

    override fun showLimits(min: String?, max: String?, fiat: String?) {
        if (min != null && max != null) {
            limits.text = getString(R.string.receive_limit, min, fiat, max, fiat)
        }
    }

    override fun showError(message: String) {
        showError(message, R.id.content)
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

    private fun showDialogFiatChange() {
        val alertDialog = AlertDialog.Builder(baseActivity).create()
        alertDialog.setTitle(getString(R.string.receive_fiat_choose_dialog_title))
        val view = LayoutInflater.from(baseActivity)
                .inflate(R.layout.receive_fiat_choose_dialog, null)
        view.findViewById<RadioButton>(R.id.radioButton_usd).click {
            alertDialog.dismiss()
            setFiat(USD)
        }
        view.findViewById<RadioButton>(R.id.radioButton_eur).click {
            alertDialog.dismiss()
            setFiat(EURO)
        }
        alertDialog.setView(view)
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,
                getString(R.string.receive_fiat_choose_dialog_cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        alertDialog.show()
        alertDialog.makeStyled()
    }

    private fun setFiat(value: String) {
        amount_title.text = getString(R.string.receive_amount_title, value)
        presenter.fiatChanged(value)
    }

    companion object {

        const val USD = "USD"
        const val EURO = "EURO"

        fun newInstance(): CardFragment {
            return CardFragment()
        }
    }
}
