/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.custom

import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.animation.Interpolator

import jp.wasabeef.recyclerview.animators.BaseItemAnimator

class FadeInWithoutDelayAnimator : BaseItemAnimator {

    var animationEnable = true

    constructor()

    constructor(interpolator: Interpolator) {
        mInterpolator = interpolator
    }

    override fun animateRemoveImpl(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder) {
        if (animationEnable) {
            ViewCompat.animate(holder.itemView)
                    .alpha(0f)
                    .setDuration(removeDuration)
                    .setInterpolator(mInterpolator)
                    .setListener(DefaultRemoveVpaListener(holder))
                    .start()
        }
    }

    override fun preAnimateAddImpl(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder?) {
        if (animationEnable) {
            ViewCompat.setAlpha(holder!!.itemView, 0f)
        }
    }

    override fun animateAddImpl(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder) {
        if (animationEnable) {
            ViewCompat.animate(holder.itemView)
                    .alpha(1f)
                    .setDuration(addDuration)
                    .setInterpolator(mInterpolator)
                    .setListener(DefaultAddVpaListener(holder))
                    .start()
        }
    }
}
