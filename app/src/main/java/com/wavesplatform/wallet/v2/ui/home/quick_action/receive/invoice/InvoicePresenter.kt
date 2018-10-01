package com.wavesplatform.wallet.v2.ui.home.quick_action.receive.invoice

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAsSingle
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.manager.CoinomatManager
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetInfo
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.card.CardFragment
import io.reactivex.Single
import pyxis.uzuki.live.richutilskt.utils.runAsync
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import javax.inject.Inject

@InjectViewState
class InvoicePresenter @Inject constructor() :BasePresenter<InvoiceView>(){

    @Inject
    lateinit var coinomatManager: CoinomatManager
    var assetBalance: AssetBalance? = null
    var address: String? = App.getAccessManager().getWallet()!!.address
    var amount: String? = "0"


    fun amountChanged(amount: String) {
        this.amount = amount
        runAsync {
            val findAsset: Single<List<AssetInfo>> = queryAsSingle { equalTo("id", assetBalance!!.assetId) }
            addSubscription(findAsset.toObservable().flatMap {
                val crypto = it[0].ticker
                coinomatManager.loadRate(crypto, address, CardFragment.USD, amount)
            }.subscribe({ rate ->
                runOnUiThread {
                    viewState.showRate(rate)
                }
            }, {
                viewState.showError(App.getAppContext()
                        .getString(R.string.receive_error_network))
            }))
        }

        nodeDataManager.loadTransactions()
    }
}
