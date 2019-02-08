package com.wavesplatform.wallet.v2.ui.home.wallet.your_assets

import com.wavesplatform.sdk.model.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface YourAssetsView : BaseMvpView {
    fun showAssets(assets: MutableList<AssetBalance>)
}
