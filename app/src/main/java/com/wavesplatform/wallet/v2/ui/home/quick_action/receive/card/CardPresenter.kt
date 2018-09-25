package com.wavesplatform.wallet.v2.ui.home.quick_action.receive.card

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAsSingle
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.data.manager.CoinomatManager
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import pyxis.uzuki.live.richutilskt.utils.runAsync
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import javax.inject.Inject

@InjectViewState
class CardPresenter @Inject constructor() : BasePresenter<CardView>() {

    @Inject
    lateinit var coinomatManager: CoinomatManager

    private var crypto: String? = "WAVES"
    private var address: String? = App.getAccessManager().getWallet()!!.address
    private var amount: String? = "30"
    private var fiat: String? = "USD"

    fun loadAssets() {
        runAsync {
            addSubscription(queryAsSingle<AssetBalance> {
                equalTo("isFavorite", true)
                        .greaterThan("balance", 0)
            }
                    .compose(RxUtil.applySingleDefaultSchedulers())
                    .subscribe({ assets ->
                        runOnUiThread {
                            viewState.showWaves(assets)
                        }
                    }, {
                        it.printStackTrace()
                    }))
        }
    }

    fun loadWithFiat(fiat: String) {
        this.fiat = fiat
        loadRate(amount)
        loadLimits()
    }

    fun loadRate(amount: String?) {
        this.amount = amount
        runAsync {
            addSubscription(coinomatManager.loadRate(crypto, address, fiat, amount).subscribe({ rate ->
                runOnUiThread {
                    viewState.showRate(rate)
                }
            }, {
                it.printStackTrace()
            }))
        }
    }

    fun loadLimits() {
        runAsync {
            addSubscription(coinomatManager.loadLimits(crypto, address, fiat).subscribe({ limits ->
                runOnUiThread {
                    viewState.showLimits(limits.min, limits.max, fiat)
                }
            }, {
                it.printStackTrace()
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
