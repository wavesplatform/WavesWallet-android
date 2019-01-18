package com.wavesplatform.wallet.v2.ui.welcome

import android.support.v4.view.ViewPager
import android.view.View
import kotlinx.android.synthetic.main.item_welcome.view.*


class AlphaScalePageTransformer(var minScale: Float = 0.32f, var minAlpha: Float = 0.4f) : ViewPager.PageTransformer {
    var decimal = -1f

    override fun transformPage(view: View, position: Float) {
        if (decimal == -1f && position > 0f) decimal = position - position.toInt()

        val scaleFactor = Math.max(minScale, 1 - Math.abs(position - (1 + decimal)))
        val alphaFactor = Math.max(minAlpha, 1 - Math.abs(position - (1 + decimal)))

        view.image_welcome_photo.scaleX = scaleFactor
        view.image_welcome_photo.scaleY = scaleFactor
        view.image_welcome_photo.alpha = alphaFactor
    }
}