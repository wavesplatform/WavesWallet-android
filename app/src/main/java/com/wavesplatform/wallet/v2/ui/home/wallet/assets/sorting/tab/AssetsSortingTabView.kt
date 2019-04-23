/*
 * Created by Eduard Zaydel on 23/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.assets.sorting.tab

import com.wavesplatform.wallet.v2.data.model.local.AssetSortingItem
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface AssetsSortingTabView : BaseMvpView {
    fun showAssets(assets: MutableList<AssetSortingItem>)
}
