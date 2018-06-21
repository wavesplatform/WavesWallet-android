package com.wavesplatform.wallet.v2.ui.home.wallet.assets

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import javax.inject.Inject

@InjectViewState
class AssetsPresenter @Inject constructor() :BasePresenter<AssetsView>(){

    fun loadAssetsBalance() {
        addSubscription(nodeDataManager.loadAssetsBalance()
                .compose(RxUtil.applyDefaultSchedulers())
                .subscribe({
                    val hiddenList = it.filter({ it.isHidden })
                    val sortedToFirstFavoriteList = it.sortedByDescending({ it.isFavorite })

                    viewState.afterSuccessLoadAssets(sortedToFirstFavoriteList)
                    viewState.afterSuccessLoadHiddenAssets(hiddenList)

                    // TODO: implement spam assets
                    viewState.afterSuccessLoadSpamAssets(listOf<AssetBalance>())
                }, {
                    it.printStackTrace()
                }))
    }
}
