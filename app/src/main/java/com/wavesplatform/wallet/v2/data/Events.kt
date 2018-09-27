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
    class NewAssetsList(var assets: ArrayList<AssetBalance>)

}
