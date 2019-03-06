package com.wavesplatform.wallet.v2.ui.home.profile.addresses.alias.create

import com.wavesplatform.wallet.v2.data.model.remote.response.Alias
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface CreateAliasView : BaseMvpView {
    fun aliasIsAvailable()
    fun aliasIsNotAvailable()
    fun successCreateAlias(alias: Alias)
    fun failedCreateAlias(message: String?)
    fun failedCreateAliasCauseSmart()
}
