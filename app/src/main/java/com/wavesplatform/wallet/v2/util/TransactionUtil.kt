package com.wavesplatform.wallet.v2.util

import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.helpers.PublicKeyAccountHelper
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import javax.inject.Inject

/**
 * Created by anonymous on 07.03.18.
 */

class TransactionUtil @Inject constructor(var keyAccountHelper: PublicKeyAccountHelper) {
    fun getTransactionType(transaction: Transaction): Int =
            if (transaction.type == 4 && transaction.sender != keyAccountHelper.publicKeyAccount?.address && transaction.asset?.isSpam == true) Constants.ID_SPAM_RECEIVE_TYPE
            else if (transaction.type == 11 && transaction.sender != keyAccountHelper.publicKeyAccount?.address && transaction.asset?.isSpam == true) Constants.ID_MASS_SPAM_RECEIVE_TYPE
            else if ((transaction.type == 4 || transaction.type == 9) && transaction.sender != keyAccountHelper.publicKeyAccount?.address) Constants.ID_RECEIVED_TYPE
            else if (transaction.type == 4 && transaction.sender == transaction.recipientAddress) Constants.ID_SELF_TRANSFER_TYPE
            else if (transaction.type == 4 && transaction.sender == keyAccountHelper.publicKeyAccount?.address) Constants.ID_SENT_TYPE
            else if (transaction.type == 8 && transaction.recipientAddress != keyAccountHelper.publicKeyAccount?.address) Constants.ID_STARTED_LEASING_TYPE
            else if (transaction.type == 7) Constants.ID_EXCHANGE_TYPE
            else if (transaction.type == 9) Constants.ID_CANCELED_LEASING_TYPE
            else if (transaction.type == 3) Constants.ID_TOKEN_GENERATION_TYPE
            else if (transaction.type == 6) Constants.ID_TOKEN_BURN_TYPE
            else if (transaction.type == 5) Constants.ID_TOKEN_REISSUE_TYPE
            else if (transaction.type == 10) Constants.ID_CREATE_ALIAS_TYPE
            else if (transaction.type == 8 && transaction.recipientAddress == keyAccountHelper.publicKeyAccount?.address) Constants.ID_INCOMING_LEASING_TYPE
            else if (transaction.type == 11 && transaction.sender == keyAccountHelper.publicKeyAccount?.address) Constants.ID_MASS_SEND_TYPE
            else if (transaction.type == 11 && transaction.sender != keyAccountHelper.publicKeyAccount?.address) Constants.ID_MASS_RECEIVE_TYPE
            else Constants.ID_UNRECOGNISED_TYPE
}