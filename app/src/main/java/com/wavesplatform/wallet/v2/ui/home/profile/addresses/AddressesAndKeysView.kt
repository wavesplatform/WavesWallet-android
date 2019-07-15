/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.profile.addresses

import com.wavesplatform.sdk.model.response.node.transaction.AliasTransactionResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface AddressesAndKeysView : BaseMvpView {
    fun afterSuccessLoadAliases(ownAliases: MutableList<AliasTransactionResponse>)
}
