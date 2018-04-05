package com.wavesplatform.wallet.v2.data

import com.wavesplatform.wallet.v2.data.exception.RetrofitException
import io.reactivex.subjects.PublishSubject

/**
 * Created by anonymous on 25.11.16.
 */

class Events {

    class ErrorEvent(val retrofitException: RetrofitException, val retrySubject: PublishSubject<Events.RetryEvent>)

    class RetryEvent

}
