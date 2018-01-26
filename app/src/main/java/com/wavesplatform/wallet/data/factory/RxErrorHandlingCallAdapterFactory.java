package com.wavesplatform.wallet.data.factory;


import com.wavesplatform.wallet.data.exception.RetrofitException;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;


public class RxErrorHandlingCallAdapterFactory extends CallAdapter.Factory {
    private final RxJava2CallAdapterFactory original;

    public RxErrorHandlingCallAdapterFactory() {
        original = RxJava2CallAdapterFactory.create();
    }

    /**
     * Returns an {@link RxCallAdapterWrapper} instance
     */
    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        return new RxCallAdapterWrapper(retrofit, original.get(returnType, annotations, retrofit), returnType);
    }


    public class RxCallAdapterWrapper<R> implements CallAdapter<R, Object> {

        private final Retrofit retrofit;
        private final CallAdapter<R, ?> wrapped;
        private final Type returnType;

        public RxCallAdapterWrapper(Retrofit retrofit, CallAdapter<R, ?> wrapped, Type returnType) {
            this.retrofit = retrofit;
            this.wrapped = wrapped;
            this.returnType = returnType;
        }

        @Override
        public Type responseType() {
            return wrapped.responseType();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Observable<?> adapt(Call<R> call) {
            return convert(wrapped.adapt(call)).onErrorResumeNext(new Function<Throwable, ObservableSource>() {
                @Override
                public ObservableSource apply(Throwable throwable) throws Exception {
                    return Observable.error(asRetrofitException(throwable));
                }

            });
        }

        private Observable convert(Object o) {
            if (o instanceof Completable)
                return ((Completable) o).toObservable();
            else return (Observable) o;
        }

        public RetrofitException asRetrofitException(Throwable throwable) {
            // We had non-200 http error
            if (throwable instanceof HttpException) {
                HttpException httpException = (HttpException) throwable;
                Response response = httpException.response();

                return RetrofitException.httpError(response.raw().request().url().toString(), response, retrofit);
            }
            // A network error happened
            if (throwable instanceof TimeoutException || throwable instanceof ConnectException ||
                    throwable instanceof SocketTimeoutException || throwable instanceof UnknownHostException) {
                return RetrofitException.networkError(new IOException(throwable.getMessage(), throwable));
            }

            // We don't know what happened. We need to simply convert to an unknown error
            return RetrofitException.unexpectedError(throwable);
        }
    }
}
