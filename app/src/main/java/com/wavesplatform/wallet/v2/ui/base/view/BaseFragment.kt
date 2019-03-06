package com.wavesplatform.wallet.v2.ui.base.view

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.MvpAppCompatFragment
import com.wavesplatform.wallet.v2.util.RxEventBus
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

abstract class BaseFragment : MvpAppCompatFragment(), BaseView, BaseMvpView, HasSupportFragmentInjector {

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
    lateinit var childFragmentInjector: DispatchingAndroidInjector<Fragment>

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return childFragmentInjector
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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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
