package com.wavesplatform.wallet.v2.ui.base.presenter

import com.arellomobile.mvp.MvpPresenter
import com.arellomobile.mvp.MvpView
import com.wavesplatform.wallet.v1.util.AppUtil
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.local.PreferencesHelper
import com.wavesplatform.wallet.v2.data.manager.ApiDataManager
import com.wavesplatform.wallet.v2.data.manager.NodeDataManager
import com.wavesplatform.wallet.v2.data.manager.SpamDataManager
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject

/**
 * Base class that implements the Presenter interface and provides a base implementation for
 * attachView() and detachView(). It also handles keeping a reference to the mvpView that
 * can be accessed from the children classes by calling getMvpView().
 */
open class BasePresenter<T : MvpView> @Inject constructor(): MvpPresenter<T>(){

    @Inject lateinit var preferenceHelper: PreferencesHelper
    @Inject lateinit var nodeDataManager: NodeDataManager
    @Inject lateinit var apiDataManager: ApiDataManager
    @Inject lateinit var spamDataManager: SpamDataManager
    @Inject lateinit var prefsUtil: PrefsUtil
    @Inject lateinit var appUtil: AppUtil

    private val mCompositeDisposable = CompositeDisposable()

    override fun onDestroy() {
        super.onDestroy()
        mCompositeDisposable.clear()
    }

    fun addSubscription(subscription: Disposable) {
        mCompositeDisposable.add(subscription)
    }
}

