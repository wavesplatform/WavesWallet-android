package com.wavesplatform.wallet.v2.ui.home.wallet.your_assets

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAsSingle
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import pyxis.uzuki.live.richutilskt.utils.runAsync
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import javax.inject.Inject

@InjectViewState
class YourAssetsPresenter @Inject constructor() : BasePresenter<YourAssetsView>() {
    fun loadAssets() {
        runAsync {
            Single.zip(queryAsSingle { equalTo("isFavorite", true)
                    .greaterThan("balance", 0) },
                    queryAsSingle { equalTo("isFavorite", false)
                            .greaterThan("balance", 0) },
                    BiFunction<List<AssetBalance>, List<AssetBalance>, Pair<List<AssetBalance>,
                            List<AssetBalance>>> { t1, t2 ->
                        return@BiFunction Pair(t1, t2)
                    }).compose(RxUtil.applySingleDefaultSchedulers())
                    .subscribe({
                        val assets = arrayListOf<AssetBalance>()
                        assets.addAll(it.first)
                        assets.addAll(it.second)
                        runOnUiThread {
                            viewState.showAssets(assets)
                        }
                    }, {
                        it.printStackTrace()
                    })
        }
    }

}
