/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.profile.addresses.alias.create

import com.wavesplatform.sdk.net.model.response.AliasResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface CreateAliasView : BaseMvpView {
    fun aliasIsAvailable()
    fun aliasIsNotAvailable()
    fun successCreateAlias(alias: AliasResponse)
    fun failedCreateAlias(message: String?)
    fun failedCreateAliasCauseSmart()
}
