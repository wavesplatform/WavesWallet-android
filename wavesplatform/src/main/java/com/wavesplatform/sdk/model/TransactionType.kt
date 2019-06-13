/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.wavesplatform.sdk.utils.WavesConstants
import com.wavesplatform.sdk.R

enum class TransactionType(
    var id: Int,
    @DrawableRes var image: Int,
    @StringRes var title: Int
) {
    RECEIVED_TYPE(WavesConstants.ID_RECEIVED_TYPE, R.drawable.ic_t_receive_48,
            R.string.history_type_receive),
    SENT_TYPE(WavesConstants.ID_SENT_TYPE, R.drawable.ic_t_send_48,
            R.string.history_type_sent),
    STARTED_LEASING_TYPE(WavesConstants.ID_STARTED_LEASING_TYPE, R.drawable.ic_t_startlease_48,
            R.string.history_type_started_leasing),
    SELF_TRANSFER_TYPE(WavesConstants.ID_SELF_TRANSFER_TYPE, R.drawable.ic_t_selftrans_48,
            R.string.history_type_self_transfer),
    CANCELED_LEASING_TYPE(WavesConstants.ID_CANCELED_LEASING_TYPE, R.drawable.ic_t_closelease_48,
            R.string.history_type_canceled_leasing),
    TOKEN_GENERATION_TYPE(WavesConstants.ID_TOKEN_GENERATION_TYPE, R.drawable.ic_t_tokengen_48,
            R.string.history_type_token_generation),
    TOKEN_BURN_TYPE(WavesConstants.ID_TOKEN_BURN_TYPE, R.drawable.ic_t_tokenburn_48,
            R.string.history_type_token_burn),
    TOKEN_REISSUE_TYPE(WavesConstants.ID_TOKEN_REISSUE_TYPE, R.drawable.ic_t_tokenreis_48,
            R.string.history_type_token_reissue),
    EXCHANGE_TYPE(WavesConstants.ID_EXCHANGE_TYPE, R.drawable.ic_t_exchange_48,
            R.string.history_type_exchange),
    CREATE_ALIAS_TYPE(WavesConstants.ID_CREATE_ALIAS_TYPE, R.drawable.ic_t_alias_48,
            R.string.history_type_create_alias),
    INCOMING_LEASING_TYPE(WavesConstants.ID_INCOMING_LEASING_TYPE, R.drawable.ic_t_incominglease_48,
            R.string.history_type_incoming_leasing),
    MASS_SEND_TYPE(WavesConstants.ID_MASS_SEND_TYPE, R.drawable.ic_t_masstransfer_48,
            R.string.history_type_mass_send),
    MASS_RECEIVE_TYPE(WavesConstants.ID_MASS_RECEIVE_TYPE, R.drawable.ic_t_massreceived_48,
            R.string.history_type_mass_receive),
    SPAM_RECEIVE_TYPE(WavesConstants.ID_SPAM_RECEIVE_TYPE, R.drawable.ic_t_spam_receive_48,
            R.string.history_type_spam_receive),
    MASS_SPAM_RECEIVE_TYPE(WavesConstants.ID_MASS_SPAM_RECEIVE_TYPE, R.drawable.ic_t_spam_massreceived_48,
            R.string.history_type_mass_spam_receive),
    SPAM_SELF_TRANSFER_TYPE(WavesConstants.ID_SPAM_SELF_TRANSFER, R.drawable.ic_t_spam_selftrans_48,
            R.string.history_type_self_transfer),
    DATA_TYPE(WavesConstants.ID_DATA_TYPE, R.drawable.ic_t_data_48,
            R.string.history_type_data),
    SET_SPONSORSHIP_TYPE(WavesConstants.ID_SET_SPONSORSHIP_TYPE, R.drawable.ic_t_sponsored_enable_48,
            R.string.history_type_set_sponsorship),
    CANCEL_SPONSORSHIP_TYPE(WavesConstants.ID_CANCEL_SPONSORSHIP_TYPE, R.drawable.ic_t_sponsored_disable_48,
            R.string.history_type_cancel_sponsorship),
    RECEIVE_SPONSORSHIP_TYPE(WavesConstants.ID_RECEIVE_SPONSORSHIP_TYPE, R.drawable.ic_t_sponsored_plus_48,
            R.string.history_type_receive_sponsorship),
    SET_ADDRESS_SCRIPT_TYPE(WavesConstants.ID_SET_ADDRESS_SCRIPT_TYPE, R.drawable.ic_t_setscript_48,
            R.string.history_type_set_address_script),
    CANCEL_ADDRESS_SCRIPT_TYPE(WavesConstants.ID_CANCEL_ADDRESS_SCRIPT_TYPE, R.drawable.ic_t_setscript_cancel_48,
            R.string.history_type_cancel_address_script),
    UPDATE_ASSET_SCRIPT_TYPE(WavesConstants.ID_UPDATE_ASSET_SCRIPT_TYPE, R.drawable.ic_t_setassetscript_48,
            R.string.history_type_update_asset_script),
    SCRIPT_INVOCATION_TYPE(WavesConstants.ID_SCRIPT_INVOCATION_TYPE, R.drawable.ic_t_invocationscript_48,
            R.string.history_type_script_invocation),
    UNRECOGNISED_TYPE(WavesConstants.ID_UNRECOGNISED_TYPE, R.drawable.ic_t_undefined_48,
            R.string.history_type_unrecognised);

    companion object {

        fun getTypeById(id: Int): TransactionType {
            values().forEach {
                if (it.id == id) return it
            }
            return UNRECOGNISED_TYPE
        }

        fun isZeroTransferOrExchange(type: TransactionType): Boolean {
            return (type == CREATE_ALIAS_TYPE ||
                    type == DATA_TYPE ||
                    type == SET_ADDRESS_SCRIPT_TYPE ||
                    type == CANCEL_ADDRESS_SCRIPT_TYPE ||
                    type == SET_SPONSORSHIP_TYPE ||
                    type == RECEIVE_SPONSORSHIP_TYPE ||
                    type == CANCEL_SPONSORSHIP_TYPE ||
                    type == UPDATE_ASSET_SCRIPT_TYPE ||
                    type == SCRIPT_INVOCATION_TYPE ||
                    type == EXCHANGE_TYPE)
        }
    }
}