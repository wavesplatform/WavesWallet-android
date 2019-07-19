/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.base.presenter

import com.arellomobile.mvp.MvpPresenter
import com.arellomobile.mvp.MvpView
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.data.local.PreferencesHelper
import com.wavesplatform.wallet.v2.data.manager.DataServiceManager
import com.wavesplatform.wallet.v2.data.manager.GithubServiceManager
import com.wavesplatform.wallet.v2.data.manager.MatcherServiceManager
import com.wavesplatform.wallet.v2.data.manager.NodeServiceManager
import com.wavesplatform.wallet.v2.util.PrefsUtil
import com.wavesplatform.wallet.v2.util.RxEventBus
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject

/**
 * Base class that implements the Presenter interface and provides a base implementation for
 * attachView() and detachView(). It also handles keeping a reference to the mvpView that
 * can be accessed from the children classes by calling getMvpView().
 */
open class BasePresenter<T : MvpView> @Inject constructor() : MvpPresenter<T>() {

    @Inject
    lateinit var preferenceHelper: PreferencesHelper
    @Inject
    lateinit var nodeServiceManager: NodeServiceManager
    @Inject
    lateinit var dataServiceManager: DataServiceManager
    @Inject
    lateinit var matcherServiceManager: MatcherServiceManager
    @Inject
    lateinit var githubServiceManager: GithubServiceManager
    @Inject
    lateinit var prefsUtil: PrefsUtil
    @Inject
    lateinit var rxEventBus: RxEventBus

    private val mCompositeDisposable = CompositeDisposable()

    override fun onDestroy() {
        mCompositeDisposable.clear()
        super.onDestroy()
    }

    open fun addSubscription(subscription: Disposable) {
        mCompositeDisposable.add(subscription)
    }

    fun getWavesAddress(): String {
        return App.getAccessManager().getWallet()?.address ?: ""
    }
}
