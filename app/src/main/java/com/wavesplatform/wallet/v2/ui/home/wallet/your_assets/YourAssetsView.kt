/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.your_assets

import com.wavesplatform.sdk.net.model.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface YourAssetsView : BaseMvpView {
    fun showAssets(assets: MutableList<AssetBalance>)
}
