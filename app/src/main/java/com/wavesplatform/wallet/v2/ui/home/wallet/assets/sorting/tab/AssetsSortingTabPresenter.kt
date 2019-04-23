/*
 * Created by Eduard Zaydel on 23/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.assets.sorting.tab

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsSingle
import com.vicpin.krealmextensions.save
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.wallet.v2.data.model.local.AssetSortingItem
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.userdb.AssetBalanceStore
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.sorting.AssetsSortingView
import com.wavesplatform.wallet.v2.util.RxUtil
import pyxis.uzuki.live.richutilskt.utils.runAsync
import javax.inject.Inject

@InjectViewState
class AssetsSortingTabPresenter @Inject constructor() : BasePresenter<AssetsSortingTabView>() {
    var needToUpdate: Boolean = false
    var visibilityConfigurationActive = false

    fun loadAssets() {
        runAsync {
            addSubscription(queryAllAsSingle<AssetBalance>().toObservable()
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({
                        val result = mutableListOf<AssetSortingItem>()

                        val favoriteList = it.filter { it.isFavorite }
                                .sortedBy { it.position }
                                .sortedByDescending { it.isFavorite }
                                .mapTo(mutableListOf()) {
                                    AssetSortingItem(AssetSortingItem.TYPE_FAVORITE, it)
                                }
                        val notFavoriteList = it.filter { !it.isFavorite && !it.isSpam }
                                .sortedBy { it.position }
                                .mapTo(mutableListOf()) {
                                    AssetSortingItem(AssetSortingItem.TYPE_NOT_FAVORITE, it)
                                }

                        result.addAll(favoriteList)
                        if (favoriteList.isNotEmpty() && notFavoriteList.isNotEmpty()) {
                            result.add(AssetSortingItem(AssetSortingItem.TYPE_LINE))
                        }
                        result.addAll(notFavoriteList)

                        viewState.showAssets(result)
                    }, {
                        it.printStackTrace()
                    }))
        }
    }

    fun saveSortedPositions(data: MutableList<AssetSortingItem>) {
        data
                .filter { it.type != AssetSortingItem.TYPE_LINE }
                .mapIndexedTo(mutableListOf()) { position, item ->
                    item.asset.position = position
                    AssetBalanceStore(item.asset.assetId,
                            item.asset.isHidden,
                            item.asset.position,
                            item.asset.isFavorite).save()
                    return@mapIndexedTo item.asset
                }
                .saveAll()
    }
}
