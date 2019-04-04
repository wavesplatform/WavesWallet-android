package com.wavesplatform.sdk.net

import io.reactivex.Completable
import io.reactivex.Observable
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

class CallAdapterFactory(private val errorListener: OnErrorListener) : CallAdapter.Factory() {

    private val original: RxJava2CallAdapterFactory = RxJava2CallAdapterFactory.create()

    /**
     * Returns an [RxCallAdapterWrapper] instance
     */
    override fun get(returnType: Type, annotations: Array<Annotation>,
                     retrofit: Retrofit): CallAdapter<*, *> {
        return RxCallAdapterWrapper(retrofit, original.get(returnType, annotations, retrofit)
                as CallAdapter<out Any, *>, returnType)
    }

    inner class RxCallAdapterWrapper<R>(
            private val retrofit: Retrofit,
            private val wrapped: CallAdapter<R, *>,
            private val returnType: Type
    ) : CallAdapter<R, Any> {

        override fun responseType(): Type {
            return wrapped.responseType()
        }

        override fun adapt(call: Call<R>): Observable<*> {
            return convert(wrapped.adapt(call)).onErrorResumeNext { throwable: Throwable ->
                Observable.error(handleErrorToShow(throwable))
            }
        }

        private fun handleErrorToShow(throwable: Throwable): RetrofitException {
            val retrofitException = asRetrofitException(throwable)
            errorListener.onError(retrofitException)
            return retrofitException
        }

        private fun convert(o: Any): Observable<*> {
            return if (o is Completable) {
                o.toObservable<Any>()
            } else {
                o as Observable<*>
            }
        }

        private fun asRetrofitException(throwable: Throwable): RetrofitException {

            // Non-200 http error
            if (throwable is HttpException) {
                val response = throwable.response()
                return RetrofitException.httpError(response.raw().request()
                        .url().toString(), response, retrofit)
            }

            if (throwable is TimeoutException
                    || throwable is ConnectException
                    || throwable is SocketTimeoutException
                    || throwable is UnknownHostException) {
                return RetrofitException.networkError(IOException(throwable.message, throwable))
            }

            return RetrofitException.unexpectedError(throwable)
        }
    }
}