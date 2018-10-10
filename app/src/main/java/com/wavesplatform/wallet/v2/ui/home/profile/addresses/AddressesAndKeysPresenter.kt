package com.wavesplatform.wallet.v2.ui.home.profile.addresses

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsync
import com.wavesplatform.wallet.v2.data.model.remote.response.Alias
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import pyxis.uzuki.live.richutilskt.utils.runAsync
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import javax.inject.Inject

@InjectViewState
class AddressesAndKeysPresenter @Inject constructor() : BasePresenter<AddressesAndKeysView>() {


    fun loadAliases() {
        runAsync {
            queryAllAsync<Alias> { aliases ->
                val ownAliases = aliases.filter { it.own }
                runOnUiThread { viewState.afterSuccessLoadAliases(ownAliases) }
            }
        }
    }

}
