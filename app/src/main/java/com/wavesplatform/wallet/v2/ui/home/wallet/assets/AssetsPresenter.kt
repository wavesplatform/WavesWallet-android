package com.wavesplatform.wallet.v2.ui.home.wallet.assets

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsSingle
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import pyxis.uzuki.live.richutilskt.utils.runAsync
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import javax.inject.Inject

@InjectViewState
class AssetsPresenter @Inject constructor() : BasePresenter<AssetsView>() {

    fun loadAssetsBalance(withApiUpdate: Boolean = true) {
        runAsync {
            queryAllAsSingle<AssetBalance>().toObservable()
                    .subscribeOn(Schedulers.io())
                    .map { createTripleSortedLists(it) }
                    //.doOnNext { postSuccess(it, withApiUpdate, true) }
                    .flatMap { tryUpdateWithApi(withApiUpdate, it) }
                    .map { createTripleSortedLists(it) }
                    .subscribe({
                        runOnUiThread {
                            postSuccess(it, withApiUpdate, false)
                        }
                    }, {
                        runOnUiThread {
                            viewState.afterFailedLoadAssets()
                        }
                    })
        }
    }

    private fun tryUpdateWithApi(withApiUpdate: Boolean, it: Triple<List<AssetBalance>, List<AssetBalance>, List<AssetBalance>>): Observable<List<AssetBalance>>? {
        return if (withApiUpdate) {
            nodeDataManager.loadAssets(it.third)
        } else {
            Observable.just(it.third)
        }
    }

    private fun postSuccess(it: Triple<List<AssetBalance>, List<AssetBalance>, List<AssetBalance>>,
                            withApiUpdate: Boolean,
                            fromDb: Boolean) {
        viewState.afterSuccessLoadHiddenAssets(it.second)
        viewState.afterSuccessLoadSpamAssets(it.third)
        viewState.afterSuccessLoadAssets(it.first, fromDb, withApiUpdate)
    }

    private fun createTripleSortedLists(list: List<AssetBalance>): Triple<List<AssetBalance>, List<AssetBalance>, List<AssetBalance>> {
        val hiddenList = list.filter { it.isHidden && !it.isSpam }.sortedBy { it.position }
        val sortedToFirstFavoriteList = list.filter { !it.isHidden && !it.isSpam }.sortedByDescending({ it.isGateway }).sortedBy { it.position }.sortedByDescending({ it.isFavorite })
        val spamList = list.filter { it.isSpam }
        return Triple(sortedToFirstFavoriteList, hiddenList, spamList)
    }
}
