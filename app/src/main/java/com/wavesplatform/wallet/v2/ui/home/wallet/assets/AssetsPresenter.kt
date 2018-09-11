package com.wavesplatform.wallet.v2.ui.home.wallet.assets

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsSingle
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.notNull
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@InjectViewState
class AssetsPresenter @Inject constructor() : BasePresenter<AssetsView>() {

    fun loadAssetsBalance(withApiUpdate: Boolean = true) {
        addSubscription(queryAllAsSingle<AssetBalance>().toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext({
                    prepareAssetsAndShow(it, true, withApiUpdate)
                })
                .observeOn(Schedulers.io())
                .flatMap({
                    if (withApiUpdate) {
                        return@flatMap nodeDataManager.loadAssets(it)
                                .subscribeOn(Schedulers.io())
                    } else {
                        return@flatMap Observable.just(it)
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (withApiUpdate) {
                        prepareAssetsAndShow(it, false, withApiUpdate)
                    }
                }, {
                    it.printStackTrace()
                    viewState.afterFailedLoadAssets()
                }))
    }

    private fun prepareAssetsAndShow(it: List<AssetBalance>, fromDB: Boolean, withApiUpdate: Boolean) {
        it.notNull {
            val hiddenList = it.filter({ it.isHidden && !it.isSpam }).sortedBy { it.position }
            val sortedToFirstFavoriteList = it.filter({ !it.isHidden && !it.isSpam }).sortedByDescending({ it.isGateway }).sortedBy { it.position }.sortedByDescending({ it.isFavorite })
            val spamList = it.filter({ it.isSpam })

            viewState.afterSuccessLoadAssets(sortedToFirstFavoriteList, fromDB, withApiUpdate)
            viewState.afterSuccessLoadHiddenAssets(hiddenList)

            viewState.afterSuccessLoadSpamAssets(spamList)
        }
    }
}
