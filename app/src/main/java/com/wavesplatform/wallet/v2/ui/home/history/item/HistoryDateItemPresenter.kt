package com.wavesplatform.wallet.v2.ui.home.history.item

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsync
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.data.model.remote.response.TransactionType
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.home.history.HistoryItem
import com.wavesplatform.wallet.v2.util.transactionType
import pers.victor.ext.app
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@InjectViewState
class HistoryDateItemPresenter @Inject constructor() : BasePresenter<HistoryDateItemView>() {

    fun loadBundle(type: String?) {
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale(app.getString(preferenceHelper.getLanguage())))

        queryAllAsync<Transaction>({
            val sortedList = it.sortedByDescending { it.timestamp }
            val listFromDB = sortedList.mapTo(ArrayList(), {
                HistoryItem(it)
            })
            val list = arrayListOf<HistoryItem>()
            val hash = hashMapOf<Long, Long>()

            val listForTab = when (type) {
                HistoryDateItemFragment.all -> {
                    listFromDB
                }
                HistoryDateItemFragment.exchanged -> {
                    listFromDB.filter {
                        it.t.transactionType() == TransactionType.EXCHANGE_TYPE
                    }
                }
                HistoryDateItemFragment.issued -> {
                    listFromDB.filter {
                        it.t.transactionType() == TransactionType.TOKEN_REISSUE_TYPE ||
                                it.t.transactionType() == TransactionType.TOKEN_BURN_TYPE ||
                                it.t.transactionType() == TransactionType.TOKEN_GENERATION_TYPE
                    }
                }
                HistoryDateItemFragment.leased -> {
                    listFromDB.filter {
                        it.t.transactionType() == TransactionType.INCOMING_LEASING_TYPE ||
                                it.t.transactionType() == TransactionType.CANCELED_LEASING_TYPE ||
                                it.t.transactionType() == TransactionType.STARTED_LEASING_TYPE
                    }
                }
                HistoryDateItemFragment.send -> {
                    listFromDB.filter {
                        it.t.transactionType() == TransactionType.SENT_TYPE ||
                                it.t.transactionType() == TransactionType.MASS_SEND_TYPE
                    }
                }
                HistoryDateItemFragment.received -> {
                    listFromDB.filter {
                        it.t.transactionType() == TransactionType.RECEIVED_TYPE ||
                                it.t.transactionType() == TransactionType.MASS_RECEIVE_TYPE ||
                                it.t.transactionType() == TransactionType.MASS_SPAM_RECEIVE_TYPE ||
                                it.t.transactionType() == TransactionType.SPAM_RECEIVE_TYPE
                    }
                }
                else -> {
                    listFromDB
                }
            }

            listForTab.forEach {
                val date = (it.t.timestamp) / (1000 * 60 * 60 * 24)
                if (hash[date] == null) {
                    hash[date] = date
                    list.add(HistoryItem(true, dateFormat.format(Date(it.t.timestamp)).capitalize()))
                }
                list.add(it)
            }
            viewState.showData(list, type)
        })

    }

}
