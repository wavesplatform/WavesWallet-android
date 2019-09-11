/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.your_assets

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsSingle
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread

import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.db.AssetBalanceDb
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.PrefsUtil
import com.wavesplatform.wallet.v2.util.executeInBackground
import pyxis.uzuki.live.richutilskt.utils.runAsync
import javax.inject.Inject

@InjectViewState
class YourAssetsPresenter @Inject constructor() : BasePresenter<YourAssetsView>() {

    fun loadAssets(onlyGatewaysTokens: Boolean = false) {
        runAsync {
            addSubscription(queryAllAsSingle<AssetBalanceDb>()
                    .executeInBackground()
                    .subscribe({ allAssets ->
                        var assets = if (prefsUtil.getValue(PrefsUtil.KEY_ENABLE_SPAM_FILTER, true)) {
                            allAssets.filter { !it.isSpam }
                        } else {
                            allAssets
                        }.sortedByDescending { it.isFavorite }

                        if (onlyGatewaysTokens) {
                            val gatewaysIds = Constants.defaultGateways().associateBy { it }

                            assets = assets.filter { gatewaysIds[it.assetId] != null }
                        }

                        viewState.showAssets(AssetBalanceDb.convertFromDb(assets))
                    }, {
                        it.printStackTrace()
                    }))
        }
    }
}
