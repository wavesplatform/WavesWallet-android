package com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.order

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatTextView
import android.widget.Button
import android.widget.EditText
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.google.gson.internal.LinkedTreeMap
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.custom.CounterHandler
import com.wavesplatform.wallet.v2.ui.home.dex.trade.TradeActivity
import com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.TradeBuyAndSellBottomSheetFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.leasing.start.StartLeasingActivity.Companion.TOTAL_BALANCE
import com.wavesplatform.wallet.v2.util.clearBalance
import com.wavesplatform.wallet.v2.util.makeStyled
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.fragment_trade_order.*
import pers.victor.ext.children
import pers.victor.ext.click
import pers.victor.ext.findColor
import java.math.BigDecimal
import javax.inject.Inject


class TradeOrderFragment : BaseFragment(), TradeOrderView {

    @Inject
    @InjectPresenter
    lateinit var presenter: TradeOrderPresenter
    var buttonPositive: Button? = null

    @ProvidePresenter
    fun providePresenter(): TradeOrderPresenter = presenter

    override fun configLayoutRes() = R.layout.fragment_trade_order

    companion object {
        var BUNDLE_ORDER_TYPE = "order_type"

        fun newInstance(watchMarket: WatchMarket?, orderType: Int, initPrice: Long? = null, initAmount: Long? = null): TradeOrderFragment {
            val args = Bundle()
            args.classLoader = WatchMarket::class.java.classLoader
            args.putParcelable(TradeActivity.BUNDLE_MARKET, watchMarket)
            args.putInt(BUNDLE_ORDER_TYPE, orderType)
            initPrice.notNull {
                args.putLong(TradeBuyAndSellBottomSheetFragment.BUNDLE_PRICE, it)
            }
            initAmount.notNull {
                args.putLong(TradeBuyAndSellBottomSheetFragment.BUNDLE_AMOUNT, it)
            }
            val fragment = TradeOrderFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        arguments.notNull {
            presenter.watchMarket = it.getParcelable<WatchMarket>(TradeActivity.BUNDLE_MARKET)
            presenter.orderType = it.getInt(BUNDLE_ORDER_TYPE)

        }

        presenter.getMatcherKey()
        presenter.getBalanceFromAssetPair()

        CounterHandler.Builder()
                .valueView(edit_amount)
                .incrementalView(image_amount_plus)
                .decrementalView(image_amount_minus)
                .listener(object : CounterHandler.CounterListener {
                    override fun onIncrement(view: EditText?, number: BigDecimal) {
                        view?.setText(number.toString())
                    }

                    override fun onDecrement(view: EditText?, number: BigDecimal) {
                        view?.setText(number.toString())
                    }
                })
                .build()

        CounterHandler.Builder()
                .valueView(edit_limit_price)
                .incrementalView(image_limit_price_plus)
                .decrementalView(image_limit_price_minus)
                .listener(object : CounterHandler.CounterListener {
                    override fun onIncrement(view: EditText?, number: BigDecimal) {
                        view?.setText(number.toString())
                    }

                    override fun onDecrement(view: EditText?, number: BigDecimal) {
                        view?.setText(number.toString())
                    }
                })
                .build()

        CounterHandler.Builder()
                .valueView(edit_total_price)
                .incrementalView(image_total_price_plus)
                .decrementalView(image_total_price_minus)
                .listener(object : CounterHandler.CounterListener {
                    override fun onIncrement(view: EditText?, number: BigDecimal) {
                        view?.setText(number.toString())
                    }

                    override fun onDecrement(view: EditText?, number: BigDecimal) {
                        view?.setText(number.toString())
                    }
                })
                .build()

        text_amount_hint.text = getString(R.string.buy_and_sell_amount_in, presenter.watchMarket?.market?.amountAssetShortName)
        text_amount_error.text = getString(R.string.buy_and_sell_not_enough, presenter.watchMarket?.market?.amountAssetShortName)
        text_limit_price_hint.text = getString(R.string.buy_and_sell_limit_price_in, presenter.watchMarket.market.priceAssetShortName)
        text_total_price_hint.text = getString(R.string.buy_and_sell_total_in, presenter.watchMarket?.market?.priceAssetShortName)

        if (presenter.orderType == TradeBuyAndSellBottomSheetFragment.SELL_TYPE) {
            button_confirm.setBackgroundResource(R.drawable.selector_btn_red)
            button_confirm.text = getString(R.string.sell_btn_txt, presenter.watchMarket?.market?.amountAssetShortName)
        } else {
            button_confirm.text = getString(R.string.buy_btn_txt, presenter.watchMarket?.market?.amountAssetShortName)
        }



        presenter.watchMarket.notNull { watchMarket ->
            val amountFromOrderbook = arguments?.getLong(TradeBuyAndSellBottomSheetFragment.BUNDLE_AMOUNT)
            val priceFromOrderbook = arguments?.getLong(TradeBuyAndSellBottomSheetFragment.BUNDLE_PRICE)

            if (amountFromOrderbook != null && priceFromOrderbook != null) {
                val amountUIValue = MoneyUtil.getScaledText(amountFromOrderbook, watchMarket.market.amountAssetDecimals).clearBalance()
                val priceUIValue = MoneyUtil.getScaledText(priceFromOrderbook, watchMarket.market.priceAssetDecimals).clearBalance()
                val sum = amountUIValue.toDouble() * priceUIValue.toDouble()

                edit_amount.setText(amountUIValue)
                edit_limit_price.setText(priceUIValue)

                edit_total_price.setText(MoneyUtil.getFormattedTotal(sum, watchMarket.market.priceAssetDecimals))
            }
        }

        text_expiration_value.click {
            val alt_bld = AlertDialog.Builder(baseActivity)
            alt_bld.setTitle(getString(R.string.buy_and_sell_expiration_dialog_title))
            alt_bld.setSingleChoiceItems(presenter.expirationList.map { it.timeUI }.toTypedArray(), presenter.selectedExpiration) { dialog, item ->
                if (presenter.selectedExpiration == item) {
                    buttonPositive?.setTextColor(findColor(R.color.basic300))
                    buttonPositive?.isClickable = false
                } else {
                    buttonPositive?.setTextColor(findColor(R.color.submit400))
                    buttonPositive?.isClickable = true
                }
                presenter.newSelectedExpiration = item
            }
            alt_bld.setPositiveButton(getString(R.string.buy_and_sell_expiration_dialog_positive_btn)) { dialog, which ->
                dialog.dismiss()
                presenter.selectedExpiration = presenter.newSelectedExpiration
                text_expiration_value.text = presenter.expirationList[presenter.selectedExpiration].timeUI
            }
            alt_bld.setNegativeButton(getString(R.string.buy_and_sell_expiration_dialog_negative_btn)) { dialog, which -> dialog.dismiss() }
            val alert = alt_bld.create()
            alert.show()
            alert.makeStyled()

            buttonPositive = alert?.findViewById<Button>(android.R.id.button1)
            buttonPositive?.setTextColor(findColor(R.color.basic300))
            buttonPositive?.isClickable = false
        }

        button_confirm.click {
            presenter.createOrder(edit_amount.text.toString(), edit_limit_price.text.toString())
//            launchActivity<TradeBuyAndSendSuccessActivity> {
//                putExtra(TradeBuyAndSendSuccessActivity.BUNDLE_OPERATION_TYPE, presenter.orderType)
//            }
        }
    }

    override fun successLoadPairBalance(pairBalance: LinkedTreeMap<String, Long>) {
        pairBalance[presenter.watchMarket.market.amountAsset].notNull { balance ->
            linear_percent_values.children.forEach { children ->
                val quickBalanceView = children as AppCompatTextView
                when (quickBalanceView.tag) {
                    TOTAL_BALANCE -> {
                        quickBalanceView.click {
                            edit_amount.setText((MoneyUtil.getScaledText(balance, presenter.watchMarket.market.amountAssetDecimals)).clearBalance())
                            edit_amount.setSelection(edit_amount.text?.length ?: 0)
                        }
                    }
                    else -> {
                        val percentBalance = (balance.times((quickBalanceView.tag.toString().toDouble().div(100)))).toLong()
                        quickBalanceView.click {
                            edit_amount.setText(MoneyUtil.getScaledText(percentBalance, presenter.watchMarket.market.amountAssetDecimals))
                            edit_amount.setSelection(edit_amount.text?.length ?: 0)
                        }
                    }
                }
            }
        }
    }

    override fun successPlaceOrder() {

    }
}
