/*
 * Created by Ershov Aleksandr on 9/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.search_asset

import com.arellomobile.mvp.InjectViewState
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.AssetsAdapter
import com.wavesplatform.wallet.v2.util.findAssetBalanceInDb
import javax.inject.Inject

@InjectViewState
class SearchAssetPresenter @Inject constructor() : BasePresenter<SearchAssetView>() {

    fun search(query: String) {
        val find = findAssetBalanceInDb(query)
        val result = mutableListOf<MultiItemEntity>()
        val hiddenAssets = mutableListOf<MultiItemEntity>()

        find.forEach {
            if (it.isHidden) {
                hiddenAssets.add(it)
            } else {
                result.add(it)
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