package com.wavesplatform.wallet.v2.ui.home.wallet.assets.details

import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView
import java.util.*

interface AssetDetailsView : BaseMvpView {
    fun afterSuccessLoadAssets(sortedToFirstFavoriteList: ArrayList<AssetBalance>)
}
