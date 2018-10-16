package com.wavesplatform.wallet.v2.ui.home.profile.addresses

import com.wavesplatform.wallet.v2.data.model.remote.response.Alias
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface AddressesAndKeysView : BaseMvpView {
    fun afterSuccessLoadAliases(ownAliases: List<Alias>)
}
