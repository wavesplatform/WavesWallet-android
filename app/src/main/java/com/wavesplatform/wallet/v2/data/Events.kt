/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data

import com.wavesplatform.sdk.net.NetworkException
import io.reactivex.subjects.PublishSubject


class Events {
    class ErrorEvent(val retrofitException: NetworkException, val retrySubject: PublishSubject<Events.RetryEvent>)

    class RetryEvent
    class NeedUpdateHistoryScreen
    class StopUpdateHistoryScreen
    class UpdateListOfActiveTransaction(var position: Int)
    class ScrollToTopEvent(var position: Int)

    class SpamFilterStateChanged
    class SpamFilterUrlChanged(var updateTransaction: Boolean = false)
    class DexOrderButtonClickEvent(var buy: Boolean)
    class OrderBookTabClickEvent
    class NeedUpdateMyOrdersScreen
    class UpdateMarketAfterChangeChartTimeFrame(var id: String?, var timeServer: Int)
    class UpdateButtonsPrice(var askPrice: Long?, var bidPrice: Long?)

    class UpdateAssetsBalance
    object UpdateAssetsDetailsHistory
}
