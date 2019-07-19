/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.profile.addresses.alias.create

import com.wavesplatform.sdk.model.response.node.transaction.AliasTransactionResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface CreateAliasView : BaseMvpView {
    fun aliasIsAvailable()
    fun aliasIsNotAvailable()
    fun successCreateAlias(alias: AliasTransactionResponse)
    fun failedCreateAlias(message: String?)
    fun failedCreateAliasCauseSmart()
}
