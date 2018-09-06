package com.wavesplatform.wallet.v2.ui.home.history.tab

import android.os.Looper
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsSingle
import com.vicpin.krealmextensions.queryAsSingle
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.home.history.HistoryItem
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import pers.victor.ext.app
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@InjectViewState
class HistoryTabPresenter @Inject constructor() : BasePresenter<HistoryTabView>() {
    var allItemsFromDb = listOf<Transaction>()
    var totalHeaders = 0
    var type: String? = "all"
    var needLoadMore: Boolean = true
    var loadMoreCompleted = true
    val hashOfTimestamp = hashMapOf<Long, Long>()

    companion object {
        var PER_PAGE = 25
    }

    fun loadTransactions() {
        val singleData: Single<List<Transaction>> = when (type) {
            HistoryTabFragment.all -> {
                queryAllAsSingle()
            }
            HistoryTabFragment.exchanged -> {
                queryAsSingle({ `in`("transactionTypeId", arrayOf(Constants.ID_EXCHANGE_TYPE)) })
            }
            HistoryTabFragment.issued -> {
                queryAsSingle({
                    `in`("transactionTypeId", arrayOf(Constants.ID_TOKEN_REISSUE_TYPE,
                            Constants.ID_TOKEN_BURN_TYPE, Constants.ID_TOKEN_GENERATION_TYPE))
                })
            }
            HistoryTabFragment.leased -> {
                queryAsSingle({
                    `in`("transactionTypeId", arrayOf(Constants.ID_INCOMING_LEASING_TYPE,
                            Constants.ID_CANCELED_LEASING_TYPE, Constants.ID_STARTED_LEASING_TYPE))
                })
            }
            HistoryTabFragment.send -> {
                queryAsSingle({
                    `in`("transactionTypeId", arrayOf(Constants.ID_SENT_TYPE, Constants.ID_MASS_SEND_TYPE))
                })
            }
            HistoryTabFragment.received -> {
                queryAsSingle({
                    `in`("transactionTypeId", arrayOf(Constants.ID_RECEIVED_TYPE, Constants.ID_MASS_RECEIVE_TYPE,
                            Constants.ID_MASS_SPAM_RECEIVE_TYPE, Constants.ID_SPAM_RECEIVE_TYPE))
                })
            }
            HistoryTabFragment.leasing_all -> {
                queryAsSingle({
                    `in`("transactionTypeId", arrayOf(Constants.ID_STARTED_LEASING_TYPE,
                            Constants.ID_INCOMING_LEASING_TYPE, Constants.ID_CANCELED_LEASING_TYPE))
                })
            }
            HistoryTabFragment.leasing_active_now -> {
                queryAsSingle({
                    `in`("transactionTypeId", arrayOf(Constants.ID_STARTED_LEASING_TYPE))
                })
            }
            HistoryTabFragment.leasing_canceled -> {
                queryAsSingle({
                    `in`("transactionTypeId", arrayOf(Constants.ID_CANCELED_LEASING_TYPE))
                })
            }
            else -> {
                queryAllAsSingle()
            }
        }

        addSubscription(singleData
                .map({
                    allItemsFromDb = it.sortedByDescending({ it.timestamp })
                    if (allItemsFromDb.size > 50) {
                        return@map sortAndConfigToUi(allItemsFromDb.subList(0, PER_PAGE))
                    } else {
                        return@map sortAndConfigToUi(allItemsFromDb)
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    viewState.showData(it, type)
                }, {

                }))
    }

    fun loadMore(currentItemsSize: Int) {
        val itemsWithoutHeaders = currentItemsSize - totalHeaders

        if (itemsWithoutHeaders == allItemsFromDb.size) {
            needLoadMore = false
            viewState.goneLoadMoreView()
            return
        }

        val toIndex = if (itemsWithoutHeaders + PER_PAGE >= allItemsFromDb.size) allItemsFromDb.size else itemsWithoutHeaders + PER_PAGE

        addSubscription(Single.just(allItemsFromDb.subList(itemsWithoutHeaders, toIndex))
                .map({
                    return@map sortAndConfigToUi(it)
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    viewState.showData(it, type)
                }, {}))
    }


    private fun sortAndConfigToUi(it: List<Transaction>): ArrayList<HistoryItem> {
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale(app.getString(preferenceHelper.getLanguage())))

        val sortedList = it
                .mapTo(mutableListOf(), {
                    HistoryItem(it)
                })

        val list = arrayListOf<HistoryItem>()

        sortedList.forEach {
            val date = (it.t.timestamp) / (1000 * 60 * 60 * 24)
            if (hashOfTimestamp[date] == null) {
                hashOfTimestamp[date] = date
                list.add(HistoryItem(true, dateFormat.format(Date(it.t.timestamp)).capitalize()))
                totalHeaders++
            }
            list.add(it)
        }

        return list
    }

}
