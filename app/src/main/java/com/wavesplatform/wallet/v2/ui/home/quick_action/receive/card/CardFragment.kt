package com.wavesplatform.wallet.v2.ui.home.quick_action.receive.card

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.ethanhua.skeleton.Skeleton
import com.ethanhua.skeleton.SkeletonScreen
import com.jakewharton.rxbinding2.widget.RxTextView
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.success.SuccessActivity
import com.wavesplatform.wallet.v2.util.*
import kotlinx.android.synthetic.main.fragment_card.*
import kotlinx.android.synthetic.main.layout_asset_card.*
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
    private var skeletonScreen: SkeletonScreen? = null

    @ProvidePresenter
    fun providePresenter(): CardPresenter = presenter

    override fun configLayoutRes(): Int = R.layout.fragment_card

    override fun onViewReady(savedInstanceState: Bundle?) {
        presenter.loadWaves()

        button_continue.click {
            if (presenter.isValid()) {
                launchActivity<SuccessActivity> {
                    putExtra(SuccessActivity.KEY_INTENT_TITLE, getString(R.string.coinomat_success_title))
                    putExtra(SuccessActivity.KEY_INTENT_SUBTITLE, getString(R.string.coinomat_success_subtitle))
                }
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(presenter.createLink()))
                startActivity(browserIntent)
            } else {
                showError(getString(R.string.receive_error_amount))
            }
        }

        edit_amount.applyFilterStartWithDot()

        eventSubscriptions.add(RxTextView.textChanges(edit_amount)
                .debounce(300, TimeUnit.MILLISECONDS)
                .subscribe { string ->
                    presenter.amountChanged(string.toString())
                })

        fiat_change.click {
            showDialogFiatChange()
        }

        skeletonScreen = Skeleton.bind(attention_title)
                .color(R.color.basic50)
                .load(R.layout.item_skeleton_limits)
                .show()

        setFiat(USD)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState == null) {
            presenter.loadWaves()
            setFiat(USD)
        } else {
            presenter.invalidate()
        }
    }

    override fun showWaves(asset: AssetBalance?) {
        asset.notNull { setAssetBalance(it) }
    }

    override fun showRate(rate: String?) {
        if (rate.isNullOrEmpty()) {
            text_amount_in_dollar.text = ""
        } else {
            text_amount_in_dollar.text = "≈ ${String.format("%.8f", rate.toDouble())} WAVES"
        }
    }

    override fun showLimits(min: String?, max: String?, fiat: String?) {
        val currency = getCurrency(fiat)
        skeletonScreen!!.hide()
        if (min != null && max != null) {
            attention_title.text = getString(R.string.receive_limit, min, currency, max, currency)
            button_continue.isEnabled = true
        }
    }

    override fun showError(message: String) {
        skeletonScreen!!.hide()
        showError(message, R.id.content)
    }

    override fun onGatewayError() {
        skeletonScreen!!.hide()
        attention_title.text = getString(R.string.send_gateway_error_title)
        attention_subtitle.text = getString(R.string.send_gateway_error_subtitle)
        button_continue.isEnabled = false
    }

    private fun assetChangeDisable() {
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

    private fun setAssetBalance(assetBalance: AssetBalance?) {
        image_asset_icon.setAsset(assetBalance)
        text_asset_name.text = assetBalance?.getName()
        text_asset_value.text = assetBalance?.getDisplayAvailableBalance()

        image_is_favourite.visiableIf {
            assetBalance?.isFavorite!!
        }

        text_asset.gone()
        container_asset.visiable()
        container_info.visiable()
        button_continue.isEnabled = presenter.asset != null
        assetChangeDisable()
    }

    private fun showDialogFiatChange() {
        val alertDialog = AlertDialog.Builder(baseActivity).create()
        alertDialog.setTitle(getString(R.string.receive_fiat_choose_dialog_title))
        val view = LayoutInflater.from(baseActivity)
                .inflate(R.layout.receive_fiat_choose_dialog, null)
        val usdButton = view.findViewById<RadioButton>(R.id.radioButton_usd)
        val euroButton = view.findViewById<RadioButton>(R.id.radioButton_eur)
        var currency = presenter.fiat

        if (presenter.fiat == USD) {
            usdButton.isChecked = true
        } else {
            euroButton.isChecked = true
        }

        usdButton.click { currency = USD }
        euroButton.click { currency = EURO }

        alertDialog.setView(view)
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,
                getString(R.string.receive_fiat_choose_dialog_cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,
                getString(R.string.receive_fiat_choose_dialog_ok)) { dialog, _ ->
            dialog.dismiss()
            setFiat(currency)
        }
        alertDialog.show()
        alertDialog.makeStyled()
    }

    private fun setFiat(value: String) {
        amount_title.text = getString(R.string.receive_amount_title, getCurrency(value))
        presenter.fiatChanged(value)
        skeletonScreen!!.show()
    }

    private fun getCurrency(value: String?): String {
        if (value.isNullOrEmpty()) {
            return ""
        }

        return when (value) {
            USD -> getString(R.string.receive_fiat_choose_dialog_usd)
            EURO -> getString(R.string.receive_fiat_choose_dialog_euro)
            else -> value!!
        }
    }

    override fun onNetworkConnectionChanged(networkConnected: Boolean) {
        super.onNetworkConnectionChanged(networkConnected)
        button_continue.isEnabled = presenter.asset != null && networkConnected
    }

    companion object {

        const val USD = "USD"
        const val EURO = "EURO"

        fun newInstance(): CardFragment {
            return CardFragment()
        }
    }
}
