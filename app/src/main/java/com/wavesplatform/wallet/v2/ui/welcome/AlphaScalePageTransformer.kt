/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.welcome

import android.support.v4.view.ViewPager
import android.view.View
import kotlinx.android.synthetic.main.item_asset_details_avatar.view.*
import pers.victor.ext.dp
import pers.victor.ext.dp2px
import kotlin.math.abs
import kotlin.math.max

class AlphaScalePageTransformer(var minScale: Float = 0.32f, var minAlpha: Float = 0.4f) : ViewPager.PageTransformer {
    private var decimal = -1f
    private var size = 48.dp

    override fun transformPage(view: View, position: Float) {
        if (decimal == -1f && position > 0f) decimal = position - position.toInt()

        val scaleFactor = max(minScale, 1 - abs(position - (1 + decimal)))
        val alphaFactor = max(minAlpha, 1 - abs(position - (1 + decimal)))

        view.image_welcome_photo.layoutParams.height = (size * scaleFactor).toInt()
        view.image_welcome_photo.layoutParams.width = (size * scaleFactor).toInt()
        view.image_welcome_photo.alpha = alphaFactor
        view.image_welcome_photo.setTextSizeValue(view.image_welcome_photo.textSize * scaleFactor)

        view.image_welcome_photo.requestLayout()
    }
}