package com.wavesplatform.wallet.v2.ui.home.wallet.assets.sorting

import com.wavesplatform.wallet.v2.data.model.local.AssetSortingItem
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface AssetsSortingView : BaseMvpView {
    fun showAssets(assets: MutableList<AssetSortingItem>)
}
