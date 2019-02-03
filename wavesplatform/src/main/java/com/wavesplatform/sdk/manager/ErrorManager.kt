package com.wavesplatform.sdk.manager

import android.content.Context
import android.provider.CalendarContract
import com.wavesplatform.sdk.exception.RetrofitException
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

/**
 * Created by anonymous on 22.03.17.
 */

class ErrorManager @Inject constructor(val mRxEventBus: RxEventBus, val mPreferencesHelper: PreferencesHelper) {
    private lateinit var mActivity: BaseActivity
    private lateinit var retrySubject: PublishSubject<CalendarContract.Events.RetryEvent>

    fun handleError(response: RetrofitException, retrySubject: PublishSubject<Events.RetryEvent>) {
        mRxEventBus.post(Events.ErrorEvent(response, retrySubject))
    }

    fun showError(context: Context, retrofitException: RetrofitException, retrySubject: PublishSubject<CalendarContract.Events.RetryEvent>) {
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
