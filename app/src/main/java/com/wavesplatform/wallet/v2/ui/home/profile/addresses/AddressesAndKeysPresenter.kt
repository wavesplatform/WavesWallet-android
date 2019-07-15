/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.profile.addresses

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsSingle
import com.wavesplatform.wallet.v2.data.model.db.AliasDb
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.sdk.utils.RxUtil
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import javax.inject.Inject

@InjectViewState
class AddressesAndKeysPresenter @Inject constructor() : BasePresenter<AddressesAndKeysView>() {

    fun loadAliases() {
            addSubscription(
                    queryAllAsSingle<AliasDb>().toObservable()
                            .map { aliases ->
                                val ownAliases = aliases.filter { it.own }
                                runOnUiThread { viewState.afterSuccessLoadAliases(AliasDb.convertFromDb(ownAliases)) }
                            }
                            .flatMap { dataServiceManager.loadAliases() }
                            .compose(RxUtil.applyObservableDefaultSchedulers())
                            .subscribe { aliases ->
                                runOnUiThread { viewState.afterSuccessLoadAliases(aliases.toMutableList()) }
                            })
    }
}
