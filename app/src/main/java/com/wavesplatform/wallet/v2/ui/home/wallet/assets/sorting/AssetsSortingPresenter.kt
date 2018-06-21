package com.wavesplatform.wallet.v2.ui.home.wallet.assets.sorting

import com.arellomobile.mvp.InjectViewState
import com.google.common.base.Predicates.equalTo
import com.vicpin.krealmextensions.query
import com.vicpin.krealmextensions.queryAll
import com.wavesplatform.wallet.v2.data.model.local.MultipleSortingAssetItem
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class AssetsSortingPresenter @Inject constructor() : BasePresenter<AssetsSortingView>() {

    fun getFavoriteAssets(): List<AssetBalance> {
        return query { equalTo("isFavorite", true) }
    }

    fun getAssets(): List<AssetBalance> {
        return query { equalTo("isFavorite", false) }
    }

}
