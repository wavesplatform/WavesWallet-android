package com.wavesplatform.wallet.v2.ui.home.wallet.assets.token_burn

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.data.model.remote.response.*
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import com.wavesplatform.wallet.v2.util.TransactionUtil
import io.reactivex.Observable
import io.reactivex.functions.Function3
import javax.inject.Inject

@InjectViewState
class TokenBurnPresenter @Inject constructor() : BasePresenter<TokenBurnView>() {
    var quantityValidation = false
    var wavesBalance: AssetBalance = AssetBalance()
    var assetBalance = AssetBalance()
    var fee = 0L

    fun isAllFieldsValid(): Boolean {
        return quantityValidation && fee > 0L
    }

    fun loadWavesBalance() {
        addSubscription(nodeDataManager.loadWavesBalance()
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe {
                    wavesBalance = it
                })
    }

    fun loadCommission(assetId: String?) {
        viewState.showCommissionLoading()
        fee = 0L
        addSubscription(Observable.zip(
                githubDataManager.getGlobalCommission(),
                nodeDataManager.scriptAddressInfo(
                        App.getAccessManager().getWallet()?.address ?: ""),
                nodeDataManager.assetDetails(assetId),
                Function3 { t1: GlobalTransactionCommission,
                            t2: ScriptInfo,
                            t3: AssetsDetails ->
                    return@Function3 Triple(t1, t2, t3)
                })
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ triple ->
                    val commission = triple.first
                    val scriptInfo = triple.second
                    val assetsDetails = triple.third
                    val params = GlobalTransactionCommission.Params()
                    params.transactionType = Transaction.BURN
                    params.smartAccount = scriptInfo.extraFee != 0L
                    params.smartAsset = assetsDetails.scripted
                    fee = TransactionUtil.countCommission(commission, params)
                    viewState.showCommissionSuccess(fee)
                }, {
                    it.printStackTrace()
                    fee = 0L
                    viewState.showCommissionError()
                }))
    }
}
