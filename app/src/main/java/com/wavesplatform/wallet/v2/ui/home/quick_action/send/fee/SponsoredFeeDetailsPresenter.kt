/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.quick_action.send.fee

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.sdk.utils.WavesConstants
import com.wavesplatform.sdk.model.response.AssetBalanceResponse
import com.wavesplatform.sdk.utils.MoneyUtil
import com.wavesplatform.sdk.utils.clearBalance
import com.wavesplatform.sdk.utils.stripZeros
import com.wavesplatform.wallet.v2.data.model.local.SponsoredAssetItem
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.wallet.v2.util.WavesWallet
import javax.inject.Inject

@InjectViewState
class SponsoredFeeDetailsPresenter @Inject constructor() : BasePresenter<SponsoredFeeDetailsView>() {

    var wavesFee: Long = WavesConstants.WAVES_MIN_FEE

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
                        }))
    }

    private fun isValidBalanceForSponsoring(item: AssetBalanceResponse, fee: String): Boolean {
        val sponsorBalance = MoneyUtil.getScaledText(item.getSponsorBalance(), WavesConstants.WAVES_ASSET_INFO.precision).clearBalance().toBigDecimal()
        val feeDecimalValue = fee.clearBalance().toBigDecimal()
        val availableBalance = MoneyUtil.getScaledText(
                item.getAvailableBalance(), item.getDecimals()).clearBalance().toBigDecimal()

        return ((sponsorBalance >= WavesConstants.MIN_WAVES_SPONSORED_BALANCE.toBigDecimal()
                && availableBalance >= feeDecimalValue)
                || (sponsorBalance >= MoneyUtil.getScaledText(
                        wavesFee, WavesConstants.WAVES_ASSET_INFO.precision)
                        .clearBalance()
                        .toBigDecimal()
                        && availableBalance >= feeDecimalValue
                        && item.isMyWavesToken(WavesWallet.getAddress()))
                || item.isWaves())
    }

    private fun getFee(item: AssetBalanceResponse): String {
        return if (item.isWaves()) {
            MoneyUtil.getScaledText(wavesFee, WavesConstants.WAVES_ASSET_INFO.precision).stripZeros()
        } else {
            calculateFeeForSponsoredAsset(item).stripZeros()
        }
    }

    private fun calculateFeeForSponsoredAsset(item: AssetBalanceResponse): String {
        val sponsorFee = MoneyUtil.getScaledText(item.minSponsoredAssetFee, item).clearBalance().toBigDecimal()
        val value = ((MoneyUtil.getScaledText(wavesFee, WavesConstants.WAVES_ASSET_INFO.precision).clearBalance().toBigDecimal() /
                MoneyUtil.getScaledText(WavesConstants.WAVES_MIN_FEE, WavesConstants.WAVES_ASSET_INFO.precision).clearBalance().toBigDecimal()) * sponsorFee)

        return value.toString()
    }
}
