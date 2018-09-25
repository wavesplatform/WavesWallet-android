package com.wavesplatform.wallet.v2.ui.home.quick_action.receive.card

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAsSingle
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

    fun loadRate(crypto: String?, address: String?, fiat: String?, amount: String?) {
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

    fun loadLimits(crypto: String?, address: String?, fiat: String?) {
        runAsync {
            addSubscription(coinomatManager.loadLimits(crypto, address, fiat).subscribe({ limits ->
                runOnUiThread {
                    viewState.showLimits(limits.min, limits.max)
                }
            }, {
                it.printStackTrace()
            }))
        }
    }
}
