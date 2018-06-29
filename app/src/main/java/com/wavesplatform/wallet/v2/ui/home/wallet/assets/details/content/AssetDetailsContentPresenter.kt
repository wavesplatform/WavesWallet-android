package com.wavesplatform.wallet.v2.ui.home.wallet.assets.details.content

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.home.history.TestObject
import com.wavesplatform.wallet.v2.ui.home.history.adapter.HistoryItem
import java.util.*
import javax.inject.Inject

@InjectViewState
class AssetDetailsContentPresenter @Inject constructor() : BasePresenter<AssetDetailsContentView>() {
    var assetBalance: AssetBalance? = null

    fun loadBundle() {

        var data = ArrayList<HistoryItem>()
        data.add(HistoryItem(TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble())))
        data.add(HistoryItem(TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble())))
        data.add(HistoryItem(TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble())))
        data.add(HistoryItem(TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble())))
        data.add(HistoryItem(TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble())))
        data.add(HistoryItem(TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble())))

        viewState.showData(data)
    }

}
