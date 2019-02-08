package com.wavesplatform.wallet.v2.ui.home.profile.addresses.alias.create

import com.wavesplatform.sdk.model.response.Alias
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface CreateAliasView : BaseMvpView{
    fun aliasIsAvailable()
    fun aliasIsNotAvailable()
    fun successCreateAlias(alias: Alias)
    fun failedCreateAlias(message: String?)
    fun failedCreateAliasCauseSmart()
}
