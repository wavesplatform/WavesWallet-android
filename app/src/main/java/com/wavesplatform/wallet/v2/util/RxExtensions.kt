/*
 * Created by Eduard Zaydel on 28/6/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.util

import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

fun <T> Observable<T>.executeInBackground(scheduler: Scheduler = Schedulers.io()): Observable<T> {
    return this.subscribeOn(scheduler)
            .observeOn(AndroidSchedulers.mainThread())
}

fun <T> Flowable<T>.executeInBackground(scheduler: Scheduler = Schedulers.io()): Flowable<T> {
    return this.subscribeOn(scheduler)
            .observeOn(AndroidSchedulers.mainThread())
}

fun <T> Single<T>.executeInBackground(scheduler: Scheduler = Schedulers.io()): Single<T> {
    return this.subscribeOn(scheduler)
            .observeOn(AndroidSchedulers.mainThread())
}