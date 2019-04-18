/*
 * Created by Ershov Aleksandr on 9/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.assets.search_asset

import com.arellomobile.mvp.InjectViewState
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.vicpin.krealmextensions.queryAll
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.AssetsAdapter
import com.wavesplatform.wallet.v2.util.findAssetBalanceInDb
import javax.inject.Inject

@InjectViewState
class SearchAssetPresenter @Inject constructor() : BasePresenter<SearchAssetView>() {

    private var findAssetList = listOf<AssetBalance>()

    fun search(query: String) {
        if (findAssetList.isEmpty()) {
            findAssetList = queryAll()
        }
        val find = findAssetBalanceInDb(query, findAssetList)
        val result = mutableListOf<MultiItemEntity>()
        val hiddenAssets = mutableListOf<MultiItemEntity>()

        find.forEach {
            if (it.isFavorite) {
                result.add(it)
            }
        }

        find.forEach {
            if (!it.isFavorite) {
                if (it.isHidden) {
                    hiddenAssets.add(it)
                } else {
                    result.add(it)
                }
            }
        }

        if (hiddenAssets.isNotEmpty()) {
            val hiddenHeaderItem = MultiItemEntity {
                AssetsAdapter.TYPE_HEADER
            }
            result.add(hiddenHeaderItem)
            result.addAll(hiddenAssets)
        }

        viewState.setSearchResult(result)
    }
}