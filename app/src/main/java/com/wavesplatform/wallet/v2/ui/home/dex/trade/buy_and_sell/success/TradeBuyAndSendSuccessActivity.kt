package com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.success

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.analytics.AnalyticEvents
import com.wavesplatform.wallet.v2.data.analytics.analytics
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.TradeBuyAndSellBottomSheetFragment
import com.wavesplatform.wallet.v2.util.safeLet
import kotlinx.android.synthetic.main.activity_trade_send_and_buy_success.*
import pers.victor.ext.click
import pers.victor.ext.findColor
import javax.inject.Inject

class TradeBuyAndSendSuccessActivity : BaseActivity(), TradeBuyAndSendSucessView {

    @Inject
    @InjectPresenter
    lateinit var presenterAndSend: TradeBuyAndSendSucessPresenter

    @ProvidePresenter
    fun providePresenter(): TradeBuyAndSendSucessPresenter = presenterAndSend

    override fun configLayoutRes() = R.layout.activity_trade_send_and_buy_success

    companion object {
        var BUNDLE_OPERATION_TYPE = "operation_type"
        var BUNDLE_AMOUNT_ASSET_NAME = "amount_asset_name"
        var BUNDLE_PRICE_ASSET_NAME = "price_asset_name"
        var BUNDLE_AMOUNT = "amount"
        var BUNDLE_PRICE = "price"
        var BUNDLE_TIME = "time"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        translucentStatusBar = true
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        val type = intent.getIntExtra(BUNDLE_OPERATION_TYPE, TradeBuyAndSellBottomSheetFragment.BUY_TYPE)
        if (type == TradeBuyAndSellBottomSheetFragment.BUY_TYPE) {
            text_status_value.setTextColor(findColor(R.color.submit400))
        } else if (type == TradeBuyAndSellBottomSheetFragment.SELL_TYPE) {
            text_status_value.setTextColor(findColor(R.color.error400))
        }

        text_time_value.text = intent.getStringExtra(BUNDLE_TIME)
        text_price_value.text = intent.getStringExtra(BUNDLE_PRICE)
        text_amount_value.text = intent.getStringExtra(BUNDLE_AMOUNT)

        button_okay.click {
            logEvent(type)
            finish()
        }
    }

    private fun logEvent(type: Int) {
        safeLet(intent.getStringExtra(BUNDLE_AMOUNT_ASSET_NAME), intent.getStringExtra(BUNDLE_PRICE_ASSET_NAME))
        { amountAssetName, priceAssetName ->
            if (type == TradeBuyAndSellBottomSheetFragment.BUY_TYPE) {
                analytics.trackEvent(AnalyticEvents.DEXBuyTapEvent(amountAssetName, priceAssetName))
            } else {
                analytics.trackEvent(AnalyticEvents.DEXSellTapEvent(amountAssetName, priceAssetName))
            }
        }

    }
}
