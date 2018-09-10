package com.wavesplatform.wallet.v2.ui.home.wallet.assets

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsSingle
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@InjectViewState
class AssetsPresenter @Inject constructor() : BasePresenter<AssetsView>() {

    fun loadAssetsBalance() {
        addSubscription(queryAllAsSingle<AssetBalance>().toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    prepareAssetsAndShow(it, true)
                }
                .observeOn(Schedulers.io())
                .flatMap {
                    return@flatMap nodeDataManager.loadAssets(it)
                            .subscribeOn(Schedulers.io())
                }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    prepareAssetsAndShow(it)
                }, {
                    it.printStackTrace()
                }))
    }

    private fun prepareAssetsAndShow(assetList: List<AssetBalance>, fromDB: Boolean = false) {
        addSubscription(Observable.fromIterable(assetList)
                .toList()
                .map { list ->
                    val assets = list.filter { !it.isHidden && !it.isSpam  }
                            .sortedByDescending { it.isGateway }
                            .sortedByDescending { it.isFavorite}
                    val hidden = list.filter { it.isHidden }
                    val spam = list.filter { it.isSpam }
                    return@map Triple(assets, hidden, spam)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { t ->
                    viewState.afterSuccessLoadAssets(t.first, fromDB)
                    viewState.afterSuccessLoadHiddenAssets(t.second)
                    viewState.afterSuccessLoadSpamAssets(t.third)
                })
    }
}
