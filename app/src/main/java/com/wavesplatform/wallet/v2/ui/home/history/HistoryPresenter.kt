package com.wavesplatform.wallet.v2.ui.home.history

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class HistoryPresenter @Inject constructor() : BasePresenter<HistoryView>() {
    var hideShadow: Boolean = true
}
