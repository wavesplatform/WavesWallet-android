/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.quick_action.receive.address_view

import android.graphics.Bitmap
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface ReceiveAddressView : BaseMvpView {
    fun showQRCode(it: Bitmap?)
}
