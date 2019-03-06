package com.wavesplatform.wallet.v2.data.factory

import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.exception.RetrofitException
import com.wavesplatform.wallet.v2.data.helpers.SentryHelper
import com.wavesplatform.wallet.v2.data.manager.ErrorManager
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.io.IOException
import java.lang.reflect.Type
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

class RxErrorHandlingCallAdapterFactory(private val mErrorManager: ErrorManager) : CallAdapter.Factory() {
    private val original: RxJava2CallAdapterFactory = RxJava2CallAdapterFactory.create()

    /**
     * Returns an [RxCallAdapterWrapper] instance
     */
    override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *> {
        return RxCallAdapterWrapper(retrofit, original.get(returnType, annotations, retrofit) as CallAdapter<out Any, *>, returnType)
    }

    inner class RxCallAdapterWrapper<R>(private val retrofit: Retrofit, private val wrapped: CallAdapter<R, *>, private val returnType: Type) : CallAdapter<R, Any> {

        override fun responseType(): Type {
            return wrapped.responseType()
        }

        override fun adapt(call: Call<R>): Observable<*> {
            val retrySubject = PublishSubject.create<Events.RetryEvent>()
            var observable = convert(wrapped.adapt(call))
                    .onErrorResumeNext { t: Throwable -> Observable.error(handleErrorToShow(t, retrySubject)) }
            return observable
        }

        private fun handleErrorToShow(throwable: Throwable, retrySubject: PublishSubject<Events.RetryEvent>): RetrofitException {
            val retrofitException = asRetrofitException(throwable)
            mErrorManager.handleError(retrofitException, retrySubject)
            SentryHelper.logException(retrofitException)
            return retrofitException
        }

        private fun convert(o: Any): Observable<*> {
            return if (o is Completable)
                o.toObservable<Any>()
            else
                o as Observable<*>
        }

        fun asRetrofitException(throwable: Throwable): RetrofitException {
            // We had non-200 http error
            if (throwable is HttpException) {
                val response = throwable.response()

                return RetrofitException.httpError(response.raw().request().url().toString(), response, retrofit)
            }
            // A network error happened
            if (throwable is TimeoutException || throwable is ConnectException ||
                    throwable is SocketTimeoutException || throwable is UnknownHostException) {
                return RetrofitException.networkError(IOException(throwable.message, throwable))
            }

            // We don't know what happened. We need to simply convert to an unknown error
            return RetrofitException.unexpectedError(throwable)
        }
    }
}
