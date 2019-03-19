package com.wavesplatform.wallet.v2.ui.home.wallet.assets

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

@StateStrategyType(AddToEndSingleStrategy::class)
interface AssetsView : BaseMvpView {
    fun afterSuccessLoadAssets(assets: ArrayList<MultiItemEntity>, fromDB: Boolean, withApiUpdate: Boolean)
    fun afterFailedLoadAssets()
    fun afterFailedUpdateAssets()
    fun startServiceToLoadData()
}
