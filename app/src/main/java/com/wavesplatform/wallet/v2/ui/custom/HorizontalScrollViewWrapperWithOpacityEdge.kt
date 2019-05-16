/*
 * Created by Eduard Zaydel on 16/5/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.custom

import android.content.Context
import android.graphics.Bitmap
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.util.afterMeasured
import kotlinx.android.synthetic.main.content_top_info_alert.view.*
import kotlinx.android.synthetic.main.fragment_history_bottom_sheet_bottom_btns.view.*
import pers.victor.ext.*

class HorizontalScrollViewWrapperWithOpacityEdge : RelativeLayout {

    private val leftEdge: View by lazy { View(context) }
    private val rightEdge: View  by lazy { View(context) }

    constructor(context: Context) : super(context) {
        inflate()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        inflate()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        inflate()
    }

    private fun inflate() {
        this.afterMeasured {
            val horizontalScrollView = children.firstOrNull()
            horizontalScrollView?.let {
                // setup left opacity edge
                leftEdge.apply {
                    val leftEdgeLayoutParams = LayoutParams(48.dp, 80.dp)
                    leftEdgeLayoutParams.addRule(ALIGN_BOTTOM, horizontalScrollView.id)
                    layoutParams = leftEdgeLayoutParams
                    setBackgroundResource(R.drawable.bg_opacity_scroll_left_edge)
                }

                // setup right opacity edge
                rightEdge.apply {
                    val rightEdgeLayoutParams = LayoutParams(48.dp, 80.dp)
                    rightEdgeLayoutParams.addRule(ALIGN_BOTTOM, horizontalScrollView.id)
                    rightEdgeLayoutParams.addRule(ALIGN_PARENT_RIGHT)
                    layoutParams = rightEdgeLayoutParams
                    setBackgroundResource(R.drawable.bg_opacity_scroll_right_edge)
                }

                // handle scroll and change visibility of edge opacity
                horizontalScrollView.viewTreeObserver?.addOnScrollChangedListener {
                    checkScrollStates(horizontalScrollView)
                }

                // first state of edge opacity after measured
                horizontalScrollView.afterMeasured {
                    checkScrollStates(horizontalScrollView)
                }

                // add views to parent
                addView(leftEdge)
                addView(rightEdge)
            }
        }
    }

    private fun checkScrollStates(horizontalScrollView: View) {
        if (horizontalScrollView.canScrollHorizontally(LEFT_EDGE)) {
            // can scroll to left, show opacity block
            leftEdge.visiable()
        } else {
            // end of left scroll, need hide opacity block
            leftEdge.gone()
        }

        if (horizontalScrollView.canScrollHorizontally(RIGHT_EDGE)) {
            // can scroll to right, show opacity block
            rightEdge.visiable()
        } else {
            // end of right scroll, need hide opacity block
            rightEdge.gone()
        }
    }

    companion object {
        const val LEFT_EDGE = -1
        const val RIGHT_EDGE = 1
    }
}