package com.wavesplatform.wallet.v2.data

import com.wavesplatform.wallet.v2.data.exception.RetrofitException
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import io.reactivex.subjects.PublishSubject

/**
 * Created by anonymous on 25.11.16.
 */

class Events {

    class ErrorEvent(val retrofitException: RetrofitException, val retrySubject: PublishSubject<Events.RetryEvent>)

    class RetryEvent
    class NeedUpdateHistoryScreen
    class ScrollToTopEvent(var position: Int)

    class SpamFilterStateChanged
    class SpamFilterUrlChanged(var updateTransaction: Boolean = false)
    class DexOrderButtonClickEvent(var buy: Boolean)
    class NeedUpdateMyOrdersScreen
}
