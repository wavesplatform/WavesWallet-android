/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.assets.sorting

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsSingle
import com.vicpin.krealmextensions.save
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.wallet.v2.data.model.local.AssetSortingItem
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.userdb.AssetBalanceStore
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import pyxis.uzuki.live.richutilskt.utils.runAsync
import javax.inject.Inject

@InjectViewState
class AssetsSortingPresenter @Inject constructor() : BasePresenter<AssetsSortingView>() {
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
                                    AssetSortingItem(AssetSortingItem.TYPE_FAVORITE_ITEM, it)
                                }
                        val notFavoriteList = it.filter { !it.isFavorite && !it.isSpam && !it.isHidden }
                                .sortedBy { it.position }
                                .mapTo(mutableListOf()) {
                                    AssetSortingItem(AssetSortingItem.TYPE_DEFAULT_ITEM, it)
                                }

                        val hiddenList = it.filter { it.isHidden }
                                .sortedBy { it.position }
                                .mapTo(mutableListOf()) {
                                    AssetSortingItem(AssetSortingItem.TYPE_HIDDEN_ITEM, it)
                                }

                        if (favoriteList.isNotEmpty()) {
                            result.addAll(favoriteList)
                        } else {
                            result.add(AssetSortingItem(AssetSortingItem.TYPE_EMPTY_FAVORITE))
                        }

                        result.add(AssetSortingItem(AssetSortingItem.TYPE_FAVORITE_SEPARATOR))

                        if (notFavoriteList.isNotEmpty()) {
                            result.addAll(notFavoriteList)
                        } else {
                            result.add(AssetSortingItem(AssetSortingItem.TYPE_EMPTY_DEFAULT))
                        }

                        result.add(AssetSortingItem(AssetSortingItem.TYPE_HIDDEN_HEADER))

                        if (hiddenList.isNotEmpty()) {
                            result.addAll(hiddenList)
                        } else {
                            result.add(AssetSortingItem(AssetSortingItem.TYPE_EMPTY_DEFAULT))
                        }

                        viewState.showAssets(result)
                    }, {
                        it.printStackTrace()
                    }))
        }
    }

    fun saveSortedPositions(data: MutableList<AssetSortingItem>) {
        data
                .filter { it.type != AssetSortingItem.TYPE_FAVORITE_SEPARATOR && it.type != AssetSortingItem.TYPE_HIDDEN_HEADER }
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
