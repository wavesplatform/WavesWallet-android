package com.wavesplatform.wallet.v2.ui.welcome

import android.support.v4.view.ViewPager
import android.view.View
import kotlinx.android.synthetic.main.item_asset_details_avatar.view.*
import pers.victor.ext.dp2px

class AlphaScalePageTransformer(var minScale: Float = 0.32f, var minAlpha: Float = 0.4f) : ViewPager.PageTransformer {
    var decimal = -1f
    var size = dp2px(48)

    override fun transformPage(view: View, position: Float) {
        if (decimal == -1f && position > 0f) decimal = position - position.toInt()

        val scaleFactor = Math.max(minScale, 1 - Math.abs(position - (1 + decimal)))
        val alphaFactor = Math.max(minAlpha, 1 - Math.abs(position - (1 + decimal)))

        view.image_welcome_photo.layoutParams.height = (size * scaleFactor).toInt()
        view.image_welcome_photo.layoutParams.width = (size * scaleFactor).toInt()
        view.image_welcome_photo.alpha = alphaFactor
        view.image_welcome_photo.setTextSizeValue(view.image_welcome_photo.textSize * scaleFactor)

        view.image_welcome_photo.requestLayout()
    }
}