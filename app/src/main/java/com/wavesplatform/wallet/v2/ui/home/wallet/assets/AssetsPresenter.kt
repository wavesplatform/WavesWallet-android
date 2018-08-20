package com.wavesplatform.wallet.v2.ui.home.wallet.assets

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsSingle
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.notNull
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@InjectViewState
class AssetsPresenter @Inject constructor() : BasePresenter<AssetsView>() {

    fun loadAssetsBalance() {
        addSubscription(queryAllAsSingle<AssetBalance>().toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext({
                    prepareAssetsAndShow(it, true)
                })
                .observeOn(Schedulers.io())
                .flatMap({
                    return@flatMap nodeDataManager.loadAssets(it)
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    prepareAssetsAndShow(it)
                }, {
                    it.printStackTrace()
                }))
    }

    private fun prepareAssetsAndShow(it: List<AssetBalance>, fromDB: Boolean = false) {
        it.notNull {
            val hiddenList = it.filter({ it.isHidden })
            val sortedToFirstFavoriteList = it.filter({ !it.isHidden }).filter({ !it.isSpam }).sortedByDescending({ it.isGateway })
            val spamList = it.filter({ it.isSpam })

            viewState.afterSuccessLoadAssets(sortedToFirstFavoriteList, fromDB)
            viewState.afterSuccessLoadHiddenAssets(hiddenList)

            viewState.afterSuccessLoadSpamAssets(spamList)
        }
    }
}
