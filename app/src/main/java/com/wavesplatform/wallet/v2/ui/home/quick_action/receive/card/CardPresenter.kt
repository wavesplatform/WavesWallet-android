package com.wavesplatform.wallet.v2.ui.home.quick_action.receive.card

import android.text.TextUtils
import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAsSingle
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.manager.CoinomatManager
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import pyxis.uzuki.live.richutilskt.utils.runAsync
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import javax.inject.Inject

@InjectViewState
class CardPresenter @Inject constructor() : BasePresenter<CardView>() {

    @Inject
    lateinit var coinomatManager: CoinomatManager

    private var crypto: String = Constants.WAVES_ASSET_ID_FILLED
    private var address: String? = App.getAccessManager().getWallet()!!.address
    private var amount: String = "0"
    var fiat: String = "USD"
    private var min: Float = 0F
    private var max: Float = 0F
    var asset: AssetBalance? = null
    private var rate = ""

    fun invalidate() {
        viewState.showWaves(asset)
        viewState.showRate(rate)
        viewState.showLimits(min.toString(), max.toString(), fiat)
    }

    fun isValid(): Boolean {
        return !TextUtils.isEmpty(amount) && amount.toFloat() >= min && amount.toFloat() <= max
    }

    fun loadWaves() {
        runAsync {
            val singleData: Single<List<AssetBalance>> = queryAsSingle { equalTo("assetId", "") }
            addSubscription(singleData
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        if (it.size == 1) {
                            runOnUiThread {
                                asset = it[0]
                                viewState.showWaves(it[0])
                            }
                        }
                    }, {
                        viewState.showError(App.getAppContext()
                                .getString(R.string.receive_error_network))
                    }))
        }
    }

    fun fiatChanged(fiat: String) {
        this.fiat = fiat
        loadLimits()
        loadRate()
    }

    fun amountChanged(amount: String) {
        if (!TextUtils.isEmpty(amount)) {
            this.amount = amount
        }
        loadRate()
    }

    private fun loadRate() {
        if (TextUtils.isEmpty(amount) || amount == "0") {
            runOnUiThread {
                viewState.showRate("0")
            }
            return
        }

        runAsync {
            addSubscription(coinomatManager.loadRate(crypto, address, fiat, amount).subscribe({ rate ->
                this.rate = rate
                runOnUiThread {
                    viewState.showRate(rate)
                }
            }, {
                runOnUiThread {
                    viewState.onGatewayError()
                }
            }))
        }
    }

    private fun loadLimits() {
        runAsync {
            addSubscription(coinomatManager.loadLimits(crypto, address, fiat).subscribe({ limits ->
                min = if (limits?.min == null) {
                    0F
                } else {
                    limits.min!!.toFloat()
                }
                max = if (limits?.max == null) {
                    0F
                } else {
                    limits.max!!.toFloat()
                }
                runOnUiThread {
                    viewState.showLimits(limits.min, limits.max, fiat)
                }
            }, {
                runOnUiThread {
                    viewState.showError(App.getAppContext()
                            .getString(R.string.receive_error_network))
                }
            }))
        }
    }

    fun createLink(): String {
        return "https://coinomat.com/api/v2/indacoin/buy.php?" +
                "crypto=$crypto" +
                "&fiat=$fiat" +
                "&address=$address" +
                "&amount=$amount"
    }
}
