/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsSingle
import com.vicpin.krealmextensions.queryAsSingle
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.sdk.crypto.WavesCrypto
import com.wavesplatform.sdk.utils.Identicon
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.sdk.utils.WavesConstants
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.model.db.AssetInfoDb
import com.wavesplatform.wallet.v2.data.model.db.SpamAssetDb
import com.wavesplatform.wallet.v2.data.model.db.TransactionDb
import com.wavesplatform.wallet.v2.data.model.db.userdb.AddressBookUserDb
import com.wavesplatform.wallet.v2.data.model.local.MigrateAccountItem
import com.wavesplatform.wallet.v2.data.model.local.widget.MyAccountItem
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.EnvironmentManager
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

    fun getWalletName(): String {
        return App.getAccessManager().getWalletName(App.getAccessManager().getLoggedInGuid())
    }

    fun getAddresses() {
        // TODO: Multi account logic here
        val list = arrayListOf<MyAccountItem>()

        list.add(MyAccountItem(R.string.migrate_account_successfully_unlocked_header))

        val guids = prefsUtil.getGlobalValueList(
                EnvironmentManager.name + PrefsUtil.LIST_WALLET_GUIDS)
        for (i in guids.indices) {
            val publicKey = prefsUtil.getGlobalValue(guids[i] + PrefsUtil.KEY_PUB_KEY, "")
            val name = prefsUtil.getGlobalValue(guids[i] + PrefsUtil.KEY_WALLET_NAME, "")
            val address = WavesCrypto.addressFromPublicKey(WavesCrypto.base58decode(publicKey), EnvironmentManager.netCode)
            list.add(MyAccountItem(AddressBookUserDb(address, name), locked = false, active = true))
        }

        //TODO: Remove test
        list.add(MyAccountItem(R.string.migrate_account_pending_unlock_header))
        list.add(MyAccountItem(AddressBookUserDb("test", "test"), locked = true, active = false))

        viewState.afterSuccessGetAddress(list)
    }
}
