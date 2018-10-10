package com.wavesplatform.wallet.v2.ui.home.wallet.assets.details.content

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsync
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.data.model.remote.response.TransactionType
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.data.model.local.HistoryItem
import com.wavesplatform.wallet.v2.util.transactionType
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import javax.inject.Inject

@InjectViewState
class AssetDetailsContentPresenter @Inject constructor() : BasePresenter<AssetDetailsContentView>() {
    var assetBalance: AssetBalance? = null

    fun loadLastTransactions() {
        queryAllAsync<Transaction> {
            val list = it
                    .sortedByDescending({ it.timestamp })
                    .filter { it.transactionType() != TransactionType.MASS_SPAM_RECEIVE_TYPE || it.transactionType() != TransactionType.SPAM_RECEIVE_TYPE }
                    .filter {
                        if (assetBalance?.isWaves == true) it.assetId.isNullOrEmpty()
                        else it.assetId == assetBalance?.assetId
                    }
                    .mapTo(ArrayList()) { HistoryItem(HistoryItem.TYPE_DATA, it) }
                    .take(10)
                    .toMutableList()

            runOnUiThread {
                viewState.showLastTransactions(list)
            }
        }
    }
}
