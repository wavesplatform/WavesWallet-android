/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.splash

import android.content.Intent
import android.text.TextUtils
import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.sdk.model.response.data.AssetInfoResponse
import com.wavesplatform.sdk.model.response.matcher.MarketResponse
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.util.PrefsUtil
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import java.net.URI
import javax.inject.Inject

@InjectViewState
class SplashPresenter @Inject constructor() : BasePresenter<SplashView>() {

    fun storeIncomingURI(intent: Intent) {
        val action = intent.action
        val scheme = intent.scheme
        if (action != null && Intent.ACTION_VIEW == action && scheme != null && scheme == "waves") {
            prefsUtil.setGlobalValue(PrefsUtil.GLOBAL_SCHEME_URL, intent.data!!.toString())
        }
    }

    fun resolveNextAction() {
        if (TextUtils.isEmpty(App.getAccessManager().getLoggedInGuid())) {
            viewState.onNotLoggedIn()
        } else {
            val pubKey = prefsUtil.getValue(PrefsUtil.KEY_PUB_KEY, "")
            viewState.onStartMainActivity(pubKey)
        }
    }

    fun loadMarkets(url: String) {
        val amountPricePair = getPair(url)
        addSubscription(
                dataServiceManager.assets(ids = listOf(amountPricePair.first, amountPricePair.second))
                        .compose(RxUtil.applyObservableDefaultSchedulers())
                        .subscribe({ assetInfoResponseList ->
                            val response = createMarketResponse(
                                    amountPricePair, assetInfoResponseList)
                            viewState.openDex(response)
                        }, {
                            it.printStackTrace()
                        }))
    }

    private fun getPair(url: String?): Pair<String, String> {
        if (url.isNullOrEmpty()) {
            return Pair("", "")
        }

        if (url.contains("https://beta.wavesplatform.com/dex".toRegex())) {
            val uri = URI.create(url)
            val params = uri.query.split("&")
            var assetId1 = ""
            var assetId2 = ""
            for (parameter in params) {
                if (parameter.contains("assetId1=")) {
                    assetId1 = parameter.replace("assetId1=", "")
                }
                if (parameter.contains("assetId2=")) {
                    assetId2 = parameter.replace("assetId2=", "")
                }
            }
            return Pair(assetId1, assetId2)
        } else {
            return Pair("", "")
        }
    }

    private fun createMarketResponse(pair: Pair<String, String>,
                                     assets: List<AssetInfoResponse> = mutableListOf()): MarketResponse {

        val market = MarketResponse()

        val amountAsset = assets.firstOrNull { it.id == pair.first }
        val priceAsset = assets.firstOrNull { it.id == pair.second }

        market.id = pair.first + pair.second

        market.amountAsset = pair.first
        market.priceAsset = pair.second

        market.amountAssetLongName = amountAsset?.name
        market.priceAssetLongName = priceAsset?.name

        market.amountAssetShortName = amountAsset?.ticker ?: amountAsset?.name
        market.priceAssetShortName = priceAsset?.ticker ?: priceAsset?.name

        market.amountAssetDecimals = amountAsset?.precision ?: 8
        market.priceAssetDecimals = priceAsset?.precision ?: 8

        return market
    }
}
