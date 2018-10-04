package com.wavesplatform.wallet.v2.ui.home.history.tab

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsSingle
import com.vicpin.krealmextensions.queryAsSingle
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.home.history.HistoryItem
import com.wavesplatform.wallet.v2.util.notNull
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import pers.victor.ext.app
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@InjectViewState
class HistoryTabPresenter @Inject constructor() : BasePresenter<HistoryTabView>() {
    var allItemsFromDb = listOf<Transaction>()
    var totalHeaders = 0
    var type: String? = "all"
    var hashOfTimestamp = hashMapOf<Long, Long>()
    var assetBalance: AssetBalance? = null

    fun loadTransactions() {
        Log.d("historydev", "on presenter")
        val singleData: Single<List<Transaction>> = when (type) {
            HistoryTabFragment.all -> {
                queryAllAsSingle<Transaction>()
            }
            HistoryTabFragment.exchanged -> {
                queryAsSingle<Transaction> { `in`("transactionTypeId", arrayOf(Constants.ID_EXCHANGE_TYPE)) }
            }
            HistoryTabFragment.issued -> {
                queryAsSingle<Transaction> {
                    `in`("transactionTypeId", arrayOf(Constants.ID_TOKEN_REISSUE_TYPE,
                            Constants.ID_TOKEN_BURN_TYPE, Constants.ID_TOKEN_GENERATION_TYPE))
                }
            }
            HistoryTabFragment.leased -> {
                queryAsSingle<Transaction> {
                    `in`("transactionTypeId", arrayOf(Constants.ID_INCOMING_LEASING_TYPE,
                            Constants.ID_CANCELED_LEASING_TYPE, Constants.ID_STARTED_LEASING_TYPE))
                }
            }
            HistoryTabFragment.send -> {
                queryAsSingle<Transaction> {
                    `in`("transactionTypeId", arrayOf(Constants.ID_SENT_TYPE, Constants.ID_MASS_SEND_TYPE))
                }
            }
            HistoryTabFragment.received -> {
                queryAsSingle<Transaction> {
                    `in`("transactionTypeId", arrayOf(Constants.ID_RECEIVED_TYPE, Constants.ID_MASS_RECEIVE_TYPE,
                            Constants.ID_MASS_SPAM_RECEIVE_TYPE, Constants.ID_SPAM_RECEIVE_TYPE))
                }
            }
            HistoryTabFragment.leasing_all -> {
                queryAsSingle<Transaction> {
                    `in`("transactionTypeId", arrayOf(Constants.ID_STARTED_LEASING_TYPE,
                            Constants.ID_INCOMING_LEASING_TYPE, Constants.ID_CANCELED_LEASING_TYPE))
                }
            }
            HistoryTabFragment.leasing_active_now -> {
                queryAsSingle<Transaction> {
                    `in`("transactionTypeId", arrayOf(Constants.ID_STARTED_LEASING_TYPE))
                }
            }
            HistoryTabFragment.leasing_canceled -> {
                queryAsSingle<Transaction> {
                    `in`("transactionTypeId", arrayOf(Constants.ID_CANCELED_LEASING_TYPE))
                }
            }
            else -> {
                queryAllAsSingle<Transaction>()
            }
        }

        addSubscription(singleData
                .map {
                    // all history
                    allItemsFromDb = it.sortedByDescending({ it.timestamp })

                    // history only for detailed asset
                    assetBalance.notNull {
                        allItemsFromDb = allItemsFromDb.filter {
                            if (assetBalance?.isWaves() == true) it.assetId.isNullOrEmpty()
                            else it.assetId == assetBalance?.assetId
                        }
                    }

                    return@map sortAndConfigToUi(allItemsFromDb)
                }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    viewState.afterSuccessLoadTransaction(it, type)
                }, {
                    it.printStackTrace()
                }))
    }

    private fun sortAndConfigToUi(it: List<Transaction>): ArrayList<HistoryItem> {
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale(app.getString(preferenceHelper.getLanguage())))

        val sortedList = it
                .mapTo(mutableListOf()) {
                    HistoryItem(HistoryItem.TYPE_DATA, it)
                }

        val list = arrayListOf<HistoryItem>()

        sortedList.forEach {
            val date = (it.data.timestamp) / (1000 * 60 * 60 * 24)
            if (hashOfTimestamp[date] == null) {
                hashOfTimestamp[date] = date
                list.add(HistoryItem(HistoryItem.TYPE_HEADER, dateFormat.format(Date(it.data.timestamp)).capitalize()))
                totalHeaders++
            }
            list.add(it)
        }

        if (list.isNotEmpty()) {
            list.add(0, HistoryItem(HistoryItem.TYPE_EMPTY, ""))
        }

        return list
    }

}
