package com.wavesplatform.wallet.v2.ui.home.wallet.assets.sorting

import com.arellomobile.mvp.InjectViewState
import com.google.common.base.Predicates.equalTo
import com.vicpin.krealmextensions.query
import com.vicpin.krealmextensions.queryAsSingle
import com.vicpin.krealmextensions.queryAsync
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@InjectViewState
class AssetsSortingPresenter @Inject constructor() : BasePresenter<AssetsSortingView>() {

    fun loadAssets() {
        Single.zip(queryAsSingle({ equalTo("isFavorite", true) }),
                queryAsSingle({ equalTo("isFavorite", false) }),
                BiFunction<List<AssetBalance>, List<AssetBalance>, Pair<List<AssetBalance>, List<AssetBalance>>> { t1, t2 ->
                    return@BiFunction Pair(t1, t2)
                }).compose(RxUtil.applySingleDefaultSchedulers())
                .subscribe({
                    viewState.showFavoriteAssets(it.first)

                    viewState.showNotFavoriteAssets(it.second)

                    viewState.checkIfNeedToShowLine()
                }, {
                    it.printStackTrace()
                })
    }

}
