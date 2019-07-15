/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.quick_action.send.fee

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v1.crypto.Base58
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.local.SponsoredAssetItem
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetsDetails
import com.wavesplatform.wallet.v2.data.model.remote.response.MatcherSettings
import com.wavesplatform.wallet.v2.data.model.remote.response.ScriptInfo
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.*
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.ceil

@InjectViewState
class SponsoredFeeDetailsPresenter @Inject constructor() : BasePresenter<SponsoredFeeDetailsView>() {

    var wavesFee: Long = Constants.WAVES_MIN_FEE

    fun loadSponsoredAssets(listener: (MutableList<SponsoredAssetItem>) -> Unit) {
        addSubscription(
                nodeDataManager.assetsBalances()
                        .flatMapIterable { it }
                        .filter { it.isSponsored() || it.isWaves() }
                        .map {
                            val fee = getFee(it)
                            val isActive = isValidBalanceForSponsoring(it, fee)
                            return@map SponsoredAssetItem(it, fee, isActive)
                        }
                        .toList()
                        .map {
                            it.sortedByDescending { it.isActive }.toMutableList()
                        }
                        .compose(RxUtil.applySingleDefaultSchedulers())
                        .subscribe({
                            listener.invoke(it)
                        }, {
                            it.printStackTrace()
                            viewState.showNetworkError()
                        }))
    }


    fun loadExchangeCommission(amountAssetId: String?,
                               priceAssetId: String?,
                               listener: (MutableList<SponsoredAssetItem>) -> Unit) {

        viewState.showProgressBar(true)

        wavesFee = Constants.WAVES_ORDER_MIN_FEE
        var addressMatcherScripted = false
        var amountAssetScripted = false
        var priceAssetScripted = false

        addSubscription(matcherDataManager.getMatcherKey()
                .flatMap { matcherPublicKey ->
                    Observable.zip(
                            nodeDataManager.scriptAddressInfo(
                                    AddressUtil.addressFromPublicKey(Base58.decode(matcherPublicKey))),
                            nodeDataManager.assetDetails(amountAssetId),
                            nodeDataManager.assetDetails(priceAssetId),
                            io.reactivex.functions.Function3 { addressMatcherScripted: ScriptInfo,
                                                               amountAssetDetails: AssetsDetails,
                                                               priceAssetDetails: AssetsDetails ->
                                return@Function3 Triple(
                                        addressMatcherScripted, amountAssetDetails, priceAssetDetails)
                            })
                }
                .flatMap { detailsInfoTriple ->
                    addressMatcherScripted = detailsInfoTriple.first.extraFee != 0L
                    amountAssetScripted = detailsInfoTriple.second.scripted
                    priceAssetScripted = detailsInfoTriple.third.scripted

                    Observable.zip(
                            matcherDataManager.getSettings(),
                            matcherDataManager.getSettingsRates(),
                            nodeDataManager.assetsBalances(),
                            io.reactivex.functions.Function3 { settings: MatcherSettings,
                                                               rates: MutableMap<String, Double>,
                                                               balances: MutableList<AssetBalance> ->
                                return@Function3 Triple(settings, rates, balances)
                            })
                }
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ triple ->
                    val matcherSettings = triple.first
                    val settingsRates = triple.second
                    val assets = triple.third

                    val sponsoredAssetItems = mutableListOf<SponsoredAssetItem>()
                    settingsRates.forEach { (assetId, rate) ->

                        val assetIdWavesChecked = if (assetId == Constants.WAVES_ASSET_ID_FILLED) {
                            Constants.WAVES_ASSET_ID_EMPTY
                        } else {
                            assetId
                        }

                        val assetBalance = assets.find { it.assetId == assetIdWavesChecked }
                        assetBalance.notNull { matcherFeeAssetBalance ->

                            val executedScripts = executedScripts(
                                    matcherFeeAssetBalance.isScripted(),
                                    addressMatcherScripted,
                                    amountAssetScripted,
                                    priceAssetScripted)

                            val baseFee = matcherSettings.orderFee[MatcherSettings.DYNAMIC]?.baseFee!!
                            val minFee = countMinFee(rate, baseFee, executedScripts)

                            if (minFee < matcherFeeAssetBalance.balance ?: 0) {
                                sponsoredAssetItems.add(SponsoredAssetItem(
                                        matcherFeeAssetBalance,
                                        MoneyUtil.getScaledText(
                                                minFee,
                                                matcherFeeAssetBalance.getDecimals()).stripZeros(),
                                        true))
                            }
                        }
                    }
                    listener.invoke(sponsoredAssetItems)
                    viewState.showProgressBar(false)
                }, {
                    it.printStackTrace()
                    viewState.showNetworkError()
                }))
    }


    private fun executedScripts(matcherFeeAssetScripted: Boolean,
                                matcherAddressScripted: Boolean,
                                amountAssetScripted: Boolean,
                                priceAssetScripted: Boolean): Int {
        var executedScripts = 0

        if (matcherFeeAssetScripted) {
            executedScripts++
        }

        if (matcherAddressScripted) {
            executedScripts++
        }

        if (amountAssetScripted) {
            executedScripts++
        }

        if (priceAssetScripted) {
            executedScripts++
        }

        return executedScripts
    }

    private fun countMinFee(rate: Double, baseFee: Long, executedScripts: Int): Long {
        return ceil(rate * (baseFee + 400_000 * executedScripts)).toLong()
    }

    private fun isValidBalanceForSponsoring(item: AssetBalance, fee: String): Boolean {
        val sponsorBalance = MoneyUtil.getScaledText(item.getSponsorBalance(),
                Constants.wavesAssetInfo.precision).clearBalance().toBigDecimal()
        val feeDecimalValue = fee.clearBalance().toBigDecimal()
        val availableBalance = MoneyUtil.getScaledText(
                item.getAvailableBalance(), item.getDecimals()).clearBalance().toBigDecimal()

        return ((sponsorBalance >= Constants.MIN_WAVES_SPONSORED_BALANCE.toBigDecimal()
                && availableBalance >= feeDecimalValue)
                || (sponsorBalance >= MoneyUtil.getScaledText(
                wavesFee, Constants.wavesAssetInfo.precision).clearBalance().toBigDecimal()
                && availableBalance >= feeDecimalValue
                && item.isMyWavesToken())
                || item.isWaves())
    }

    private fun getFee(item: AssetBalance): String {
        return if (item.isWaves()) {
            MoneyUtil.getScaledText(wavesFee, Constants.wavesAssetInfo.precision).stripZeros()
        } else {
            calculateFeeForSponsoredAsset(item).stripZeros()
        }
    }

    private fun calculateFeeForSponsoredAsset(item: AssetBalance): String {
        val sponsorFee = MoneyUtil.getScaledText(item.minSponsoredAssetFee, item)
                .clearBalance().toBigDecimal()
        val value = ((MoneyUtil.getScaledText(wavesFee, Constants.wavesAssetInfo.precision)
                .clearBalance().toBigDecimal() /
                MoneyUtil.getScaledText(Constants.WAVES_MIN_FEE, Constants.wavesAssetInfo.precision)
                        .clearBalance().toBigDecimal()) * sponsorFee)

        return value.toPlainString()
    }
}
