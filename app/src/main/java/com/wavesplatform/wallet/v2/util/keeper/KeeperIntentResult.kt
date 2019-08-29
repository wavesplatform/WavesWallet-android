/*
 * Created by Eduard Zaydel on 29/8/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.util.keeper

import android.os.Parcelable
import com.wavesplatform.sdk.keeper.interfaces.KeeperTransaction
import com.wavesplatform.sdk.keeper.interfaces.KeeperTransactionResponse
import kotlinx.android.parcel.Parcelize

sealed class KeeperIntentResult : Parcelable {
    @Parcelize
    data class SuccessSignResult(val transaction: KeeperTransaction) : KeeperIntentResult()

    @Parcelize
    data class ErrorSignResult(val error: String) : KeeperIntentResult()

    @Parcelize
    data class SuccessSendResult(val transaction: KeeperTransactionResponse) : KeeperIntentResult()

    @Parcelize
    data class ErrorSendResult(val error: String) : KeeperIntentResult()
}