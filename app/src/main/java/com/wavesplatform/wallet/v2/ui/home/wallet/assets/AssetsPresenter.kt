package com.wavesplatform.wallet.v2.ui.home.wallet.assets

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v1.api.NodeManager
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class AssetsPresenter @Inject constructor() :BasePresenter<AssetsView>(){

    fun getActiveAccountAndAddressList(){
//        val allAssets = NodeManager.get().allAssets
//        var test = ""
    }
}
