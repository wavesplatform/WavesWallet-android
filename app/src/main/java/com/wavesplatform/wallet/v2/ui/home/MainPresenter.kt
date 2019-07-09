/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsSingle
import com.vicpin.krealmextensions.queryAsSingle
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.sdk.utils.WavesConstants
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.model.db.AssetInfoDb
import com.wavesplatform.wallet.v2.data.model.db.SpamAssetDb
import com.wavesplatform.wallet.v2.data.model.db.TransactionDb
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.PrefsUtil
import com.wavesplatform.wallet.v2.util.WavesWallet
import com.wavesplatform.wallet.v2.util.getTransactionType
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Function3
import pyxis.uzuki.live.richutilskt.utils.runAsync
import javax.inject.Inject

@InjectViewState
class MainPresenter @Inject constructor() : BasePresenter<MainView>() {

    var checkedAboutFundsOnDevice = false
    var checkedAboutBackup = false
    var checkedAboutTerms = false

    fun isAllCheckedToStart(): Boolean {
        return checkedAboutBackup && checkedAboutFundsOnDevice && checkedAboutTerms
    }

    fun reloadTransactionsAfterSpamSettingsChanged(afterUrlChanged: Boolean = false) {
        runAsync {
            val singleData: Single<List<TransactionDb>> = if (afterUrlChanged) {
                queryAsSingle {
                    `in`("transactionTypeId", arrayOf(
                            Constants.ID_MASS_SPAM_RECEIVE_TYPE,
                            Constants.ID_SPAM_RECEIVE_TYPE,
                            Constants.ID_RECEIVED_TYPE,
                            Constants.ID_MASS_RECEIVE_TYPE))
                }
            } else {
                val enableSpamFilter = prefsUtil.getValue(PrefsUtil.KEY_ENABLE_SPAM_FILTER, true)
                if (!enableSpamFilter) {
                    queryAsSingle {
                        `in`("transactionTypeId", arrayOf(
                                Constants.ID_RECEIVED_TYPE,
                                Constants.ID_MASS_RECEIVE_TYPE))
                    }
                } else {
                    queryAsSingle {
                        `in`("transactionTypeId", arrayOf(
                                Constants.ID_MASS_SPAM_RECEIVE_TYPE,
                                Constants.ID_SPAM_RECEIVE_TYPE))
                    }
                }
            }

            addSubscription(Observable.zip(
                    singleData.toObservable(),
                    queryAllAsSingle<SpamAssetDb>().toObservable()
                            .map { spamListFromDb ->
                                if (prefsUtil.getValue(PrefsUtil.KEY_ENABLE_SPAM_FILTER, true)) {
                                    return@map spamListFromDb
                                } else {
                                    return@map listOf<SpamAssetDb>()
                                }
                            },
                    queryAllAsSingle<AssetInfoDb>().toObservable(),
                    Function3 { t1: List<TransactionDb>, t2: List<SpamAssetDb>, t3: List<AssetInfoDb> ->
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
                                AssetInfoDb(WavesConstants.WAVES_ASSET_INFO)
                            } else {
                                assetsInfoListFromDb.firstOrNull { it.id == transaction.assetId }
                            }
                            transaction.transactionTypeId = getTransactionType(
                                    transaction.convertFromDb(), WavesWallet.getAddress())
                        }

                        transactionListFromDb.saveAll()

                        rxEventBus.post(Events.NeedUpdateHistoryScreen())
                    })
        }
    }

    fun loadNews() {
        addSubscription(githubServiceManager.loadNews()
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe {
                    viewState.showNews(it)
                })
    }
}
