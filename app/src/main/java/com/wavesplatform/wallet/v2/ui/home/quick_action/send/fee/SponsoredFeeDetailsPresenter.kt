package com.wavesplatform.wallet.v2.ui.home.quick_action.send.fee

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.local.SponsoredAssetItem
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import com.wavesplatform.wallet.v2.util.clearBalance
import com.wavesplatform.wallet.v2.util.stripZeros
import javax.inject.Inject

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
                        }))
    }

    private fun isValidBalanceForSponsoring(item: AssetBalance, fee: String): Boolean {
        val sponsorBalance = MoneyUtil.getScaledText(item.getSponsorBalance(), Constants.wavesAssetInfo.precision).clearBalance().toBigDecimal()
        val feeDecimalValue = fee.clearBalance().toBigDecimal()
        val availableBalance = MoneyUtil.getScaledText(item.getAvailableBalance()
                ?: 0, item.getDecimals()).clearBalance().toBigDecimal()

        return ((sponsorBalance >= Constants.MIN_WAVES_SPONSORED_BALANCE.toBigDecimal() && availableBalance >= feeDecimalValue) ||

                (sponsorBalance >= MoneyUtil.getScaledText(wavesFee, Constants.wavesAssetInfo.precision).clearBalance().toBigDecimal() &&
                        availableBalance >= feeDecimalValue &&
                        item.isMyWavesToken()) ||

                item.isWaves())
    }

    private fun getFee(item: AssetBalance): String {
        return if (item.isWaves()) {
            MoneyUtil.getScaledText(wavesFee, Constants.wavesAssetInfo.precision).stripZeros()
        } else {
            calculateFeeForSponsoredAsset(item).stripZeros()
        }
    }

    private fun calculateFeeForSponsoredAsset(item: AssetBalance): String {
        val sponsorFee = MoneyUtil.getScaledText(item.minSponsoredAssetFee, item).clearBalance().toBigDecimal()
        val value = ((MoneyUtil.getScaledText(wavesFee, Constants.wavesAssetInfo.precision).clearBalance().toBigDecimal() /
                MoneyUtil.getScaledText(Constants.WAVES_MIN_FEE, Constants.wavesAssetInfo.precision).clearBalance().toBigDecimal()) * sponsorFee)

        return value.toString()
    }
}
