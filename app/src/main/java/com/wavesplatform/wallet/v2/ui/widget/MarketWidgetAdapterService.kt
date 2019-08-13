/*
 * Created by Eduard Zaydel on 22/7/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget

import android.content.Intent
import android.widget.RemoteViewsService

class MarketWidgetAdapterService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return MarketWidgetAdapterFactory(applicationContext, intent)
    }

}