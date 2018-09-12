package com.wavesplatform.wallet.v2.ui.home.wallet.assets.details

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsSingle
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import java.util.*
import javax.inject.Inject

@InjectViewState
class AssetDetailsPresenter @Inject constructor() : BasePresenter<AssetDetailsView>() {
    var needToUpdate: Boolean = false

    fun loadAssets() {
        addSubscription(queryAllAsSingle<AssetBalance>()
                .compose(RxUtil.applySingleDefaultSchedulers())
                .subscribe({
                    val sortedToFirstFavoriteList = it.filter({ !it.isHidden && !it.isSpam }).sortedByDescending({ it.isGateway &&  it.isFavorite  }).toCollection(ArrayList())
                    val hiddenList = it.filter({ it.isHidden }).toCollection(ArrayList())
                    val spamList = it.filter({ it.isSpam })
                    sortedToFirstFavoriteList.addAll(hiddenList)
                    sortedToFirstFavoriteList.addAll(spamList)

                    viewState.afterSuccessLoadAssets(sortedToFirstFavoriteList)
                }, {
                    it.printStackTrace()
                }))
    }

}
