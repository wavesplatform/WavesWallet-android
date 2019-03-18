package com.wavesplatform.wallet.v2.ui.home.profile.addresses

import com.wavesplatform.sdk.net.model.response.Alias
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface AddressesAndKeysView : BaseMvpView {
    fun afterSuccessLoadAliases(ownAliases: List<Alias>)
}
