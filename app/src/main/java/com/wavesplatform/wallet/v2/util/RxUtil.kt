package com.wavesplatform.wallet.v2.util

import io.reactivex.FlowableTransformer
import io.reactivex.ObservableTransformer
import io.reactivex.SingleTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

object RxUtil {

    fun unsubscribe(subscription: Disposable?) {
        if (subscription != null && !subscription.isDisposed) {
            subscription.dispose()
        }
    }

    fun <T> applyObservableDefaultSchedulers(): ObservableTransformer<T, T> { // compose
        return ObservableTransformer { observable ->
            observable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun <T> applySingleDefaultSchedulers(): SingleTransformer<T, T> { // compose
        return SingleTransformer { observable ->
            observable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun <T> applyFlowableDefaultSchedulers(): FlowableTransformer<T, T> { // compose
        return FlowableTransformer { observable ->
            observable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }
}
