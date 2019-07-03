/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.history.details

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.AppCompatTextView
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import com.vicpin.krealmextensions.queryFirst
import com.wavesplatform.sdk.crypto.WavesCrypto
import com.wavesplatform.wallet.v2.data.model.local.OrderType
import com.wavesplatform.wallet.v2.data.model.local.TransactionType
import com.wavesplatform.sdk.model.response.data.AssetInfoResponse
import com.wavesplatform.sdk.model.response.node.AssetBalanceResponse
import com.wavesplatform.sdk.model.response.node.HistoryTransactionResponse
import com.wavesplatform.sdk.model.response.node.TransferResponse
import com.wavesplatform.wallet.v2.data.manager.service.CoinomatService
import com.wavesplatform.sdk.utils.*
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.BuildConfig
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.model.db.AssetBalanceDb
import com.wavesplatform.wallet.v2.data.analytics.AnalyticEvents
import com.wavesplatform.wallet.v2.data.analytics.analytics
import com.wavesplatform.wallet.v2.data.model.local.LeasingStatus
import com.wavesplatform.wallet.v2.data.model.db.userdb.AddressBookUserDb
import com.wavesplatform.wallet.v2.ui.base.view.BaseTransactionBottomSheetFragment
import com.wavesplatform.wallet.v2.ui.custom.AssetAvatarView
import com.wavesplatform.wallet.v2.ui.custom.SpamTag
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.add.AddAddressActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.edit.EditAddressActivity
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.SendActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.leasing.cancel.confirmation.ConfirmationCancelLeasingActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.leasing.start.StartLeasingActivity
import com.wavesplatform.wallet.v2.util.*
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_bottom_sheet_exchange_layout.view.*
import kotlinx.android.synthetic.main.fragment_bottom_sheet_script_invocation_layout.view.*
import kotlinx.android.synthetic.main.fragment_history_bottom_sheet_base_info_layout.view.*
import kotlinx.android.synthetic.main.fragment_history_bottom_sheet_bottom_btns.view.*
import kotlinx.android.synthetic.main.content_history_details_layout.view.*
import pers.victor.ext.*
import java.util.concurrent.TimeUnit

class HistoryDetailsBottomSheetFragment : BaseTransactionBottomSheetFragment<HistoryTransactionResponse>() {

    override fun configLayoutRes(): Int {
        return R.layout.bottom_sheet_dialog_history_details
    }

    override fun setupHeader(transaction: HistoryTransactionResponse): View? {
        val view = inflate(R.layout.content_history_details_layout)

        view.text_tag.gone()
        view.text_transaction_value.setTypeface(null, Typeface.NORMAL)
        val decimals = transaction.asset?.precision ?: 8

        transaction.transactionType().notNull {
            view.image_transaction_type.setImageDrawable(it.icon())
            view.text_transaction_name.text = getString(it.title)
            when (it) {
                TransactionType.SENT_TYPE -> {
                    transaction.amount.notNull {
                        view.text_transaction_value.text =
                                "-${MoneyUtil.getScaledText(it, decimals).stripZeros()}"
                    }
                }
                TransactionType.RECEIVED_TYPE -> {
                    transaction.amount.notNull {
                        view.text_transaction_value.text =
                                "+${MoneyUtil.getScaledText(it, decimals).stripZeros()}"
                    }
                }
                TransactionType.RECEIVE_SPONSORSHIP_TYPE -> {
                    transaction.fee.notNull {
                        view.text_transaction_value.text = "+${MoneyUtil.getScaledText(
                                it, transaction.feeAssetObject?.precision ?: 8)
                                .stripZeros()} ${transaction.feeAssetObject?.name}"
                    }
                }
                TransactionType.MASS_SPAM_RECEIVE_TYPE,
                TransactionType.MASS_RECEIVE_TYPE,
                TransactionType.MASS_SEND_TYPE -> {
                    view.text_transaction_value.text = getTransactionAmount(
                            transaction = transaction, decimals = decimals, round = false)
                }
                TransactionType.CREATE_ALIAS_TYPE -> {
                    view.text_transaction_value.text = transaction.alias
                    view.text_transaction_value.setTypeface(null, Typeface.BOLD)
                }
                TransactionType.EXCHANGE_TYPE -> {
                    setExchangeItem(transaction, view)
                }
                TransactionType.CANCELED_LEASING_TYPE -> {
                    transaction.lease?.amount.notNull {
                        view.text_transaction_value.text =
                                MoneyUtil.getScaledText(it, decimals).stripZeros()
                    }
                }
                TransactionType.TOKEN_BURN_TYPE -> {
                    transaction.amount.notNull {
                        view.text_transaction_value.text =
                                "-${MoneyUtil.getScaledText(it, decimals).stripZeros()}"
                    }
                }
                TransactionType.TOKEN_GENERATION_TYPE -> {
                    val quantity = MoneyUtil.getScaledText(transaction.quantity, decimals)
                            .stripZeros()
                    view.text_transaction_value.text = quantity
                }
                TransactionType.TOKEN_REISSUE_TYPE -> {
                    val quantity = MoneyUtil.getScaledText(transaction.quantity, decimals)
                            .stripZeros()
                    view.text_transaction_value.text = "+$quantity"
                }
                TransactionType.DATA_TYPE,
                TransactionType.SET_ADDRESS_SCRIPT_TYPE,
                TransactionType.CANCEL_ADDRESS_SCRIPT_TYPE,
                TransactionType.SCRIPT_INVOCATION_TYPE,
                TransactionType.UPDATE_ASSET_SCRIPT_TYPE -> {
                    view.text_transaction_name.text = getString(R.string.history_data_type_title)
                    view.text_transaction_value.text = getString(transaction.transactionType().title)
                    view.text_transaction_value.setTypeface(null, Typeface.BOLD)
                }
                TransactionType.SET_SPONSORSHIP_TYPE,
                TransactionType.CANCEL_SPONSORSHIP_TYPE -> {
                    view.text_transaction_value.text = transaction.asset?.name
                    view.text_transaction_value.setTypeface(null, Typeface.BOLD)
                }
                else -> {
                    transaction.amount.notNull {
                        view.text_transaction_value.text =
                                MoneyUtil.getScaledText(it, decimals).stripZeros()
                    }
                }
            }
        }

        if (!TransactionType.isZeroTransferOrExchange(transaction.transactionType())) {
            if (isSpamConsidered(transaction.assetId, prefsUtil)) {
                // nothing
            } else {
                if (isShowTicker(transaction.assetId)) {
                    val ticker = transaction.asset?.getTokenTicker()
                    if (!ticker.isNullOrBlank()) {
                        view.text_tag.text = ticker
                        view.text_tag.visiable()
                    }
                } else {
                    view.text_transaction_value.text = "${view.text_transaction_value.text} ${transaction.asset?.name}"
                }
            }
        }
        view.text_transaction_value.makeTextHalfBold()

        return view
    }

    override fun setupBody(transaction: HistoryTransactionResponse): View? {
        val historyContainer = LinearLayout(activity)
        historyContainer.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        historyContainer.orientation = LinearLayout.VERTICAL

        /**Comment block views**/
        val commentBlock = inflate(R.layout.content_history_detailed_transcation_comment_block_layout)

        val textComment = commentBlock?.findViewById<TextView>(R.id.text_comment)

        val showCommentBlock = !transaction.attachment.isNullOrEmpty()
        if (showCommentBlock) {
            commentBlock.visiable()
            textComment?.text = String(WavesCrypto.base58decode(transaction.attachment!!))
        } else {
            commentBlock.gone()
        }

        when (transaction.transactionType()) {
            TransactionType.RECEIVED_TYPE,
            TransactionType.MASS_RECEIVE_TYPE,
            TransactionType.SPAM_RECEIVE_TYPE,
            TransactionType.MASS_SPAM_RECEIVE_TYPE,
            TransactionType.UNRECOGNISED_TYPE -> {
                val receiveView = inflater?.inflate(R.layout.fragment_bottom_sheet_receive_layout, historyContainer, false)
                val receivedFromName = receiveView?.findViewById<AppCompatTextView>(R.id.text_received_from_name)
                val imageCopy = receiveView?.findViewById<AppCompatImageView>(R.id.image_address_copy)
                val receivedFromAddress = receiveView?.findViewById<AppCompatTextView>(R.id.text_received_from_address)
                val textAddressAction = receiveView?.findViewById<AppCompatTextView>(R.id.text_address_action)
                val spamTag = receiveView?.findViewById<SpamTag>(R.id.spam_tag)

                eventSubscriptions.add(RxView.clicks(imageCopy!!)
                        .throttleFirst(1500, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            imageCopy.copyToClipboard(receivedFromAddress?.text.toString())
                        })

                receivedFromAddress?.text = transaction.sender

                textAddressAction?.goneIf {
                    transaction.transactionType() == TransactionType.SPAM_RECEIVE_TYPE ||
                            transaction.transactionType() == TransactionType.MASS_SPAM_RECEIVE_TYPE
                }

                spamTag?.visiableIf {
                    transaction.transactionType() == TransactionType.SPAM_RECEIVE_TYPE ||
                            transaction.transactionType() == TransactionType.MASS_SPAM_RECEIVE_TYPE
                }

                resolveExistOrNoAddress(receivedFromName, receivedFromAddress, textAddressAction)

                historyContainer?.addView(receiveView)
            }
            TransactionType.SENT_TYPE -> {
                val sendView = inflater?.inflate(R.layout.fragment_bottom_sheet_send_layout, historyContainer, false)
                val sentToName = sendView?.findViewById<AppCompatTextView>(R.id.text_sent_to_name)
                val sentAddress = sendView?.findViewById<AppCompatTextView>(R.id.text_sent_address)
                val imageCopy = sendView?.findViewById<AppCompatImageView>(R.id.image_address_copy)
                val imageAddressAction = sendView?.findViewById<AppCompatTextView>(R.id.text_address_action)

                var recipient = transaction.recipient.parseAlias()
                if (TextUtils.isEmpty(recipient)) {
                    recipient = transaction.recipientAddress ?: ""
                }
                sentAddress?.text = recipient

                eventSubscriptions.add(RxView.clicks(imageCopy!!)
                        .throttleFirst(1500, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            imageCopy.copyToClipboard(sentAddress?.text.toString())
                        })

                resolveExistOrNoAddress(sentToName, sentAddress, imageAddressAction)

                historyContainer?.addView(sendView)
            }
            TransactionType.STARTED_LEASING_TYPE -> {
                val startLeaseView = inflater?.inflate(R.layout.fragment_bottom_sheet_start_lease_layout, historyContainer, false)
                val textLeasingToName = startLeaseView?.findViewById<TextView>(R.id.text_leasing_to_name)
                val imageCopy = startLeaseView?.findViewById<AppCompatImageView>(R.id.image_address_copy)
                val textLeasingToAddress = startLeaseView?.findViewById<AppCompatTextView>(R.id.text_leasing_to_address)
                val imageAddressAction = startLeaseView?.findViewById<AppCompatTextView>(R.id.text_address_action)

                val nodeLeasingRecipient = transaction.lease?.recipient?.parseAlias()
                if (nodeLeasingRecipient.isNullOrEmpty()) {
                    textLeasingToAddress?.text = transaction.recipient.parseAlias()
                } else {
                    textLeasingToAddress?.text = nodeLeasingRecipient
                }

                eventSubscriptions.add(RxView.clicks(imageCopy!!)
                        .throttleFirst(1500, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            imageCopy.copyToClipboard(textLeasingToAddress?.text.toString())
                        })

                resolveExistOrNoAddress(textLeasingToName, textLeasingToAddress, imageAddressAction)

                historyContainer?.addView(startLeaseView)
            }
            TransactionType.EXCHANGE_TYPE -> {
                val exchangeView = inflater?.inflate(
                        R.layout.fragment_bottom_sheet_exchange_layout,
                        historyContainer, false)


                exchangeView?.let {
                    val myOrder = findMyOrder(transaction.order1!!, transaction.order2!!,
                            App.getAccessManager().getWallet()?.address!!)

                    if (myOrder.getType() == OrderType.BUY) {
                        exchangeView.history_details_type?.text = getString(R.string.history_exchange_sell)
                    } else {
                        exchangeView.history_details_type?.text = getString(R.string.history_exchange_buy)
                    }

                    exchangeView.text_exchange_value.text = transaction.getScaledTotal(myOrder.assetPair?.priceAssetObject?.precision)
                    exchangeView.text_price_value.text = transaction.getScaledPrice(myOrder.assetPair?.amountAssetObject?.precision,
                            myOrder.assetPair?.priceAssetObject?.precision)

                    showTickerOrSimple(exchangeView.text_exchange_value, exchangeView.text_exchange_tag, myOrder.assetPair?.priceAssetObject)
                    showTickerOrSimple(exchangeView.text_price_value, exchangeView.text_price_tag, myOrder.assetPair?.priceAssetObject)
                }

                historyContainer.addView(exchangeView)
            }
            TransactionType.SELF_TRANSFER_TYPE -> {
                val selfTransferView = inflater?.inflate(R.layout.fragment_bottom_sheet_seft_transfer_layout, historyContainer, false)

                historyContainer.addView(selfTransferView)
            }
            TransactionType.CANCELED_LEASING_TYPE,
            TransactionType.INCOMING_LEASING_TYPE -> {
                val receiveView = inflater?.inflate(R.layout.fragment_bottom_sheet_cancel_or_incoming_leasing_layout, historyContainer, false)
                val textCancelLeasingFromName = receiveView?.findViewById<AppCompatTextView>(R.id.text_cancel_leasing_from_name)
                val textCancelLeasingFromAddress = receiveView?.findViewById<AppCompatTextView>(R.id.text_cancel_leasing_from_address)
                val imageCopy = receiveView?.findViewById<AppCompatImageView>(R.id.image_address_copy)
                val imageAddressAction = receiveView?.findViewById<AppCompatTextView>(R.id.text_address_action)

                val nodeLeasingRecipient = transaction.lease?.recipient?.parseAlias()
                if (nodeLeasingRecipient.isNullOrEmpty()) {
                    textCancelLeasingFromAddress?.text = transaction.recipient.parseAlias()
                } else {
                    textCancelLeasingFromAddress?.text = nodeLeasingRecipient
                }

                if (transaction.transactionType() == TransactionType.INCOMING_LEASING_TYPE) {
                    receiveView?.findViewById<AppCompatTextView>(R.id.text_received_from_hint)?.text = getString(R.string.history_details_leasing_from)
                    textCancelLeasingFromAddress?.text = transaction.sender
                }

                eventSubscriptions.add(RxView.clicks(imageCopy!!)
                        .throttleFirst(1500, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            imageCopy.copyToClipboard(textCancelLeasingFromAddress?.text.toString())
                        })

                resolveExistOrNoAddress(textCancelLeasingFromName, textCancelLeasingFromAddress, imageAddressAction)

                historyContainer?.addView(receiveView)
            }
            TransactionType.TOKEN_GENERATION_TYPE,
            TransactionType.TOKEN_BURN_TYPE,
            TransactionType.TOKEN_REISSUE_TYPE -> {
                val tokenView = inflater?.inflate(R.layout.fragment_bottom_sheet_token_layout, historyContainer, false)
                val textIdValue = tokenView?.findViewById<TextView>(R.id.text_id_value)
                val imageCopy = tokenView?.findViewById<AppCompatImageView>(R.id.image_copy)
                val textTokenStatus = tokenView?.findViewById<TextView>(R.id.text_token_status)

                textIdValue?.text = transaction.assetId
                if (transaction.asset?.reissuable == true) {
                    textTokenStatus?.text = getString(R.string.history_details_reissuable)
                } else {
                    textTokenStatus?.text = getString(R.string.history_details_not_reissuable)
                }

                eventSubscriptions.add(RxView.clicks(imageCopy!!)
                        .throttleFirst(1500, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            imageCopy.copyToClipboard(textIdValue?.text.toString())
                        })

                historyContainer.addView(tokenView)
            }
            TransactionType.CREATE_ALIAS_TYPE,
            TransactionType.DATA_TYPE,
            TransactionType.CANCEL_ADDRESS_SCRIPT_TYPE,
            TransactionType.SET_ADDRESS_SCRIPT_TYPE -> {
                val createAliasView = inflater?.inflate(R.layout.fragment_bottom_sheet_only_line_layout, historyContainer, false)

                historyContainer.addView(createAliasView)
            }
            TransactionType.MASS_SEND_TYPE -> {
                val massSendLayout = inflater?.inflate(R.layout.fragment_bottom_sheet_mass_send_layout, null, false)
                val addressContainer = massSendLayout?.findViewById<LinearLayout>(R.id.container_address)
                val showMoreAddress = massSendLayout?.findViewById<TextView>(R.id.text_show_more_address)

                val transfers: MutableList<TransferResponse> = transaction.transfers.toMutableList()

                transfers.forEachIndexed { index, transfer ->
                    val addressView = inflater?.inflate(R.layout.content_address_layout, null, false)
                    val textRecipientNumber = addressView?.findViewById<AppCompatTextView>(R.id.text_recipient_number)
                    val textSentAddress = addressView?.findViewById<TextView>(R.id.text_sent_address)
                    val textSendAmountTag = addressView?.findViewById<AppCompatTextView>(R.id.text_send_amount_tag)
                    val imageCopy = addressView?.findViewById<AppCompatImageView>(R.id.image_copy)
                    val textSentName = addressView?.findViewById<TextView>(R.id.text_sent_name)
                    val textSentAmount = addressView?.findViewById<TextView>(R.id.text_sent_amount)
                    val imageAddressAction = addressView?.findViewById<AppCompatImageView>(R.id.image_address_action)
                    val viewDivider = addressView?.findViewById<View>(R.id.view_divider)

                    if (isSpamConsidered(transaction.assetId, prefsUtil)) {
                        textSentAmount?.text = MoneyUtil.getScaledText(transfer.amount, transaction.asset).stripZeros()
                    } else {
                        if (isShowTicker(transaction.assetId)) {
                            textSentAmount?.text = MoneyUtil.getScaledText(transfer.amount, transaction.asset).stripZeros()
                            val ticker = transaction.asset?.getTokenTicker()
                            if (!ticker.isNullOrBlank()) {
                                textSendAmountTag?.text = ticker
                                textSendAmountTag?.visiable()
                            }
                        } else {
                            textSentAmount?.text = "${MoneyUtil.getScaledText(transfer.amount, transaction.asset).stripZeros()} ${transaction.asset?.name}"
                        }
                    }

                    textRecipientNumber?.text = getString(R.string.history_mass_send_recipient, index.inc().toString())

                    var recipient = transfer.recipient.parseAlias()
                    if (TextUtils.isEmpty(recipient)) {
                        recipient = transfer.recipientAddress ?: ""
                    }
                    textSentAddress?.text = recipient

                    eventSubscriptions.add(RxView.clicks(imageCopy!!)
                            .throttleFirst(1500, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                imageCopy.copyToClipboard(textSentAddress?.text.toString())
                            })

                    resolveExistOrNoAddressForMassSend(textSentName, textSentAddress, imageAddressAction)

                    if (index >= 3) {
                        showMoreAddress?.visiable()
                        showMoreAddress?.text = getString(R.string.history_details_show_all, (transfers.size).toString())

                        showMoreAddress?.click {
                            showMoreAddress.gone()

                            for (i in 3 until transfers.size) {
                                val addressView = inflater?.inflate(R.layout.content_address_layout, null, false)
                                val imageCopy = addressView?.findViewById<AppCompatImageView>(R.id.image_copy)
                                val textSendAmountTag = addressView?.findViewById<AppCompatTextView>(R.id.text_send_amount_tag)
                                val textRecipientNumber = addressView?.findViewById<AppCompatTextView>(R.id.text_recipient_number)
                                val textSentAddress = addressView?.findViewById<AppCompatTextView>(R.id.text_sent_address)
                                val textSentName = addressView?.findViewById<TextView>(R.id.text_sent_name)
                                val textSentAmount = addressView?.findViewById<AppCompatTextView>(R.id.text_sent_amount)
                                val imageAddressAction = addressView?.findViewById<AppCompatImageView>(R.id.image_address_action)
                                val viewDivider = addressView?.findViewById<View>(R.id.view_divider)

                                textRecipientNumber?.text = getString(R.string.history_mass_send_recipient, index.inc().toString())

                                val transfer = transfers[i]

                                var recipient = transfer.recipient.parseAlias()
                                if (TextUtils.isEmpty(recipient)) {
                                    recipient = transfer.recipientAddress ?: ""
                                }
                                textSentAddress?.text = recipient

                                eventSubscriptions.add(RxView.clicks(imageCopy!!)
                                        .throttleFirst(1500, TimeUnit.MILLISECONDS)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe {
                                            imageCopy.copyToClipboard(textSentAddress?.text.toString())
                                        })

                                if (isSpamConsidered(transaction.assetId, prefsUtil)) {
                                    textSentAmount?.text = MoneyUtil.getScaledText(transfer.amount, transaction.asset).stripZeros()
                                } else {
                                    if (isShowTicker(transaction.assetId)) {
                                        textSentAmount?.text = MoneyUtil.getScaledText(transfer.amount, transaction.asset).stripZeros()
                                        val ticker = transaction.asset?.getTokenTicker()
                                        if (!ticker.isNullOrBlank()) {
                                            textSendAmountTag?.text = ticker
                                            textSendAmountTag?.visiable()
                                        }
                                    } else {
                                        textSentAmount?.text = "${MoneyUtil.getScaledText(transfer.amount, transaction.asset).stripZeros()} ${transaction.asset?.name}"
                                    }
                                }

                                resolveExistOrNoAddressForMassSend(textSentName, textSentAddress, imageAddressAction)

                                if (i == transfers.size - 1) viewDivider?.gone()

                                addressContainer?.addView(addressView)
                            }
                        }
                    } else {
                        if (index == transfers.size - 1) viewDivider?.gone()

                        addressContainer?.addView(addressView)
                    }
                }
                historyContainer?.addView(massSendLayout)
//                historyContainer?.addView(button)
            }
            TransactionType.SET_SPONSORSHIP_TYPE -> {
                val sponsorView = inflater?.inflate(R.layout.fragment_bottom_sheet_set_sponsorship_layout, historyContainer, false)
                val textIdValue = sponsorView?.findViewById<TextView>(R.id.text_id_value)
                val imageCopy = sponsorView?.findViewById<AppCompatImageView>(R.id.image_copy)
                val textAmountPerTransValue = sponsorView?.findViewById<TextView>(R.id.text_amount_per_trans_value)

                textIdValue?.text = transaction.assetId
                textAmountPerTransValue?.text = "${
                getScaledAmount(transaction.minSponsoredAssetFee?.toLong()
                        ?: 0, transaction.asset?.precision ?: 0)
                } ${transaction.asset?.name}"

                eventSubscriptions.add(RxView.clicks(imageCopy!!)
                        .throttleFirst(1500, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            imageCopy.copyToClipboard(textIdValue?.text.toString())
                        })

                historyContainer?.addView(sponsorView)
            }
            TransactionType.SCRIPT_INVOCATION_TYPE -> {
                val view = inflater?.inflate(R.layout.fragment_bottom_sheet_script_invocation_layout, historyContainer, false)

                view?.let {
                    view.text_script_address_value?.text = transaction.dApp

                    val payment = transaction.payment.firstOrNull()

                    if (payment != null) {
                        view.relative_payment.visiable()
                        if (isShowTicker(payment.assetId)) {
                            view.text_payment_value?.text = MoneyUtil.getScaledText(payment.amount, payment.asset).stripZeros()
                            val ticker = transaction.asset?.getTokenTicker()
                            if (!ticker.isNullOrBlank()) {
                                view.text_payment_tag?.text = ticker
                                view.text_payment_tag?.visiable()
                            }
                        } else {
                            view.text_payment_value?.text = "${MoneyUtil.getScaledText(payment.amount, payment.asset).stripZeros()} ${payment.asset?.name}"
                        }
                    } else {
                        view.relative_payment.gone()
                    }

                    eventSubscriptions.add(RxView.clicks(view.image_copy)
                            .throttleFirst(1500, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                view.image_copy.copyToClipboard(view.text_script_address_value.text.toString())
                            })
                }

                historyContainer.addView(view)
            }
            TransactionType.UPDATE_ASSET_SCRIPT_TYPE -> {
                val tokenView = inflater?.inflate(R.layout.fragment_bottom_sheet_set_asset_script_layout, historyContainer, false)
                val textAssetValue = tokenView?.findViewById<AppCompatTextView>(R.id.text_asset_value)
                val imageAssetIcon = tokenView?.findViewById<AssetAvatarView>(R.id.image_asset_icon)

                transaction.asset?.let {
                    imageAssetIcon?.setAsset(it)
                }

                textAssetValue?.text = transaction.asset?.name

                historyContainer?.addView(tokenView)
            }
            TransactionType.CANCEL_SPONSORSHIP_TYPE,
            TransactionType.RECEIVE_SPONSORSHIP_TYPE -> {
                val tokenView = inflater?.inflate(R.layout.fragment_bottom_sheet_token_layout, historyContainer, false)
                val textIdValue = tokenView?.findViewById<TextView>(R.id.text_id_value)
                val imageCopy = tokenView?.findViewById<AppCompatImageView>(R.id.image_copy)
                val textTokenStatus = tokenView?.findViewById<TextView>(R.id.text_token_status)

                textIdValue?.text =
                        if (transaction.feeAssetId?.isNotEmpty() == true) {
                            transaction.feeAssetId
                        } else {
                            transaction.assetId
                        }

                // force hide for this types of transaction
                commentBlock.gone()

                eventSubscriptions.add(RxView.clicks(imageCopy!!)
                        .throttleFirst(1500, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            imageCopy.copyToClipboard(textIdValue?.text.toString())
                        })

                textTokenStatus?.gone()

                historyContainer.addView(tokenView)
            }
            else -> {
            }
        }

        historyContainer.addView(commentBlock)

        return historyContainer
    }


    private fun showTickerOrSimple(valueView: AppCompatTextView, tickerView: AppCompatTextView, assetInfo: AssetInfoResponse?) {
        if (isShowTicker(assetInfo?.id)) {
            val ticker = assetInfo?.getTokenTicker()
            if (!ticker.isNullOrBlank()) {
                tickerView.text = ticker
                tickerView.visiable()
            }
        } else {
            valueView.text = valueView.text.toString().plus(" ${assetInfo?.name}")
        }
    }

    override fun setupInfo(transaction: HistoryTransactionResponse): View? {
        val layout = inflate(R.layout.fragment_history_bottom_sheet_base_info_layout)

        fun showTransactionFee() {
            if (transaction.feeAssetObject?.name?.isWaves() == true) {
                layout.text_fee?.text = MoneyUtil.getScaledText(transaction.fee, transaction.feeAssetObject).stripZeros()
                layout.text_base_info_tag.visiable()
            } else {
                layout.text_fee?.text = "${MoneyUtil.getScaledText(transaction.fee, transaction.feeAssetObject).stripZeros()} ${transaction.feeAssetObject?.name}"
                layout.text_base_info_tag.gone()
            }
        }

        val confirmations = preferencesHelper.currentBlocksHeight - transaction.height
        layout.text_confirmations?.text = if (confirmations < 0) {
            "0"
        } else {
            confirmations.toString()
        }
        layout.text_block?.text = transaction.height.toString()
        layout.text_timestamp?.text = transaction.timestamp.date("dd.MM.yyyy HH:mm")

        showTransactionFee()

        if (preferencesHelper.currentBlocksHeight.minus(transaction.height) > 0) {
            layout.text_status?.setBackgroundResource(R.drawable.success400_01_shape)
            layout.text_status?.text = getString(R.string.history_details_completed)
            layout.text_status?.setTextColor(findColor(R.color.success500))
        } else {
            layout.text_status?.setBackgroundResource(R.drawable.warning400_01_shape)
            layout.text_status?.text = getString(R.string.history_details_unconfirmed)
            layout.text_status?.setTextColor(findColor(R.color.warning600))
        }

        return layout
    }

    override fun setupFooter(transaction: HistoryTransactionResponse): View? {
        val view = inflater?.inflate(R.layout.fragment_history_bottom_sheet_bottom_btns, null, false)

        fun changeViewMargin(view: View) {
            val llp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            llp.setMargins(dp2px(8), 0, 0, 0)
            view.layoutParams = llp
        }

        eventSubscriptions.add(RxView.clicks(view!!.image_close)
                .throttleFirst(1500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    dismiss()
                })

        eventSubscriptions.add(RxView.clicks(view.text_copy_tx_id)
                .throttleFirst(1500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    copyToClipboard(transaction.id, view?.text_copy_tx_id, R.string.history_details_copy_tx_id)
                })

        eventSubscriptions.add(RxView.clicks(view.text_copy_all_data)
                .throttleFirst(1500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    copyToClipboard(HistoryTransactionResponse.getInfo(transaction,
                            WavesWallet.getAddress()), view.text_copy_all_data,
                            R.string.history_details_copy_all_data)
                })

        if (BuildConfig.DEBUG) {
            view.check_box_staging_on_explorer.visibility = View.VISIBLE
        }

        eventSubscriptions.add(RxView.clicks(view.text_view_on_explorer)
                .throttleFirst(1500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (view.check_box_staging_on_explorer.isChecked) {
                        openUrlWithChromeTab(String.format(
                                WavesConstants.URL_WAVES_STAGE_EXPLORER,
                                transaction.id))
                    } else {
                        openUrlWithChromeTab(String.format(
                                WavesConstants.URL_WAVES_EXPLORER,
                                transaction.id))
                    }
                })

        when (transaction.transactionType()) {
            TransactionType.SENT_TYPE -> {
                view.text_send_again.visiable()

                changeViewMargin(view.text_view_on_explorer)

                val assetBalance = queryFirst<AssetBalanceDb> {
                    equalTo("assetId", transaction.assetId ?: "")
                }?.convertFromDb()
                if (assetBalance != null && (nonGateway(assetBalance, transaction)
                                || assetBalance.isWaves() || assetBalance.isFiatMoney)) {
                    eventSubscriptions.add(RxView.clicks(view.text_send_again)
                            .throttleFirst(1500, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                launchActivity<SendActivity> {
                                    putExtra(SendActivity.KEY_INTENT_REPEAT_TRANSACTION, true)
                                    putExtra(SendActivity.KEY_INTENT_TRANSACTION_ASSET_BALANCE, assetBalance)
                                    putExtra(SendActivity.KEY_INTENT_TRANSACTION_AMOUNT,
                                            MoneyUtil.getScaledText(transaction.amount, transaction.asset)
                                                    .clearBalance())
                                    putExtra(SendActivity.KEY_INTENT_TRANSACTION_RECIPIENT,
                                            transaction.recipientAddress)
                                    if (!transaction.attachment.isNullOrEmpty()) {
                                        putExtra(SendActivity.KEY_INTENT_TRANSACTION_ATTACHMENT,
                                                String(WavesCrypto.base58decode(transaction.attachment!!)))
                                    } else {
                                        putExtra(SendActivity.KEY_INTENT_TRANSACTION_ATTACHMENT, "")
                                    }
                                }
                            })
                }
            }
            TransactionType.STARTED_LEASING_TYPE -> {
                if (transaction.status == LeasingStatus.ACTIVE.status) {

                    view.text_cancel_leasing.visiable()

                    changeViewMargin(view.text_view_on_explorer)

                    eventSubscriptions.add(RxView.clicks(view.text_cancel_leasing)
                            .throttleFirst(1500, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                launchActivity<ConfirmationCancelLeasingActivity>(
                                        ConfirmationCancelLeasingActivity.REQUEST_CANCEL_LEASING_CONFIRMATION) {
                                    putExtra(ConfirmationCancelLeasingActivity.BUNDLE_CANCEL_CONFIRMATION_LEASING_TX,
                                            transaction.id)
                                    putExtra(ConfirmationCancelLeasingActivity.BUNDLE_ADDRESS, transaction.recipient)
                                    putExtra(ConfirmationCancelLeasingActivity.BUNDLE_AMOUNT,
                                            MoneyUtil.getScaledText(transaction.amount, transaction.asset))
                                }
                            })
                }
            }
            else -> {
                view.text_send_again.gone()
                view.text_cancel_leasing.gone()
            }
        }
        return view
    }

    private fun setExchangeItem(transaction: HistoryTransactionResponse, view: View) {
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

        if (myOrder.orderType == WavesConstants.SELL_ORDER_TYPE) {
            directionStringResId = R.string.history_my_dex_intent_sell
            directionSign = "-"
        } else {
            directionStringResId = R.string.history_my_dex_intent_buy
            directionSign = "+"
        }

        view.text_transaction_name.text = getString(
                directionStringResId,
                amountAsset.name,
                secondOrder.assetPair?.priceAssetObject?.name)

        if (isShowTicker(amountAsset.id)) {
            val ticker = amountAsset.getTokenTicker()
            if (!ticker.isNullOrBlank()) {
                view.text_tag.text = ticker
                view.text_tag.visiable()
            }
            view.text_transaction_value.text = directionSign + amountValue
        } else {
            view.text_transaction_value.text = directionSign + amountValue + " ${amountAsset.name}"
        }
    }

    private fun nonGateway(assetBalance: AssetBalanceResponse, transaction: HistoryTransactionResponse) =
            !assetBalance.isGateway || (assetBalance.isGateway &&
                    !transaction.recipientAddress.equals(CoinomatService.GATEWAY_ADDRESS))

    private fun resolveExistOrNoAddress(textViewName: TextView?, textViewAddress: TextView?, textAddressAction: AppCompatTextView?) {
        val addressBookUser = queryFirst<AddressBookUserDb> { equalTo("address", textViewAddress?.text.toString()) }
        makeAddressActionViewClickableStyled(textAddressAction)

        if (addressBookUser == null) {
            //  not exist
            textViewName?.gone()
            textViewAddress?.textSize = 14f

            textAddressAction?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_address_submit_300, 0, 0, 0)
            textAddressAction?.text = getString(R.string.history_details_add_address)

            textAddressAction?.click {
                analytics.trackEvent(AnalyticEvents.TransactionAddressSaveEvent)
                launchActivity<AddAddressActivity>(AddressBookActivity.REQUEST_ADD_ADDRESS) {
                    putExtra(AddressBookActivity.BUNDLE_TYPE, AddressBookActivity.SCREEN_TYPE_NOT_EDITABLE)
                    putExtra(AddressBookActivity.BUNDLE_ADDRESS_ITEM, AddressBookUserDb(textViewAddress?.text.toString(), ""))
                }
            }
        } else {
            // exist
            textViewName?.text = addressBookUser.name
            textViewName?.visiable()
            textViewAddress?.textSize = 12f

            textAddressAction?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_edit_address_submit_300, 0, 0, 0)
            textAddressAction?.text = getString(R.string.history_details_edit_address)

            textAddressAction?.click {
                analytics.trackEvent(AnalyticEvents.TransactionAddressEditEvent)
                launchActivity<EditAddressActivity>(AddressBookActivity.REQUEST_EDIT_ADDRESS) {
                    putExtra(AddressBookActivity.BUNDLE_TYPE, AddressBookActivity.SCREEN_TYPE_NOT_EDITABLE)
                    putExtra(AddressBookActivity.BUNDLE_ADDRESS_ITEM, AddressBookUserDb(textViewAddress?.text.toString(), addressBookUser.name))
                }
            }
        }
    }

    private fun makeAddressActionViewClickableStyled(view: View?) {
        view?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.alpha = 0.6f
                }
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    v.alpha = 1f
                }
            }
            return@setOnTouchListener false
        }
    }

    private fun resolveExistOrNoAddressForMassSend(textViewName: TextView?, textViewAddress: TextView?, imageAddressAction: AppCompatImageView?) {
        val addressBookUser = queryFirst<AddressBookUserDb> { equalTo("address", textViewAddress?.text.toString()) }

        makeAddressActionViewClickableStyled(imageAddressAction)

        if (addressBookUser == null) {
            //  not exist
            textViewName?.gone()
            textViewAddress?.textSize = 14f

            imageAddressAction?.setImageResource(R.drawable.ic_add_address_submit_300)

            imageAddressAction?.click {
                analytics.trackEvent(AnalyticEvents.TransactionAddressSaveEvent)
                launchActivity<AddAddressActivity>(AddressBookActivity.REQUEST_ADD_ADDRESS) {
                    putExtra(AddressBookActivity.BUNDLE_TYPE, AddressBookActivity.SCREEN_TYPE_NOT_EDITABLE)
                    putExtra(AddressBookActivity.BUNDLE_ADDRESS_ITEM, AddressBookUserDb(textViewAddress?.text.toString(), ""))
                }
            }
        } else {
            // exist
            textViewName?.text = addressBookUser.name
            textViewName?.visiable()
            textViewAddress?.textSize = 12f

            imageAddressAction?.setImageResource(R.drawable.ic_edit_address_submit_300)

            imageAddressAction?.click {
                analytics.trackEvent(AnalyticEvents.TransactionAddressEditEvent)
                launchActivity<EditAddressActivity>(AddressBookActivity.REQUEST_EDIT_ADDRESS) {
                    putExtra(AddressBookActivity.BUNDLE_TYPE, AddressBookActivity.SCREEN_TYPE_NOT_EDITABLE)
                    putExtra(AddressBookActivity.BUNDLE_ADDRESS_ITEM, AddressBookUserDb(textViewAddress?.text.toString(), addressBookUser.name))
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AddressBookActivity.REQUEST_EDIT_ADDRESS || requestCode == AddressBookActivity.REQUEST_ADD_ADDRESS) {
            if (resultCode == Constants.RESULT_OK || resultCode == Constants.RESULT_OK_NO_RESULT) {
                selectedItem?.let {
                    configureView()
                }
            }
        }

        if (requestCode == StartLeasingActivity.REQUEST_CANCEL_LEASING_CONFIRMATION) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    rxEventBus.post(Events.UpdateListOfActiveTransaction(selectedItemPosition))
                    dismiss()
                }
                Constants.RESULT_SMART_ERROR -> {
                    activity?.showAlertAboutScriptedAccount()
                }
            }
            if (resultCode == Activity.RESULT_OK) {
            }
        }
    }
}