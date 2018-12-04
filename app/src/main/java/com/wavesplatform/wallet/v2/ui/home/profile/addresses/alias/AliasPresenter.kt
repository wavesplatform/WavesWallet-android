package com.wavesplatform.wallet.v2.ui.home.profile.addresses.alias

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsSingle
import com.wavesplatform.wallet.v2.data.model.remote.response.Alias
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import pyxis.uzuki.live.richutilskt.utils.runAsync
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import javax.inject.Inject

@InjectViewState
class AliasPresenter @Inject constructor() : BasePresenter<AliasView>() {

    fun loadAliases(callback: (List<Alias>) -> Unit) {
        runAsync {
            addSubscription(
                    queryAllAsSingle<Alias>().toObservable()
                            .compose(RxUtil.applyObservableDefaultSchedulers())
                            .subscribe { aliases ->
                                val ownAliases = aliases.filter { it.own }.toMutableList()
                                runOnUiThread { callback.invoke(ownAliases) }
                            })
        }
    }

}
