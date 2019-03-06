package com.wavesplatform.wallet.v2.data.manager

import android.content.Context
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.exception.RetrofitException
import com.wavesplatform.wallet.v2.data.local.PreferencesHelper
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.util.RxEventBus
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

/**
 * Created by anonymous on 22.03.17.
 */

class ErrorManager @Inject constructor(val mRxEventBus: RxEventBus, val mPreferencesHelper: PreferencesHelper) {
    private lateinit var mActivity: BaseActivity
    private lateinit var retrySubject: PublishSubject<Events.RetryEvent>

    fun handleError(response: RetrofitException, retrySubject: PublishSubject<Events.RetryEvent>) {
        mRxEventBus.post(Events.ErrorEvent(response, retrySubject))
    }

    fun showError(context: Context, retrofitException: RetrofitException, retrySubject: PublishSubject<Events.RetryEvent>) {
        this.mActivity = context as BaseActivity
        this.retrySubject = retrySubject

        if (retrofitException.kind === RetrofitException.Kind.NETWORK || retrofitException.response!!.code() == 504) {
            handleNetworkError(retrofitException)
        } else if (retrofitException.kind === RetrofitException.Kind.HTTP) {
            handleHttpError(retrofitException)
        } else if (retrofitException.kind === RetrofitException.Kind.UNEXPECTED) {
            handleUnexpectedError(retrofitException)
        }
    }

    private fun handleNetworkError(retrofitException: RetrofitException) {
        // TODO: call method from activity or show here
//        mActivity.showNetworkError()
    }

    private fun handleHttpError(error: RetrofitException) {
        when (error.response!!.code()) {
            401 -> handleUnauthorizedException()
            500 -> {
            }
        }
    }

    fun handleUnauthorizedException() {
        // TODO: redirect to start screen and clear session (mPreferencesHelper)
    }

    fun handleUnexpectedError(exception: RetrofitException) {
        // TODO: Something went wrong...
    }
}
