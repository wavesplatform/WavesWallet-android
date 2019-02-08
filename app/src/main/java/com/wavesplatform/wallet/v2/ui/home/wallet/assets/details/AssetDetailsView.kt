package com.wavesplatform.wallet.v2.ui.home.wallet.assets.details

import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.wavesplatform.sdk.model.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView
import java.util.*

@StateStrategyType(SkipStrategy::class)
interface AssetDetailsView : BaseMvpView {
    fun afterSuccessLoadAssets(sortedToFirstFavoriteList: MutableList<AssetBalance>)
}
