/*
 * Created by Ershov Aleksandr on 9/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.search_asset

import android.text.TextUtils
import com.arellomobile.mvp.InjectViewState
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.vicpin.krealmextensions.queryAll
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.AssetsAdapter
import javax.inject.Inject

@InjectViewState
class SearchAssetPresenter @Inject constructor() : BasePresenter<SearchAssetView>() {

    fun search(query: String) {
        val find = if (TextUtils.isEmpty(query)) {
            queryAllAssetBalance()
        } else {
            val queryLower = query.toLowerCase()
            queryAllAssetBalance().filter {
                it.assetId.toLowerCase().contains(queryLower)
                        || it.getName().toLowerCase().contains(queryLower)
                        || it.issueTransaction?.name?.toLowerCase()?.contains(queryLower) ?: false
                        || it.issueTransaction?.assetId?.toLowerCase()?.contains(queryLower) ?: false
                        || it.assetId == Constants.findByGatewayId(query.toUpperCase())?.assetId
            }
        }

        val result = mutableListOf<MultiItemEntity>()

        val assets = find.filter {
            !it.isHidden
        }
        result.addAll(assets)

        val hiddenAssets = find.filter {
            it.isHidden
        }
        if (hiddenAssets.isNotEmpty()) {
            val hiddenHeaderItem = MultiItemEntity {
                AssetsAdapter.TYPE_HEADER
            }
            result.add(hiddenHeaderItem)
        }
        result.addAll(hiddenAssets)

        viewState.setSearchResult(result)
    }

    private fun queryAllAssetBalance(): List<AssetBalance>  {
        return queryAll<AssetBalance>().filter { !it.isSpam }
    }
}