package com.wavesplatform.wallet.v2.ui.home.wallet.assets.sorting

import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface AssetsSortingView : BaseMvpView {
    fun showFavoriteAssets(favorites: List<AssetBalance>)
    fun showNotFavoriteAssets(notFavorites: List<AssetBalance>)
    fun checkIfNeedToShowLine()
}
