package com.wavesplatform.wallet.v2.ui.home.wallet.assets.token_burn

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import org.fingerlinks.mobile.android.navigator.builder.Builders
import javax.inject.Inject

@InjectViewState
class TokenBurnPresenter @Inject constructor() : BasePresenter<TokenBurnView>() {
    var quantityValidation = false
    var wavesBalance: AssetBalance = AssetBalance()
    var assetBalance = AssetBalance()

    fun isAllFieldsValid(): Boolean {
        return quantityValidation
    }

    fun loadWavesBalance() {
        addSubscription(nodeDataManager.loadWavesBalance()
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe {
                    wavesBalance = it
                })
    }
}
