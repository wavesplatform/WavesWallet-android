package com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.order

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatTextView
import android.widget.Button
import android.widget.EditText
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.jakewharton.rxbinding2.widget.RxTextView
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.local.BuySellData
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.custom.CounterHandler
import com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.OrderListener
import com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.TradeBuyAndSellBottomSheetFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.success.TradeBuyAndSendSuccessActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.leasing.start.StartLeasingActivity.Companion.TOTAL_BALANCE
import com.wavesplatform.wallet.v2.util.*
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_trade_order.*
import kotlinx.android.synthetic.main.transient_notification.*
import pers.victor.ext.*
import pyxis.uzuki.live.richutilskt.utils.asDateString
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TradeOrderFragment : BaseFragment(), TradeOrderView {

    @Inject
    @InjectPresenter
    lateinit var presenter: TradeOrderPresenter
    var buttonPositive: Button? = null

    @ProvidePresenter
    fun providePresenter(): TradeOrderPresenter = presenter

    var orderListener: OrderListener? = null

    override fun configLayoutRes() = R.layout.fragment_trade_order

    companion object {
        fun newInstance(orderType: Int, data: BuySellData?, listener: OrderListener): TradeOrderFragment {
            val args = Bundle()
            args.classLoader = BuySellData::class.java.classLoader
            args.putParcelable(TradeBuyAndSellBottomSheetFragment.BUNDLE_DATA, data)
            args.putInt(TradeBuyAndSellBottomSheetFragment.BUNDLE_ORDER_TYPE, orderType)
            val fragment = TradeOrderFragment()
            fragment.orderListener = listener
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        arguments.notNull {
            presenter.data = it.getParcelable<BuySellData>(TradeBuyAndSellBottomSheetFragment.BUNDLE_DATA)
            presenter.orderType = it.getInt(TradeBuyAndSellBottomSheetFragment.BUNDLE_ORDER_TYPE)
        }

        if (presenter.orderType == TradeBuyAndSellBottomSheetFragment.SELL_TYPE) {
            horizontal_amount_suggestion.visiable()
            horizontal_total_suggestion.gone()
        } else {
            horizontal_amount_suggestion.gone()
            horizontal_total_suggestion.visiable()
        }

        presenter.getMatcherKey()
        presenter.loadPairBalancesAndCommission()
        presenter.loadWavesBalance()

        edit_amount.applyFilterStartWithDot()
        edit_limit_price.applyFilterStartWithDot()
        edit_total_price.applyFilterStartWithDot()

        CounterHandler.Builder()
                .valueView(edit_amount)
                .incrementalView(image_amount_plus)
                .decrementalView(image_amount_minus)
                .listener(object : CounterHandler.CounterListener {
                    override fun onIncrement(view: EditText?, number: BigDecimal) {
                        view?.setText(number.toString())
                        view?.setSelection(view.text.length)
                    }

                    override fun onDecrement(view: EditText?, number: BigDecimal) {
                        view?.setText(number.toString())
                        view?.setSelection(view.text.length)
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
                        view?.setSelection(view.text.length)
                    }

                    override fun onDecrement(view: EditText?, number: BigDecimal) {
                        view?.setText(number.toString())
                        view?.setSelection(view.text.length)
                    }
                })
                .build()

        CounterHandler.Builder()
                .valueView(edit_total_price)
                .incrementalView(image_total_price_plus)
                .decrementalView(image_total_price_minus)
                .listener(object : CounterHandler.CounterListener {
                    override fun onIncrement(view: EditText?, number: BigDecimal) {
                        presenter.humanTotalTyping = true
                        view?.setText(number.toString())
                        view?.setSelection(view.text.length)
                    }

                    override fun onDecrement(view: EditText?, number: BigDecimal) {
                        presenter.humanTotalTyping = true
                        view?.setText(number.toString())
                        view?.setSelection(view.text.length)
                    }
                })
                .build()

        eventSubscriptions.add(RxTextView.textChanges(edit_amount)
                .skipInitialValue()
                .map(CharSequence::toString)
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map {
                    presenter.amountValidation = it.isNotEmpty()
                    if (it.isNotEmpty()) {
                        text_amount_error.text = ""
                        text_amount_error.invisiable()
                    } else {
                        text_amount_error.text = getString(R.string.buy_and_sell_required)
                        text_amount_error.visiable()
                    }
                    makeButtonEnableIfValid()
                    return@map it
                }
                .filter {
                    val validNumber = it.toBigDecimalOrNull()
                    if (validNumber == null) {
                        presenter.amountValidation = false
                        makeButtonEnableIfValid()
                    }
                    validNumber != null
                }
                .map {
                    if (presenter.orderType == TradeBuyAndSellBottomSheetFragment.SELL_TYPE) {
                        val isValid = it.toBigDecimal() <= MoneyUtil.getScaledText((presenter.currentAmountBalance
                                ?: 0) - getFeeIfNeed(), presenter.data?.watchMarket?.market?.amountAssetDecimals
                                ?: 0).clearBalance().toBigDecimal()
                        presenter.amountValidation = isValid

                        if (isValid) {
                            text_amount_error.text = ""
                            text_amount_error.invisiable()
                        } else {
                            text_amount_error.text = getString(R.string.buy_and_sell_not_enough, presenter.data?.watchMarket?.market?.amountAssetShortName)
                            text_amount_error.visiable()
                        }
                        makeButtonEnableIfValid()
                        return@map Pair(isValid, it)
                    } else {
                        return@map Pair(true, it)
                    }
                }
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ isValid ->
                    if (!presenter.humanTotalTyping) {
                        if (!edit_amount.text.isNullOrEmpty() && !edit_limit_price.text.isNullOrEmpty()) {
                            edit_total_price.setText(
                                    (edit_amount.text.toString().toDouble() * edit_limit_price.text.toString().toDouble())
                                            .roundToDecimals(presenter.data?.watchMarket?.market?.priceAssetDecimals)
                                            .toBigDecimal()
                                            .toPlainString()
                                            .stripZeros())
                        }
                    }
                    presenter.humanTotalTyping = false
                    makeButtonEnableIfValid()
                }, {
                    it.printStackTrace()
                }))

        eventSubscriptions.add(RxTextView.textChanges(edit_limit_price)
                .skipInitialValue()
                .map(CharSequence::toString)
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map {
                    presenter.priceValidation = it.isNotEmpty()
                    if (presenter.priceValidation) {
                        text_limit_price_error.text = ""
                        text_limit_price_error.invisiable()
                    } else {
                        text_limit_price_error.text = getString(R.string.buy_and_sell_required)
                        text_limit_price_error.visiable()
                    }
                    makeButtonEnableIfValid()
                    return@map it
                }
                .filter {
                    val validNumber = it.toBigDecimalOrNull()
                    validNumber != null
                }
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ isValid ->
                    if (!edit_amount.text.isNullOrEmpty() && !edit_limit_price.text.isNullOrEmpty()) {
                        edit_total_price.setText(
                                (edit_amount.text.toString().toDouble() * edit_limit_price.text.toString().toDouble())
                                        .roundToDecimals(presenter.data?.watchMarket?.market?.priceAssetDecimals)
                                        .toBigDecimal()
                                        .toPlainString()
                                        .stripZeros())
                    }
                    makeButtonEnableIfValid()
                }, {
                    it.printStackTrace()
                }))

        eventSubscriptions.add(RxTextView.textChanges(edit_total_price)
                .skipInitialValue()
                .map(CharSequence::toString)
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .filter {
                    val validNumber = it.toBigDecimalOrNull()
                    if (validNumber == null) {
                        presenter.totalPriceValidation = false
                        makeButtonEnableIfValid()
                    }
                    validNumber != null
                }
                .map {
                    if (!presenter.humanTotalTyping) {
                        presenter.humanTotalTyping = edit_total_price.isFocused
                    }
                    presenter.totalPriceValidation = it.isNotEmpty()
                    if (edit_amount.text.isNullOrEmpty()) {
                        text_amount_error.text = getString(R.string.buy_and_sell_required)
                        text_amount_error.visiable()
                    }
                    if (edit_limit_price.text.isNullOrEmpty()) {
                        text_limit_price_error.text = getString(R.string.buy_and_sell_required)
                        text_limit_price_error.visiable()
                    }

                    if (isValidWavesFee()) {
                        linear_fees_error.gone()
                    } else {
                        linear_fees_error.visiable()
                    }

                    if (presenter.humanTotalTyping) {
                        if (!edit_total_price.text.isNullOrEmpty() && !edit_limit_price.text.isNullOrEmpty()) {
                            if (edit_limit_price.text.toString().toDouble() != 0.0) {
                                edit_amount.setText(
                                        (edit_total_price.text.toString().toDouble() / edit_limit_price.text.toString().toDouble())
                                                .roundToDecimals(presenter.data?.watchMarket?.market?.amountAssetDecimals)
                                                .toBigDecimal()
                                                .toPlainString()
                                                .stripZeros())
                            }
                        }
                    }
                    makeButtonEnableIfValid()
                    return@map it
                }
                .map {
                    if (presenter.orderType == TradeBuyAndSellBottomSheetFragment.BUY_TYPE) {
                        val isValid = it.toBigDecimal() <= MoneyUtil.getScaledText(presenter.currentPriceBalance
                                ?: 0, presenter.data?.watchMarket?.market?.priceAssetDecimals
                                ?: 0).clearBalance().toBigDecimal()
                        presenter.totalPriceValidation = isValid

                        if (isValid) {
                            text_total_price_error.text = ""
                            text_total_price_error.invisiable()
                        } else {
                            text_total_price_error.text = getString(R.string.buy_and_sell_not_enough, presenter.data?.watchMarket?.market?.priceAssetShortName)
                            text_total_price_error.visiable()
                        }
                        makeButtonEnableIfValid()
                        return@map Pair(isValid, it)
                    } else {
                        return@map Pair(true, it)
                    }
                }
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ isValid ->
                    makeButtonEnableIfValid()
                }, {
                    it.printStackTrace()
                }))

        horizontal_limit_price_suggestion.goneIf {
            presenter.data?.lastPrice == null &&
                    presenter.data?.askPrice == null && presenter.data?.bidPrice == null
        }
        text_bid.goneIf { presenter.data?.bidPrice == null }
        text_ask.goneIf { presenter.data?.askPrice == null }
        text_last.goneIf { presenter.data?.lastPrice == null }

        text_bid.click {
            presenter.data?.bidPrice.notNull { price ->
                edit_limit_price.setText(MoneyUtil.getScaledPrice(price,
                        presenter.data?.watchMarket?.market?.amountAssetDecimals
                                ?: 0, presenter.data?.watchMarket?.market?.priceAssetDecimals
                        ?: 0).stripZeros())
            }
        }

        text_ask.click {
            presenter.data?.askPrice.notNull { price ->
                edit_limit_price.setText(MoneyUtil.getScaledPrice(price,
                        presenter.data?.watchMarket?.market?.amountAssetDecimals
                                ?: 0, presenter.data?.watchMarket?.market?.priceAssetDecimals
                        ?: 0).stripZeros())
            }
        }

        text_last.click {
            presenter.data?.lastPrice.notNull { price ->
                edit_limit_price.setText(MoneyUtil.getScaledPrice(price,
                        presenter.data?.watchMarket?.market?.amountAssetDecimals
                                ?: 0, presenter.data?.watchMarket?.market?.priceAssetDecimals
                        ?: 0).stripZeros())
            }
        }

        text_amount_hint.text = getString(R.string.buy_and_sell_amount_in, presenter.data?.watchMarket?.market?.amountAssetShortName)
        text_amount_error.text = getString(R.string.buy_and_sell_not_enough, presenter.data?.watchMarket?.market?.amountAssetShortName)
        text_limit_price_hint.text = getString(R.string.buy_and_sell_limit_price_in, presenter.data?.watchMarket?.market?.priceAssetShortName)
        text_total_price_hint.text = getString(R.string.buy_and_sell_total_in, presenter.data?.watchMarket?.market?.priceAssetShortName)

        if (presenter.orderType == TradeBuyAndSellBottomSheetFragment.SELL_TYPE) {
            button_confirm.setBackgroundResource(R.drawable.selector_btn_red)
            button_confirm.text = getString(R.string.sell_btn_txt, presenter.data?.watchMarket?.market?.amountAssetShortName)
        } else {
            button_confirm.text = getString(R.string.buy_btn_txt, presenter.data?.watchMarket?.market?.amountAssetShortName)
        }

        presenter.data?.watchMarket.notNull { watchMarket ->
            if (presenter.data?.initAmount != null) {
                val amountUIValue = MoneyUtil.getScaledText(presenter.data?.initAmount!!, watchMarket.market.amountAssetDecimals).clearBalance()
                edit_amount.setText(amountUIValue)
            }
            if (presenter.data?.initPrice != null) {
                val priceUIValue = MoneyUtil.getScaledPrice(presenter.data?.initPrice!!, watchMarket.market.amountAssetDecimals, watchMarket.market.priceAssetDecimals).clearBalance()
                edit_limit_price.setText(priceUIValue)
            }
            if (presenter.data?.initAmount != null && presenter.data?.initPrice != null) {
                val sum = edit_limit_price.text.toString().toDouble() * edit_limit_price.text.toString().toDouble()
                edit_total_price.setText(MoneyUtil.getFormattedTotal(sum, watchMarket.market.priceAssetDecimals))
            }
        }

        text_expiration_value.click {
            val alt_bld = AlertDialog.Builder(baseActivity)
            alt_bld.setTitle(getString(R.string.buy_and_sell_expiration_dialog_title))
            alt_bld.setSingleChoiceItems(presenter.expirationList.map { getString(it.timeUI) }
                    .toTypedArray(), presenter.selectedExpiration) { dialog, item ->
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
                text_expiration_value.text = getString(
                        presenter.expirationList[presenter.selectedExpiration].timeUI)
            }
            alt_bld.setNegativeButton(getString(R.string.buy_and_sell_expiration_dialog_negative_btn)) { dialog, which -> dialog.dismiss() }
            val alert = alt_bld.create()
            alert.show()
            alert.makeStyled()

            buttonPositive = alert?.findViewById(android.R.id.button1)
            buttonPositive?.setTextColor(findColor(R.color.basic300))
            buttonPositive?.isClickable = false
        }

        button_confirm.click {
            presenter.createOrder(edit_amount.text.toString(), edit_limit_price.text.toString())
        }
    }

    private fun getFeeIfNeed(): Long {
        return if (presenter.data?.watchMarket?.market?.amountAsset?.isWaves() == true) {
            presenter.fee
        } else {
            0L
        }
    }

    private fun isValidWavesFee(): Boolean {
        return if (presenter.wavesBalance.getAvailableBalance() ?: 0 > presenter.fee) {
            true
        } else {
            if (presenter.data?.watchMarket?.market?.amountAsset?.isWaves() == true &&
                    presenter.orderType == TradeBuyAndSellBottomSheetFragment.BUY_TYPE) {
                edit_amount.text.toString().toBigDecimal() > getWavesDexFee(presenter.fee)
            } else if (presenter.data?.watchMarket?.market?.priceAsset?.isWaves() == true &&
                    presenter.orderType == TradeBuyAndSellBottomSheetFragment.SELL_TYPE) {
                edit_total_price.text.toString().toBigDecimal() > getWavesDexFee(presenter.fee)
            } else {
                false
            }
        }
    }

    private fun makeButtonEnableIfValid() {
        button_confirm.isEnabled = presenter.isAllFieldsValid() && isNetworkConnected()
    }

    override fun successLoadPairBalance(currentAmountBalance: Long?, currentPriceBalance: Long?) {
        currentAmountBalance.notNull { balance ->
            linear_percent_values.children.forEach { children ->
                var balance = balance
                val quickBalanceView = children as AppCompatTextView
                when (quickBalanceView.tag) {
                    TOTAL_BALANCE -> {
                        if (presenter.data?.watchMarket?.market?.amountAsset?.isWaves() == true) {
                            if (balance < presenter.fee) {
                                balance = 0
                            } else {
                                balance -= presenter.fee
                            }
                        }
                        quickBalanceView.click {
                            edit_amount.setText((MoneyUtil.getScaledText(balance,
                                    presenter.data?.watchMarket?.market?.amountAssetDecimals
                                            ?: 0)).clearBalance())
                            edit_amount.setSelection(edit_amount.text?.length ?: 0)
                        }
                    }
                    else -> {
                        val percentBalance = (balance.times((quickBalanceView.tag.toString().toDouble()
                                .div(100)))).toLong()
                        quickBalanceView.click {
                            edit_amount.setText(MoneyUtil.getScaledText(percentBalance,
                                    presenter.data?.watchMarket?.market?.amountAssetDecimals
                                            ?: 0).clearBalance())
                            edit_amount.setSelection(edit_amount.text?.length ?: 0)
                        }
                    }
                }
            }
        }

        currentPriceBalance.notNull { balance ->
            linear_total_percent_values.children.forEach { children ->
                var balance = balance
                val quickBalanceView = children as AppCompatTextView
                when (quickBalanceView.tag) {
                    TOTAL_BALANCE -> {
                        if (presenter.data?.watchMarket?.market?.priceAsset?.isWaves() == true) {
                            if (balance < presenter.fee) {
                                balance = 0
                            } else {
                                balance -= presenter.fee
                            }
                        }
                        quickBalanceView.click {
                            presenter.humanTotalTyping = true
                            edit_total_price.setText((MoneyUtil.getScaledText(balance,
                                    presenter.data?.watchMarket?.market?.priceAssetDecimals
                                            ?: 0)).clearBalance())
                            edit_total_price.setSelection(edit_total_price.text?.length ?: 0)
                        }
                    }
                    else -> {
                        val percentBalance = (balance.times((quickBalanceView.tag.toString().toDouble()
                                .div(100)))).toLong()
                        quickBalanceView.click {
                            presenter.humanTotalTyping = true
                            edit_total_price.setText(MoneyUtil.getScaledText(percentBalance,
                                    presenter.data?.watchMarket?.market?.priceAssetDecimals
                                            ?: 0).clearBalance())
                            edit_total_price.setSelection(edit_total_price.text?.length ?: 0)
                        }
                    }
                }
            }
        }
    }

    override fun successPlaceOrder() {
        launchActivity<TradeBuyAndSendSuccessActivity> {
            putExtra(TradeBuyAndSendSuccessActivity.BUNDLE_OPERATION_TYPE, presenter.orderType)
            putExtra(TradeBuyAndSendSuccessActivity.BUNDLE_AMOUNT, edit_amount.text.toString())
            putExtra(TradeBuyAndSendSuccessActivity.BUNDLE_PRICE, edit_limit_price.text.toString())
            putExtra(TradeBuyAndSendSuccessActivity.BUNDLE_TIME, presenter.orderRequest.timestamp.asDateString("HH:mm:ss"))
        }
        orderListener?.onSuccessPlaceOrder()
    }

    override fun afterFailedPlaceOrder(message: String?) {
        message.notNull {
            orderListener?.showError(it)
        }
    }

    override fun onNetworkConnectionChanged(networkConnected: Boolean) {
        super.onNetworkConnectionChanged(networkConnected)
        button_confirm.isEnabled = presenter.isAllFieldsValid() && networkConnected
    }

    override fun showCommissionLoading() {
        progress_bar_fee_transaction.show()
        text_fee_value.gone()
    }

    override fun showCommissionSuccess(unscaledAmount: Long) {
        text_fee_value.text = "${getScaledAmount(unscaledAmount, 8)} " +
                "${Constants.wavesAssetInfo.name}"
        progress_bar_fee_transaction.hide()
        text_fee_value.visiable()
    }

    override fun showCommissionError() {
        text_fee_value.text = "-"
        progress_bar_fee_transaction.hide()
        orderListener?.showError(getString(R.string.common_error_commission_receiving))
        text_fee_value.visiable()
    }

    override fun onDestroyView() {
        progress_bar_fee_transaction.hide()
        super.onDestroyView()
    }
}
