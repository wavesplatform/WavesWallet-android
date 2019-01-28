package com.wavesplatform.wallet.v2.ui.home.wallet.assets.details.content

import android.graphics.Typeface
import android.support.v4.app.FragmentManager
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.vicpin.krealmextensions.queryFirst
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.local.HistoryItem
import com.wavesplatform.wallet.v2.data.model.remote.response.SpamAsset
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.data.model.remote.response.TransactionType
import com.wavesplatform.wallet.v2.ui.home.history.details.HistoryDetailsBottomSheetFragment
import com.wavesplatform.wallet.v2.util.*
import kotlinx.android.synthetic.main.assets_detailed_history_item.view.*
import pers.victor.ext.*
import java.util.*

class HistoryTransactionPagerAdapter constructor(var fragmentManager: FragmentManager?) : PagerAdapter() {
    var items: List<HistoryItem> = arrayListOf()

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val layout = inflate(R.layout.assets_detailed_history_item, collection, false)
        val item = items[position]

        layout.card_transaction.click {
            val bottomSheetFragment = HistoryDetailsBottomSheetFragment()

            val allItems = items.asSequence()
                    .filter {
                        it.header.isEmpty()
                    }
                    .map { it.data }
                    .toList()

            bottomSheetFragment.configureData(item.data, position, allItems)
            bottomSheetFragment.show(fragmentManager, bottomSheetFragment.tag)
        }

        layout.image_transaction.setImageDrawable(item.data.transactionType()?.icon())

        val showTag = Constants.defaultAssets.any {
            it.assetId == item.data.assetId || item.data.assetId.isNullOrEmpty()
        }

        item.data.transactionType().notNull {
            try {
                layout.text_transaction_name.text =
                        app.getString(it.title)
            } catch (e: MissingFormatArgumentException) {
                layout.text_transaction_name.text = app.getString(it.title)
            }

            val decimals = item.data.asset?.precision ?: 8

            when (it) {
                TransactionType.SENT_TYPE -> {
                    item.data.amount.notNull {
                        layout.text_transaction_value.text = "-${getScaledAmount(it, decimals)}"
                    }
                }
                TransactionType.RECEIVED_TYPE -> {
                    item.data.amount.notNull {
                        layout.text_transaction_value.text = "+${getScaledAmount(it, decimals)}"
                    }
                }
                TransactionType.MASS_SPAM_RECEIVE_TYPE,
                TransactionType.MASS_SEND_TYPE,
                TransactionType.MASS_RECEIVE_TYPE -> {
                    if (item.data.transfers.isNotEmpty()) {
                        val sumString = getScaledAmount(
                                item.data.transfers.sumByLong { it.amount }, decimals)
                        if (sumString.isEmpty()) {
                            layout.text_transaction_value.text = ""
                        } else {
                            layout.text_transaction_value.text = sumString
                        }
                    } else {
                        layout.text_transaction_value.text =
                                getScaledAmount(item.data.amount, decimals)
                    }
                }
                TransactionType.CREATE_ALIAS_TYPE -> {
                    layout.text_transaction_value.text = item.data.alias
                }
                TransactionType.EXCHANGE_TYPE -> {
                    setExchangeItem(item.data, layout)
                }
                TransactionType.DATA_TYPE -> {
                    layout.text_transaction_value.text = app.getString(R.string.history_data_type_title)
                }
                TransactionType.CANCELED_LEASING_TYPE -> {
                    item.data.lease?.amount.notNull {
                        layout.text_transaction_value.text =
                                getScaledAmount(it, decimals)
                    }
                }
                TransactionType.TOKEN_GENERATION_TYPE -> {
                    val quantity = MoneyUtil.getScaledText(
                            item.data.quantity, item.data.asset).substringBefore(".")
                    layout.text_transaction_value.text = quantity
                }
                TransactionType.TOKEN_BURN_TYPE -> {
                    item.data.amount.notNull {
                        layout.text_transaction_value.text =
                                "-${getScaledAmount(it, decimals)}"
                    }
                }
                TransactionType.TOKEN_REISSUE_TYPE -> {
                    layout.text_transaction_value.text = "+${item.data.amount}"
                }
                TransactionType.SET_ADDRESS_SCRIPT_TYPE,
                TransactionType.CANCEL_ADDRESS_SCRIPT_TYPE,
                TransactionType.SET_SPONSORSHIP_TYPE,
                TransactionType.CANCEL_SPONSORSHIP_TYPE,
                TransactionType.UPDATE_ASSET_SCRIPT_TYPE -> {
                    layout.text_transaction_value.text =
                            layout.context.getString(R.string.history_data_type_title)
                    layout.text_transaction_name.text = layout.context.getString(
                            item.data.transactionType().title)
                    layout.text_tag.gone()
                    layout.text_transaction_name.setTypeface(null, Typeface.BOLD)
                }
                else -> {
                    item.data.amount.notNull {
                        layout.text_transaction_value.text =
                                getScaledAmount(it, decimals)
                    }
                }
            }
        }

        if (item.data.transactionType() != TransactionType.CREATE_ALIAS_TYPE
                && item.data.transactionType() != TransactionType.DATA_TYPE
                && item.data.transactionType() != TransactionType.SPAM_RECEIVE_TYPE
                && item.data.transactionType() != TransactionType.MASS_SPAM_RECEIVE_TYPE
                && item.data.transactionType() != TransactionType.EXCHANGE_TYPE
                && item.data.transactionType() != TransactionType.SET_ADDRESS_SCRIPT_TYPE
                && item.data.transactionType() != TransactionType.CANCEL_ADDRESS_SCRIPT_TYPE
                && item.data.transactionType() != TransactionType.SET_SPONSORSHIP_TYPE
                && item.data.transactionType() != TransactionType.CANCEL_SPONSORSHIP_TYPE
                && item.data.transactionType() != TransactionType.UPDATE_ASSET_SCRIPT_TYPE) {
            if (showTag) {
                val ticker = item.data.asset?.getTicker()
                if (!ticker.isNullOrBlank()) {
                    layout.text_tag.visiable()
                    layout.text_tag.text = ticker
                }
            } else {
                layout.text_tag.gone()
                layout.text_transaction_value.text = "${layout.text_transaction_value.text} ${item.data.asset?.name}"
            }
        }

        if (queryFirst<SpamAsset> { equalTo("assetId", item.data.assetId) } != null) {
            layout.text_tag.gone()
            layout.text_tag_spam.visiable()
            layout.text_transaction_value.text = "${item.data.asset?.name}"
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
                App.getAccessManager().getWallet()?.address!!)
        val secondOrder = if (myOrder.id == transaction.order1!!.id) {
            transaction.order2!!
        } else {
            transaction.order1!!
        }

        val directionStringResId: Int
        val directionSign: String
        val amountAsset = myOrder.assetPair?.amountAssetObject!!
        val amountValue = MoneyUtil.getScaledText(transaction.amount, amountAsset).stripZeros()

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
            "WAVES"
        } else {
            amountAsset.ticker
        }

        val assetName = if (amountAssetTicker.isNullOrEmpty()) {
            view.text_tag.gone()
            " ${amountAsset.name}"
        } else {
            view.text_tag.visiable()
            view.text_tag.text = amountAssetTicker
            ""
        }

        view.text_transaction_value.text = directionSign + amountValue + assetName
    }
}