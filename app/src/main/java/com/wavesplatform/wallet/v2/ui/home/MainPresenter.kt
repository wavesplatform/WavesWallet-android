package com.wavesplatform.wallet.v2.ui.home

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsSingle
import com.vicpin.krealmextensions.queryAsSingle
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.model.remote.response.*
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import com.wavesplatform.wallet.v2.util.TransactionUtil
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Function3
import pyxis.uzuki.live.richutilskt.utils.runAsync
import javax.inject.Inject

@InjectViewState
class MainPresenter @Inject constructor() : BasePresenter<MainView>() {
    @Inject
    lateinit var transactionUtil: TransactionUtil

    var checkedAboutFundsOnDevice = false
    var checkedAboutBackup = false
    var checkedAboutTerms = false

    fun isAllCheckedToStart(): Boolean {
        return checkedAboutBackup && checkedAboutFundsOnDevice && checkedAboutTerms
    }

    fun reloadTransactionsAfterSpamSettingsChanged(afterUrlChanged: Boolean = false) {
        runAsync {
            val singleData: Single<List<Transaction>> = if (afterUrlChanged) {
                queryAsSingle<Transaction> {
                    `in`("transactionTypeId", arrayOf(Constants.ID_MASS_SPAM_RECEIVE_TYPE, Constants.ID_SPAM_RECEIVE_TYPE,
                            Constants.ID_RECEIVED_TYPE, Constants.ID_MASS_RECEIVE_TYPE))
                }
            } else {
                if (prefsUtil.getValue(PrefsUtil.KEY_ENABLE_SPAM_FILTER, true)) {
                    queryAsSingle<Transaction> {
                        `in`("transactionTypeId", arrayOf(Constants.ID_RECEIVED_TYPE, Constants.ID_MASS_RECEIVE_TYPE))
                    }
                } else {
                    queryAsSingle<Transaction> {
                        `in`("transactionTypeId", arrayOf(Constants.ID_MASS_SPAM_RECEIVE_TYPE, Constants.ID_SPAM_RECEIVE_TYPE))
                    }
                }
            }

            addSubscription(Observable.zip(
                    singleData.toObservable(),
                    queryAllAsSingle<SpamAsset>().toObservable()
                            .map { spamListFromDb ->
                                if (prefsUtil.getValue(PrefsUtil.KEY_ENABLE_SPAM_FILTER, true)) {
                                    return@map spamListFromDb
                                } else {
                                    return@map listOf<SpamAsset>()
                                }
                            },
                    queryAllAsSingle<AssetInfo>().toObservable(),
                    Function3 { t1: List<Transaction>, t2: List<SpamAsset>, t3: List<AssetInfo> ->
                        return@Function3 Triple(t1, t2, t3)
                    })
                    .subscribe { pairOfData ->
                        val spamListFromDb = pairOfData.second
                        val transactionListFromDb = pairOfData.first
                        val assetsInfoListFromDb = pairOfData.third

                        assetsInfoListFromDb.forEach { assetInfo ->
                            assetInfo.isSpam = spamListFromDb.any { it.assetId == assetInfo.id }
                        }

                        transactionListFromDb.forEach { transaction ->
                            transaction.asset = if (transaction.assetId.isNullOrEmpty()) {
                                Constants.wavesAssetInfo
                            } else {
                                assetsInfoListFromDb.firstOrNull { it.id == transaction.assetId }
                            }
                            transaction.transactionTypeId = transactionUtil.getTransactionType(transaction)
                        }

                        transactionListFromDb.saveAll()

                        rxEventBus.post(Events.NeedUpdateHistoryScreen())
                    })
        }
    }

    fun loadNews() {
        addSubscription(githubDataManager.loadNews()
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe {
                    viewState.showNews(it)
                })
    }
}
