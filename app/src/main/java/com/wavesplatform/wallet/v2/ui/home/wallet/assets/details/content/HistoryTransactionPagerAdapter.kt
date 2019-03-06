package com.wavesplatform.wallet.v2.ui.home.wallet.assets.details.content

import android.graphics.Typeface
import android.support.v4.app.FragmentManager
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.local.HistoryItem
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.data.model.remote.response.TransactionType
import com.wavesplatform.wallet.v2.ui.home.history.details.HistoryDetailsBottomSheetFragment
import com.wavesplatform.wallet.v2.util.*
import com.wavesplatform.wallet.v2.util.TransactionUtil.Companion.getTransactionAmount
import kotlinx.android.synthetic.main.assets_detailed_history_item.view.*
import pers.victor.ext.*

class HistoryTransactionPagerAdapter constructor(
    var fragmentManager: FragmentManager?,
    var prefsUtil: PrefsUtil
) : PagerAdapter() {

    var items: List<HistoryItem> = arrayListOf()

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val layout = inflate(R.layout.assets_detailed_history_item, collection, false)
        val item = items[position]

        layout.card_transaction.click {
            val bottomSheetFragment = HistoryDetailsBottomSheetFragment()

            bottomSheetFragment.configureData(item.data, position)
            bottomSheetFragment.show(fragmentManager, bottomSheetFragment.tag)
        }

        layout.text_tag.gone()
        layout.text_tag_spam.gone()
        layout.text_transaction_value.setTypeface(null, Typeface.NORMAL)

        item.data.transactionType().notNull {
            layout.image_transaction.setImageDrawable(it.icon())
            layout.text_transaction_name.text = layout.text_transaction_name.context
                    .getString(it.title)

            // todo reuse this code and HistoryTabItemAdapter and HistoryDetailsAdapter
            val decimals = item.data.asset?.precision ?: 8
            when (it) {
                TransactionType.SENT_TYPE -> {
                    item.data.amount.notNull {
                        layout.text_transaction_value.text =
                                "-${getScaledAmount(it, decimals)}"
                    }
                }
                TransactionType.RECEIVED_TYPE -> {
                    item.data.amount.notNull {
                        layout.text_transaction_value.text =
                                "+${getScaledAmount(it, decimals)}"
                    }
                }
                TransactionType.RECEIVE_SPONSORSHIP_TYPE -> {
                    item.data.fee.notNull {
                        layout.text_transaction_value.text =
                                "+${getScaledAmount(it, item.data.feeAssetObject?.precision
                                        ?: 8)} ${item.data.feeAssetObject?.name}"
                    }
                }
                TransactionType.MASS_SPAM_RECEIVE_TYPE,
                TransactionType.MASS_RECEIVE_TYPE,
                TransactionType.MASS_SEND_TYPE -> {
                    layout.text_transaction_value.text = getTransactionAmount(
                            transaction = item.data, round = false)
                }
                TransactionType.CREATE_ALIAS_TYPE -> {
                    layout.text_transaction_value.text = item.data.alias
                    layout.text_transaction_value.setTypeface(null, Typeface.BOLD)
                }
                TransactionType.EXCHANGE_TYPE -> {
                    // todo reuse this method with HistoryTabItemAdapter and HistoryDetailsAdapter
                    setExchangeItem(item.data, layout)
                }
                TransactionType.CANCELED_LEASING_TYPE -> {
                    item.data.lease?.amount.notNull {
                        layout.text_transaction_value.text = getScaledAmount(it, decimals)
                    }
                }
                TransactionType.TOKEN_BURN_TYPE -> {
                    item.data.amount.notNull {
                        layout.text_transaction_value.text = "-" + getScaledAmount(it, decimals)
                    }
                }
                TransactionType.TOKEN_GENERATION_TYPE,
                TransactionType.TOKEN_REISSUE_TYPE -> {
                    val quantity = getScaledAmount(item.data.quantity, decimals)
                    layout.text_transaction_value.text = quantity
                }
                TransactionType.DATA_TYPE,
                TransactionType.SET_ADDRESS_SCRIPT_TYPE,
                TransactionType.CANCEL_ADDRESS_SCRIPT_TYPE,
                TransactionType.UPDATE_ASSET_SCRIPT_TYPE -> {
                    layout.text_transaction_value.setTypeface(null, Typeface.BOLD)
                    layout.text_transaction_value.text = layout.text_transaction_name.context
                            .getString(it.title)
                    layout.text_transaction_name.text = layout.text_transaction_name.context
                            .getString(R.string.history_data_type_title)
                }
                TransactionType.SET_SPONSORSHIP_TYPE,
                TransactionType.CANCEL_SPONSORSHIP_TYPE -> {
                    layout.text_transaction_value.text = item.data.asset?.name
                }
                else -> {
                    item.data.amount.notNull {
                        layout.text_transaction_value.text = getScaledAmount(it, decimals)
                    }
                }
            }
        }

        // todo reuse this code and HistoryTabItemAdapter and HistoryDetailsAdapter
        if (!TransactionType.isZeroTransferOrExchange(item.data.transactionType())) {
            if (isSpamConsidered(item.data.assetId, prefsUtil)) {
                layout.text_tag_spam.visiable()
            } else {
                if (isShowTicker(item.data.assetId)) {
                    val ticker = item.data.asset?.getTicker()
                    if (!ticker.isNullOrBlank()) {
                        layout.text_tag.text = ticker
                        layout.text_tag.visiable()
                    }
                } else {
                    layout.text_transaction_value.text =
                            "${layout.text_transaction_value.text} ${item.data.asset?.name}"
                }
            }
        }
        layout.text_transaction_value.makeTextHalfBold()

        collection.addView(layout)
        return layout
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getPageWidth(position: Int): Float {
        return 1f
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    private fun setExchangeItem(transaction: Transaction, view: View) {
        val myOrder = findMyOrder(
                transaction.order1!!,
                transaction.order2!!,
                App.getAccessManager().getWallet()?.address)
        val secondOrder = if (myOrder.id == transaction.order1!!.id) {
            transaction.order2!!
        } else {
            transaction.order1!!
        }

        val directionStringResId: Int
        val directionSign: String
        val amountAsset = myOrder.assetPair?.amountAssetObject!!
        val amountValue = getScaledAmount(transaction.amount,
                amountAsset.precision)

        if (myOrder.orderType == Constants.SELL_ORDER_TYPE) {
            directionStringResId = R.string.history_my_dex_intent_sell
            directionSign = "-"
        } else {
            directionStringResId = R.string.history_my_dex_intent_buy
            directionSign = "+"
        }

        view.text_transaction_name.text = app.getString(
                directionStringResId,
                amountAsset.name,
                secondOrder.assetPair?.priceAssetObject?.name)

        val amountAssetTicker = if (amountAsset.name == "WAVES") {
            Constants.WAVES_ASSET_ID_FILLED
        } else {
            amountAsset.ticker
        }

        val assetName = if (amountAssetTicker.isNullOrEmpty()) {
            " ${amountAsset.name}"
        } else {
            view.text_tag.visiable()
            view.text_tag.text = amountAssetTicker
            ""
        }

        view.text_transaction_value.text = directionSign + amountValue + assetName
    }
}