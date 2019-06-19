/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.assets

import com.arellomobile.mvp.InjectViewState
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.vicpin.krealmextensions.queryAll
import com.vicpin.krealmextensions.queryAllAsSingle
import com.vicpin.krealmextensions.save
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.sdk.model.response.node.AssetBalanceResponse
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.sdk.utils.notNull
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.helpers.ClearAssetsHelper
import com.wavesplatform.wallet.v2.data.model.db.AssetBalanceDb
import com.wavesplatform.wallet.v2.data.model.db.SpamAssetDb
import com.wavesplatform.wallet.v2.data.model.db.userdb.AssetBalanceStoreDb
import com.wavesplatform.wallet.v2.data.model.local.AssetBalanceMultiItemEntity
import com.wavesplatform.wallet.v2.data.model.local.WalletSectionItem
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.PrefsUtil
import com.wavesplatform.wallet.v2.util.WavesWallet
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import pers.victor.ext.app
import pyxis.uzuki.live.richutilskt.utils.runAsync
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import javax.inject.Inject

@InjectViewState
class AssetsPresenter @Inject constructor() : BasePresenter<AssetsView>() {
    var needToScroll: Boolean = false
    var enableElevation: Boolean = false

    fun loadAssetsBalance(withNetUpdate: Boolean = true) {
        if (!WavesWallet.isAuthenticated()) {
            runOnUiThread {
                viewState.afterFailedLoadAssets()
            }
            return
        }

        viewState.startServiceToLoadData()
        runAsync {
            val savedAssetPrefs = queryAll<AssetBalanceStoreDb>()
            var dbAssets = mutableListOf<AssetBalanceDb>()
            var dbSpamAssets = mutableListOf<SpamAssetDb>()
            addSubscription(Observable.zip(
                    queryAllAsSingle<AssetBalanceDb>().toObservable(),
                    queryAllAsSingle<SpamAssetDb>().toObservable(),
                    BiFunction { assets: List<AssetBalanceDb>, spams: List<SpamAssetDb> ->
                        return@BiFunction Pair(assets, spams)
                    })
                    .subscribeOn(Schedulers.io())
                    .map { pair ->
                        dbSpamAssets = pair.second.toMutableList()
                        dbAssets.forEach { item ->
                            savedAssetPrefs
                                    .firstOrNull { it.assetId == item.assetId }
                                    .notNull { storedAssetBalance ->
                                        item.isFavorite = storedAssetBalance.isFavorite
                                        item.position = storedAssetBalance.position
                                        item.isHidden = storedAssetBalance.isHidden
                                        item.save()
                                    }
                        }
                        dbAssets = removeSpamAssets(
                                AssetBalanceDb.convertToDb(
                                        ClearAssetsHelper.clearUnimportantAssets(
                                                prefsUtil,
                                                AssetBalanceDb.convertFromDb(pair.first))),
                                dbSpamAssets)
                        return@map createTripleSortedLists(dbAssets)
                    }
                    .doOnNext { postSuccess(it, withNetUpdate, true) }
                    .flatMap { updateWithNet(withNetUpdate, AssetBalanceDb.convertFromDb(dbAssets), dbSpamAssets) }
                    .map { netAssetSpamPair ->
                        updateSpamSettingsAndEvent()
                        return@map removeSpamAssets(
                                AssetBalanceDb.convertToDb(netAssetSpamPair.first.toMutableList()),
                                netAssetSpamPair.second.toMutableList())
                    }
                    .map { createTripleSortedLists(it) }
                    .subscribe({
                        postSuccess(it, withNetUpdate, false)
                    }, {
                        it.printStackTrace()
                        runOnUiThread {
                            viewState.afterFailedLoadAssets()
                        }
                    }))
        }
    }

    fun reloadAssetsAfterSpamFilterStateChanged() {
        runAsync {
            addSubscription(Observable.zip(
                    queryAllAsSingle<AssetBalanceDb>().toObservable(),
                    queryAllAsSingle<SpamAssetDb>().toObservable(),
                    BiFunction { t1: List<AssetBalanceDb>, t2: List<SpamAssetDb> ->
                        return@BiFunction Pair(t1, t2)
                    })
                    .map { pairOfData ->
                        return@map removeSpamAssets(
                                pairOfData.first.toMutableList(),
                                pairOfData.second.toMutableList())
                    }
                    .map { createTripleSortedLists(it) }
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({
                        postSuccess(it, withNetUpdate = false, fromDb = true)
                    }, {
                        it.printStackTrace()
                        viewState.afterFailedUpdateAssets()
                    }))
        }
    }

    private fun removeSpamAssets(assetsListFromDb: MutableList<AssetBalanceDb>,
                                 spamListFromDb: MutableList<SpamAssetDb>)
            : MutableList<AssetBalanceDb> {
        assetsListFromDb.forEach { asset ->
            asset.isSpam = spamListFromDb.any { it.assetId == asset.assetId }
            if (assetsListFromDb.any { it.position != -1 }) {
                if (asset.position == -1) {
                    asset.position = assetsListFromDb.size + 1
                }
            }
            if (asset.isSpam) {
                asset.isFavorite = false
            }
        }

        assetsListFromDb.saveAll()
        AssetBalanceStoreDb.saveAssetBalanceStore(AssetBalanceDb.convertFromDb(assetsListFromDb))
        return assetsListFromDb
    }

    fun reloadAssetsAfterSpamUrlChanged() {
        prefsUtil.setValue(PrefsUtil.KEY_NEED_UPDATE_TRANSACTION_AFTER_CHANGE_SPAM_SETTINGS, true)
        runAsync {
            addSubscription(nodeServiceManager.loadSpamAssets()
                    .flatMap { newSpamAssets ->
                        Observable.zip(
                                queryAllAsSingle<AssetBalanceDb>().toObservable(),
                                Observable.just(SpamAssetDb.convertToDb(newSpamAssets)),
                                BiFunction { t1: List<AssetBalanceDb>, t2: List<SpamAssetDb> ->
                                    return@BiFunction Pair(t1, t2)
                                })
                    }
                    .map {
                        updateSpamSettingsAndEvent()
                        return@map it
                    }
                    .map { pairOfData ->
                        return@map removeSpamAssets(
                                pairOfData.first.toMutableList(),
                                pairOfData.second.toMutableList())
                    }
                    .map { createTripleSortedLists(it.toMutableList()) }
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({
                        postSuccess(it, false, true)
                    }, {
                        it.printStackTrace()
                        viewState.afterFailedUpdateAssets()
                    }))
        }
    }

    private fun updateSpamSettingsAndEvent() {
        if (prefsUtil.getValue(PrefsUtil.KEY_NEED_UPDATE_TRANSACTION_AFTER_CHANGE_SPAM_SETTINGS, false)) {
            rxEventBus.post(Events.SpamFilterUrlChanged(true))
        }
        prefsUtil.setValue(PrefsUtil.KEY_NEED_UPDATE_TRANSACTION_AFTER_CHANGE_SPAM_SETTINGS, false)
    }

    private fun updateWithNet( // todo check
            withNetUpdate: Boolean,
            assets: List<AssetBalanceResponse>,
            spams: List<SpamAsset>): Observable<Pair<List<AssetBalanceResponse>, List<SpamAssetDb>>> {
        return if (withNetUpdate) {
            nodeServiceManager.loadAssets(assets)
        } else {
            Observable.just(Pair(assets, spams))
        }
    }

    private fun postSuccess(it: Triple<MutableList<AssetBalanceDb>, MutableList<AssetBalanceDb>,
            MutableList<AssetBalanceDb>>, withNetUpdate: Boolean, fromDb: Boolean) {
        val listToShow = arrayListOf<MultiItemEntity>()

        val searchItem = MultiItemEntity {
            AssetsAdapter.TYPE_SEARCH
        }
        listToShow.add(searchItem)

        // add all main assets
        val assetBalances = mutableListOf<AssetBalanceMultiItemEntity>()
        it.first.forEach {
            assetBalances.add(AssetBalanceMultiItemEntity(it))
        }
        listToShow.addAll(assetBalances)

        // check if hidden assets exists and create section with them
        if (it.second.isNotEmpty()) {
            val hiddenSection = WalletSectionItem(app.getString(R.string.wallet_assets_hidden_category,
                    it.second.size.toString()))
            it.second.forEach {
                hiddenSection.addSubItem(AssetBalanceMultiItemEntity(it))
            }
            listToShow.add(hiddenSection)
        }

        // check if spam assets exists and create section with them
        val enableSpamFilter = prefsUtil.getValue(PrefsUtil.KEY_ENABLE_SPAM_FILTER, true)
        if (!enableSpamFilter && it.third.isNotEmpty()) {
            val spamSection = WalletSectionItem(app.getString(R.string.wallet_assets_spam_category,
                    it.third.size.toString()))
            it.third.forEach {
                spamSection.addSubItem(AssetBalanceMultiItemEntity(it))
            }
            listToShow.add(spamSection)
        }

        // show all assets with sections
        runOnUiThread {
            viewState.afterSuccessLoadAssets(listToShow, fromDb, withNetUpdate)
        }
    }

    private fun createTripleSortedLists(list: MutableList<AssetBalanceDb>):
            Triple<MutableList<AssetBalanceDb>, MutableList<AssetBalanceDb>,
                    MutableList<AssetBalanceDb>> {
        val hiddenList = list
                .filter { it.isHidden && !it.isSpam }
                .sortedBy { it.position }
                .toMutableList()
        val sortedToFirstFavoriteList = list
                .asSequence()
                .filter { !it.isHidden && !it.isSpam }
                .sortedByDescending { it.isGateway }
                .sortedBy { it.position }
                .sortedByDescending { it.isFavorite }
                .toMutableList()
        val spamList = list
                .filter { it.isSpam }
                .toMutableList()
        return Triple(sortedToFirstFavoriteList, hiddenList, spamList)
    }

    fun loadAliases() {
        if (WavesWallet.isAuthenticated()) {
            addSubscription(dataServiceManager.loadAliases()
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe {})
        }
    }
}
