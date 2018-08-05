package com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.success

import android.os.Bundle
import javax.inject.Inject

import com.arellomobile.mvp.presenter.InjectPresenter

import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity;

import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.wavesplatform.wallet.R
import kotlinx.android.synthetic.main.activity_trade_send_and_buy_sucess.*
import pers.victor.ext.click
import pers.victor.ext.findColor


class TradeBuyAndSendSucessActivity : BaseActivity(), TradeBuyAndSendSucessView {

    @Inject
    @InjectPresenter
    lateinit var presenterAndSend: TradeBuyAndSendSucessPresenter

    @ProvidePresenter
    fun providePresenter(): TradeBuyAndSendSucessPresenter = presenterAndSend

    override fun configLayoutRes() = R.layout.activity_trade_send_and_buy_sucess

    companion object {
        var BUNDLE_OPERATION_TYPE = "operation_type"
        var BUY_TYPE = 0
        var SELL_TYPE = 1
    }


    override fun onViewReady(savedInstanceState: Bundle?) {
        if (intent.getIntExtra(BUNDLE_OPERATION_TYPE, 0) == BUY_TYPE){
            text_status_value.setTextColor(findColor(R.color.submit400))
        }else if (intent.getIntExtra(BUNDLE_OPERATION_TYPE, 0) == SELL_TYPE){
            text_status_value.setTextColor(findColor(R.color.error400))
        }

        button_okay.click {
            finish()
        }
    }

}
