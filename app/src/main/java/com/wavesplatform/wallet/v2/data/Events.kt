package com.wavesplatform.wallet.v2.data

import com.wavesplatform.wallet.v2.data.exception.RetrofitException
import io.reactivex.subjects.PublishSubject

/**
 * Created by anonymous on 25.11.16.
 */

class Events {

    class ErrorEvent(val retrofitException: RetrofitException, val retrySubject: PublishSubject<Events.RetryEvent>)

    class RetryEvent
    class NeedUpdateHistoryScreen
    class StopUpdateHistoryScreen
    class UpdateListOfActiveTransaction(var position: Int)
    class ScrollToTopEvent(var position: Int)

    class SpamFilterStateChanged
    class SpamFilterUrlChanged(var updateTransaction: Boolean = false)
    class DexOrderButtonClickEvent(var buy: Boolean)
    class NeedUpdateMyOrdersScreen
    class UpdateMarketAfterChangeChartTimeFrame(var id: String?, var timeServer: Int)
    class UpdateButtonsPrice(var askPrice: Long?, var bidPrice: Long?)
}
