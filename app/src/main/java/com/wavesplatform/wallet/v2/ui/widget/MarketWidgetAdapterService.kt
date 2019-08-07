/*
 * Created by Eduard Zaydel on 22/7/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget

import android.content.Intent
import android.widget.RemoteViewsService
import com.wavesplatform.wallet.v2.ui.widget.model.MarketWidgetActiveMarket
import com.wavesplatform.wallet.v2.ui.widget.model.MarketWidgetActiveStore
import dagger.android.AndroidInjection
import javax.inject.Inject

class MarketWidgetAdapterService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return MarketWidgetAdapterFactory(applicationContext, intent)
    }

}