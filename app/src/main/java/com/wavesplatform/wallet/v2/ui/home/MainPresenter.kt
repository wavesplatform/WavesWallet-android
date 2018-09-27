package com.wavesplatform.wallet.v2.ui.home

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import javax.inject.Inject

@InjectViewState
class MainPresenter @Inject constructor() : BasePresenter<MainView>() {
    var checkedAboutFundsOnDevice = false
    var checkedAboutBackup = false
    var checkedAboutTerms = false

    fun isAllCheckedToStart(): Boolean {
        return checkedAboutBackup && checkedAboutFundsOnDevice && checkedAboutTerms
    }

}
