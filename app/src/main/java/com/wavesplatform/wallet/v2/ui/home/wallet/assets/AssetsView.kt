package com.wavesplatform.wallet.v2.ui.home.wallet.assets

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

@StateStrategyType(AddToEndSingleStrategy::class)
interface AssetsView :BaseMvpView{
    fun afterSuccessLoadAssets(assets: List<AssetBalance>, fromDB: Boolean, withApiUpdate: Boolean)
    fun afterSuccessLoadHiddenAssets(assets: List<AssetBalance>)
    fun afterSuccessLoadSpamAssets(assets: List<AssetBalance>)
    fun afterFailedLoadAssets()
    fun startServiceToLoadData(assets: ArrayList<AssetBalance>)
}
