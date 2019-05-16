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
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.helpers.ClearAssetsHelper
import com.wavesplatform.wallet.v2.data.model.local.WalletSectionItem
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.SpamAsset
import com.wavesplatform.wallet.v2.data.model.userdb.AssetBalanceStore
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import com.wavesplatform.wallet.v2.util.notNull
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

    fun loadAssetsBalance(withApiUpdate: Boolean = true) {
        if (App.getAccessManager().getWallet() == null) {
            runOnUiThread {
                viewState.afterFailedLoadAssets()
            }
            return
        }

        runAsync {
            val savedAssetPrefs = queryAll<AssetBalanceStore>()
            var dbAssets = mutableListOf<AssetBalance>()
            var dbSpamAssets = mutableListOf<SpamAsset>()
            addSubscription(Observable.zip(
                    queryAllAsSingle<AssetBalance>().toObservable(),
                    queryAllAsSingle<SpamAsset>().toObservable(),
                    BiFunction { t1: List<AssetBalance>, t2: List<SpamAsset> ->
                        return@BiFunction Pair(t1, t2)
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
                                ClearAssetsHelper.clearUnimportantAssets(
                                        prefsUtil, pair.first.toMutableList()),
                                pair.second.toMutableList())
                        return@map createTripleSortedLists(dbAssets)
                    }
                    .doOnNext { postSuccess(it, withApiUpdate, true) }
                    .flatMap { tryUpdateWithApi(withApiUpdate, dbAssets) }
                    .map { netAssetDb ->
                        updateSpamSettingsAndEvent()
                        return@map removeSpamAssets(netAssetDb.toMutableList(), dbSpamAssets)
                    }
                    .map { createTripleSortedLists(it) }
                    .subscribe({
                        postSuccess(it, withApiUpdate, false)
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
                    queryAllAsSingle<AssetBalance>().toObservable(),
                    queryAllAsSingle<SpamAsset>().toObservable(),
                    BiFunction { t1: List<AssetBalance>, t2: List<SpamAsset> ->
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
                        postSuccess(it, withApiUpdate = false, fromDb = true)
                    }, {
                        it.printStackTrace()
                        viewState.afterFailedUpdateAssets()
                    }))
        }
    }

    private fun removeSpamAssets(assetBalances: MutableList<AssetBalance>, spams: MutableList<SpamAsset>)
            : MutableList<AssetBalance> {
        assetBalances.forEach { asset ->
            asset.isSpam = spams.any { it.assetId == asset.assetId }
            if (assetBalances.any { it.position != -1 }) {
                if (asset.position == -1) {
                    asset.position = assetBalances.size + 1
                }
            }
            if (asset.isSpam) {
                asset.isFavorite = false
            }
        }
        assetBalances.saveAll()
        AssetBalanceStore.saveAssetBalanceStore(assetBalances)
        return assetBalances
    }

    fun reloadAssetsAfterSpamUrlChanged() {
        prefsUtil.setValue(PrefsUtil.KEY_NEED_UPDATE_TRANSACTION_AFTER_CHANGE_SPAM_SETTINGS, true)
        runAsync {
            addSubscription(nodeDataManager.loadSpamAssets()
                    .flatMap { newSpamAssets ->
                        Observable.zip(
                                queryAllAsSingle<AssetBalance>().toObservable(),
                                Observable.just(newSpamAssets),
                                BiFunction { t1: List<AssetBalance>, t2: List<SpamAsset> ->
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

    private fun tryUpdateWithApi(withApiUpdate: Boolean, it: List<AssetBalance>): Observable<List<AssetBalance>> {
        return if (withApiUpdate) {
            nodeDataManager.loadAssets(it)
        } else {
            Observable.just(it)
        }
    }

    private fun postSuccess(it: Triple<MutableList<AssetBalance>, MutableList<AssetBalance>,
            MutableList<AssetBalance>>, withApiUpdate: Boolean, fromDb: Boolean) {
        val listToShow = arrayListOf<MultiItemEntity>()

        // add all main assets
        listToShow.addAll(it.first)

        // check if hidden assets exists and create section with them
        if (it.second.isNotEmpty()) {
            val hiddenSection = WalletSectionItem(app.getString(R.string.wallet_assets_hidden_category,
                    it.second.size.toString()))
            it.second.forEach {
                hiddenSection.addSubItem(it)
            }
            listToShow.add(hiddenSection)
        }

        // check if spam assets exists and create section with them

        val enableSpamFilter = prefsUtil.getValue(PrefsUtil.KEY_ENABLE_SPAM_FILTER, true)
        if (!enableSpamFilter && it.third.isNotEmpty()) {
            val spamSection = WalletSectionItem(app.getString(R.string.wallet_assets_spam_category,
                    it.third.size.toString()))
            it.third.forEach {
                spamSection.addSubItem(it)
            }
            listToShow.add(spamSection)
        }

        // show all assets with sections
        runOnUiThread {
            viewState.afterSuccessLoadAssets(listToShow, fromDb, withApiUpdate)
        }
    }

    private fun createTripleSortedLists(list: MutableList<AssetBalance>):
            Triple<MutableList<AssetBalance>, MutableList<AssetBalance>, MutableList<AssetBalance>> {
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
        addSubscription(apiDataManager.loadAliases()
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe {
                })
    }
}
