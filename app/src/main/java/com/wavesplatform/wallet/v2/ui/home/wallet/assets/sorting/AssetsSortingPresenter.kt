package com.wavesplatform.wallet.v2.ui.home.wallet.assets.sorting

import android.os.Bundle
import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsync
import com.vicpin.krealmextensions.queryAsSingle
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import javax.inject.Inject

@InjectViewState
class AssetsSortingPresenter @Inject constructor() : BasePresenter<AssetsSortingView>() {
    var needToUpdate: Boolean = false

    fun loadAssets() {
        queryAllAsync<AssetBalance> {
            val favoriteList = it.filter({ it.isFavorite }).toMutableList()
            val notFavoriteList = it.filter({ !it.isFavorite && !it.isSpam }).sortedBy { it.position }.toMutableList()

            runOnUiThread {
                viewState.showFavoriteAssets(favoriteList)

                viewState.showNotFavoriteAssets(notFavoriteList)

                viewState.checkIfNeedToShowLine()
            }
        }
    }

    fun saveSortedPositions(data: List<AssetBalance>) {
        data.forEachIndexed { index, assetBalance ->
            assetBalance.position = index
        }
        data.saveAll()
    }

}
