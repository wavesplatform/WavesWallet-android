/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.base.view

import android.os.Bundle
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.local.PreferencesHelper
import com.wavesplatform.wallet.v2.util.RxEventBus
import com.wavesplatform.wallet.v2.util.getToolBarHeight
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import pers.victor.ext.findColor
import pers.victor.ext.screenHeight
import javax.inject.Inject

open class BaseSuperBottomSheetDialogFragment : SuperBottomSheetFragment(), BaseMvpView {

    val baseActivity: BaseActivity
        get() = activity as BaseActivity
    var eventSubscriptions: CompositeDisposable = CompositeDisposable()

    @Inject
    lateinit var preferencesHelper: PreferencesHelper

    @Inject
    lateinit var prefsUtil: PrefsUtil

    @Inject
    lateinit var rxEventBus: RxEventBus

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)

        eventSubscriptions.add(ReactiveNetwork
                .observeInternetConnectivity()
                .distinctUntilChanged()
                .onErrorResumeNext(Observable.empty())
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ connected ->
                    onNetworkConnectionChanged(connected)
                }, {
                    it.printStackTrace()
                }))
    }

    open fun onNetworkConnectionChanged(networkConnected: Boolean) {
        // nothing
    }

    override fun showNetworkError() {
        baseActivity.showNetworkError()
    }

    override fun showProgressBar(isShowProgress: Boolean) {
        baseActivity.showProgressBar(isShowProgress)
    }

    override fun onDestroyView() {
        eventSubscriptions.clear()
        super.onDestroyView()
    }

    override fun getPeekHeight(): Int {
        return screenHeight - requireActivity().getToolBarHeight()
    }

    override fun getStatusBarColor(): Int {
        return findColor(R.color.white)
    }

    override fun animateStatusBar(): Boolean {
        return true
    }

    override fun animateCornerRadius(): Boolean {
        return false
    }

    override fun getCornerRadius(): Float {
        return 0f
    }
}