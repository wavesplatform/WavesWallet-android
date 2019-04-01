/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.address

import android.graphics.Bitmap
import android.support.v7.widget.AppCompatImageView
import com.wavesplatform.wallet.v2.data.model.remote.response.Alias
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface MyAddressQrView : BaseMvpView {
    fun showQRCode(qrCode: Bitmap?)
    fun afterSuccessGenerateAvatar(bitmap: Bitmap, imageView: AppCompatImageView)
    fun afterSuccessLoadAliases(ownAliases: List<Alias>)
}
