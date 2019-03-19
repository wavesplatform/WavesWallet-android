package com.wavesplatform.wallet.v2.ui.home.profile.addresses

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsSingle
import com.wavesplatform.wallet.v2.data.model.db.AliasDb
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.sdk.utils.RxUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import pyxis.uzuki.live.richutilskt.utils.runAsync
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import javax.inject.Inject

@InjectViewState
class AddressesAndKeysPresenter @Inject constructor() : BasePresenter<AddressesAndKeysView>() {

    fun loadAliases() {
        runAsync {
            addSubscription(
                    queryAllAsSingle<AliasDb>().toObservable()
                            .observeOn(AndroidSchedulers.mainThread())
                            .map { aliases ->
                                val ownAliases = aliases.filter { it.own }
                                runOnUiThread { viewState.afterSuccessLoadAliases(
                                        AliasDb.convertFromDb(ownAliases)) }
                            }
                            .observeOn(Schedulers.io())
                            .flatMap {
                                apiDataManager.loadAliases()
                            }
                            .compose(RxUtil.applyObservableDefaultSchedulers())
                            .subscribe {
                                runOnUiThread { viewState.afterSuccessLoadAliases(it) }
                            })
        }
    }
}
