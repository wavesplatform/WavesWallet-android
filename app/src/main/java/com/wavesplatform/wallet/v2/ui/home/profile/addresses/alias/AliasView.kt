package com.wavesplatform.wallet.v2.ui.home.profile.addresses.alias

import com.wavesplatform.sdk.model.response.Alias
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface AliasView : BaseMvpView {
    fun afterSuccessLoadAliases(ownAliases: List<Alias>)
}
