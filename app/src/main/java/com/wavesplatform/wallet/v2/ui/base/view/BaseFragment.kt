/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.base.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.wallet.v2.util.RxEventBus
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import moxy.MvpAppCompatFragment
import timber.log.Timber
import javax.inject.Inject

abstract class BaseFragment : MvpAppCompatFragment(), BaseView, BaseMvpView, HasAndroidInjector {

    var eventSubscriptions: CompositeDisposable = CompositeDisposable()

    val supportActionBar: ActionBar?
        get() = baseActivity.supportActionBar

    val baseActivity: BaseActivity
        get() = activity as BaseActivity

    val toolbar: Toolbar
        get() = baseActivity.toolbar

    @Inject
    lateinit var rxEventBus: RxEventBus

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> {
        return androidInjector
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        Timber.tag(javaClass.simpleName)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(configLayoutRes(), container, false)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        eventSubscriptions.add(ReactiveNetwork
                .observeInternetConnectivity()
                .onErrorResumeNext(Observable.empty())
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ connected ->
                    onNetworkConnectionChanged(connected)
                }, {
                    it.printStackTrace()
                }))
        onViewReady(savedInstanceState)
    }

    protected abstract fun onViewReady(savedInstanceState: Bundle?)

    override fun onDestroyView() {
        eventSubscriptions.clear()
        super.onDestroyView()
    }

    fun setTitle(title: Int) {
        baseActivity.title = getString(title)
    }

    override fun showNetworkError() {
        baseActivity.showNetworkError()
    }

    override fun showProgressBar(isShowProgress: Boolean) {
        baseActivity.showProgressBar(isShowProgress)
    }

    override fun onBackPressed() {
        baseActivity.onBackPressed()
    }

    override fun onNetworkConnectionChanged(networkConnected: Boolean) {
        // nothing
    }
}
