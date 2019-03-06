package com.wavesplatform.wallet.v2.ui.home.wallet.your_assets

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAsSingle
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import pyxis.uzuki.live.richutilskt.utils.runAsync
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import javax.inject.Inject

@InjectViewState
class YourAssetsPresenter @Inject constructor() : BasePresenter<YourAssetsView>() {

    var greaterZeroBalance: Boolean = false

    fun loadAssets(greaterZeroBalance: Boolean) {
        this.greaterZeroBalance = greaterZeroBalance
        runAsync {
            val favorite: Single<List<AssetBalance>>
            val notFavorite: Single<List<AssetBalance>>
            if (this.greaterZeroBalance) {
                favorite = queryAsSingle {
                    equalTo("isFavorite", true)
                            .greaterThan("balance", 0)
                }
                notFavorite = queryAsSingle {
                    equalTo("isFavorite", false)
                            .greaterThan("balance", 0)
                }
            } else {
                favorite = queryAsSingle { equalTo("isFavorite", true) }
                notFavorite = queryAsSingle { equalTo("isFavorite", false) }
            }

            addSubscription(Single.zip(favorite, notFavorite,
                    BiFunction<List<AssetBalance>, List<AssetBalance>, Pair<List<AssetBalance>,
                            List<AssetBalance>>> { t1, t2 ->
                        return@BiFunction Pair(t1, t2)
                    }).compose(RxUtil.applySingleDefaultSchedulers())
                    .subscribe({
                        val assets = mutableListOf<AssetBalance>()
                        assets.addAll(it.first)
                        assets.addAll(it.second)

                        val filteredSpamAssets = if (prefsUtil.getValue(PrefsUtil.KEY_ENABLE_SPAM_FILTER, true)) {
                            assets.filter { !it.isSpam }.toMutableList()
                        } else {
                            assets
                        }
                        runOnUiThread {
                            viewState.showAssets(filteredSpamAssets)
                        }
                    }, {
                        it.printStackTrace()
                    }))
        }
    }

    fun loadCryptoAssets(greaterZeroBalance: Boolean) {
        this.greaterZeroBalance = greaterZeroBalance
        runAsync {
            val singleData: Single<List<AssetBalance>> = if (this.greaterZeroBalance) {
                queryAsSingle {
                    greaterThan("balance", 0)
                            .`in`("assetId", Constants.defaultCrypto())
                }
            } else {
                queryAsSingle {
                    `in`("assetId", Constants.defaultCrypto())
                }
            }

            addSubscription(singleData
                    .map {
                        return@map it
                    }
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        val assets = mutableListOf<AssetBalance>()
                        assets.addAll(it)
                        runOnUiThread {
                            viewState.showAssets(assets)
                        }
                    }, {
                    }))
        }
    }
}
