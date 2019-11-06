/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.base.view

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.NonNull
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.widget.FrameLayout
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.wallet.v2.data.local.PreferencesHelper
import com.wavesplatform.wallet.v2.util.RxEventBus
import com.wavesplatform.wallet.v2.util.getToolBarHeight
import com.wavesplatform.sdk.utils.notNull
import com.wavesplatform.wallet.R
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import pers.victor.ext.getStatusBarHeight
import pers.victor.ext.screenHeight
import pers.victor.ext.setHeight
import javax.inject.Inject

open class BaseBottomSheetDialogFragment : BottomSheetDialogFragment(), BaseMvpView {

    open var fullScreenHeightEnable = false
    var extraTopMargin = 0

    val baseActivity: BaseActivity
        get() = activity as BaseActivity
    var eventSubscriptions: CompositeDisposable = CompositeDisposable()

    @Inject
    lateinit var preferencesHelper: PreferencesHelper
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

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog

            val bottomSheet = d.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet.notNull {
                if ((it.height > screenHeight - getStatusBarHeight() - it.context.getToolBarHeight()) || fullScreenHeightEnable) {
                    it.setHeight(screenHeight - it.context.getToolBarHeight() - extraTopMargin)
                }
                val bottomSheetBehavior = BottomSheetBehavior.from(it)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                bottomSheetBehavior.peekHeight = it.height
            }
        }

        // Do something with your dialog like setContentView() or whatever
        return dialog
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
}