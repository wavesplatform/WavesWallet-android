package com.wavesplatform.wallet.v2.ui.auth.new_account

import android.graphics.Bitmap
import android.support.v7.widget.AppCompatImageView
import android.widget.ImageView
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface NewAccountView : BaseMvpView {
    fun afterSuccessGenerateAvatar(seed: String, bitmap: Bitmap, imageView: AppCompatImageView)
}
