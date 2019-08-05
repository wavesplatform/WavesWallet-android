package com.wavesplatform.wallet.v2.ui.widget

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.sdk.model.request.data.PairRequest
import com.wavesplatform.sdk.model.response.data.AssetInfoResponse
import com.wavesplatform.sdk.model.response.data.SearchPairResponse
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.sdk.utils.WavesConstants
import com.wavesplatform.sdk.utils.isWavesId
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.widget.adapters.TokenAdapter
import com.wavesplatform.wallet.v2.util.EnvironmentManager
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import javax.inject.Inject


@InjectViewState
class MarketWidgetConfigurePresenter @Inject constructor() : BasePresenter<MarketWidgetConfigureView>() {

    fun loadAssets(assets: List<String>) {

        fun getFilledPairs(pairs: SearchPairResponse, assets: MutableList<String>)
                : MutableList<SearchPairResponse.Pair> {
            val filledResult = mutableListOf<SearchPairResponse.Pair>()
            for (index in 0 until pairs.data.size) {
                if (pairs.data[index].data != null) {
                    val pair = assets[index].split("/")
                    pairs.data[index].amountAsset = pair[0]
                    pairs.data[index].priceAsset = pair[1]
                    filledResult.add(pairs.data[index])
                }
            }
            return filledResult
        }

        fun createPairs(assets: List<String>): MutableList<String> {
            val initPairsList = mutableListOf<String>()

            val usdAsset = EnvironmentManager.defaultAssets.firstOrNull {
                it.issueTransaction?.name == "US Dollar"
            }

            for (priceAssetId in assets) {
                if (priceAssetId.isWavesId()) {
                    initPairsList.add("${WavesConstants.WAVES_ASSET_ID_FILLED}/${usdAsset?.assetId}")
                    continue
                } else {
                    initPairsList.add("${WavesConstants.WAVES_ASSET_ID_FILLED}/$priceAssetId")
                    initPairsList.add("$priceAssetId/${WavesConstants.WAVES_ASSET_ID_FILLED}")
                }
            }
            return initPairsList
        }

        val pairsList = createPairs(assets)
        addSubscription(
                Observable.zip(dataServiceManager.assets(ids = assets),
                        dataServiceManager.loadPairs(PairRequest(pairs = pairsList, limit = 200))
                                .flatMap { pairs ->
                                    Observable.just(getFilledPairs(pairs, pairsList))
                                },
                        BiFunction { t1: List<AssetInfoResponse>, t2: List<SearchPairResponse.Pair> ->
                            return@BiFunction Pair(t1, t2)
                        })
                        .compose(RxUtil.applyObservableDefaultSchedulers())
                        .subscribe({ pair ->
                            val tokenPairList = arrayListOf<TokenAdapter.TokenPair>()
                            pair.second.forEach { assetPair ->
                                tokenPairList.add(TokenAdapter.TokenPair(
                                        pair.first.first { it.id == assetPair.amountAsset },
                                        assetPair))
                            }
                            viewState.updatePairs(tokenPairList)
                        }, {
                            it.printStackTrace()
                            viewState.fail()
                        })
        )
    }

    fun loadPair(assetInfo: AssetInfoResponse) {
        addSubscription(dataServiceManager.loadPairs(searchByAsset = assetInfo.id)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe(
                        { result ->
                            viewState.updatePair(assetInfo, result)
                        },
                        {
                            it.printStackTrace()
                            viewState.fail()
                        }))
    }
}