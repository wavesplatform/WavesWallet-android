package com.wavesplatform.wallet.v2.ui.home.history.details

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import javax.inject.Inject

@InjectViewState
class HistoryDetailsPresenter @Inject constructor() : BasePresenter<HistoryDetailsView>() {


//    inline fun getAssetDetails(assetId: String?, crossinline callback: (AssetBalance) -> Unit) {
//        addSubscription(apiDataManager.assetDetails(assetId)
//                .compose(RxUtil.applyObservableDefaultSchedulers())
//                .subscribe({
//                    callback(it)
//                }, {
//                    it.printStackTrace()
//                }))
//    }

}
