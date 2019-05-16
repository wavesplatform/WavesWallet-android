/*
 * Created by Eduard Zaydel on 16/5/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.custom

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.HorizontalScrollVisibilityState
import com.wavesplatform.wallet.v2.util.afterMeasured
import com.wavesplatform.wallet.v2.util.animateInvisible
import com.wavesplatform.wallet.v2.util.animateVisible
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import pers.victor.ext.children
import pers.victor.ext.dp
import pers.victor.ext.findDrawable


class HorizontalScrollViewWrapperWithOpacityEdge : RelativeLayout {

    private val leftEdge: View by lazy { View(context) }
    private val rightEdge: View  by lazy { View(context) }

    private var opacityEdgeBlockWidth: Int = DEFAULT_WIDTH
    private var opacityEdgeBlockHeight: Int = DEFAULT_HEIGHT

    private var leftEdgeBlockBackground: Drawable? = findDrawable(R.drawable.bg_opacity_scroll_left_edge_white)
    private var rightEdgeBlockBackground: Drawable? = findDrawable(R.drawable.bg_opacity_scroll_right_edge_white)

    private val subscriptions: CompositeDisposable = CompositeDisposable()

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        getAttributes(attrs)

        this.afterMeasured {
            val horizontalScrollView = children.firstOrNull()
            horizontalScrollView?.let {
                // setup left opacity edge
                leftEdge.apply {
                    val leftEdgeLayoutParams = LayoutParams(opacityEdgeBlockWidth, opacityEdgeBlockHeight)
                    leftEdgeLayoutParams.addRule(ALIGN_BOTTOM, horizontalScrollView.id)
                    layoutParams = leftEdgeLayoutParams
                    background = leftEdgeBlockBackground
                    isClickable = false
                }

                // setup right opacity edge
                rightEdge.apply {
                    val rightEdgeLayoutParams = LayoutParams(opacityEdgeBlockWidth, opacityEdgeBlockHeight)
                    rightEdgeLayoutParams.addRule(ALIGN_BOTTOM, horizontalScrollView.id)
                    rightEdgeLayoutParams.addRule(ALIGN_PARENT_RIGHT)
                    layoutParams = rightEdgeLayoutParams
                    background = rightEdgeBlockBackground
                    isClickable = false
                }

                subscriptions.add(Observable.create(ObservableOnSubscribe<HorizontalScrollVisibilityState> { subscriber ->
                    // handle scroll and change visibility of edge opacity
                    horizontalScrollView.viewTreeObserver?.addOnScrollChangedListener {
                        checkScrollStates(horizontalScrollView, subscriber)
                    }

                    // first state of edge opacity after measured
                    horizontalScrollView.afterMeasured {
                        checkScrollStates(horizontalScrollView, subscriber)
                    }
                })
                        .distinctUntilChanged()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { visibilityState ->
                            if (visibilityState.needShowLeftEdge) {
                                // can scroll to left, show opacity block
                                leftEdge.animateVisible()
                            } else {
                                // end of left scroll, need hide opacity block
                                leftEdge.animateInvisible()
                            }

                            if (visibilityState.needShowRightEdge) {
                                // can scroll to right, show opacity block
                                rightEdge.animateVisible()
                            } else {
                                // end of right scroll, need hide opacity block
                                rightEdge.animateInvisible()
                            }
                        })

                // add views to parent
                addView(leftEdge)
                addView(rightEdge)
            }
        }
    }

    private fun getAttributes(attrs: AttributeSet?) {
        // obtain passed attributes of view
        val a = context.theme.obtainStyledAttributes(
                attrs, R.styleable.HorizontalScrollViewWrapperWithOpacityEdge, 0, 0)
        try {
            opacityEdgeBlockWidth = a.getDimensionPixelSize(R.styleable.HorizontalScrollViewWrapperWithOpacityEdge_edge_block_width, DEFAULT_WIDTH)
            opacityEdgeBlockHeight = a.getDimensionPixelSize(R.styleable.HorizontalScrollViewWrapperWithOpacityEdge_edge_block_height, DEFAULT_HEIGHT)
            leftEdgeBlockBackground = a.getDrawable(R.styleable.HorizontalScrollViewWrapperWithOpacityEdge_left_edge_block_background)
            rightEdgeBlockBackground = a.getDrawable(R.styleable.HorizontalScrollViewWrapperWithOpacityEdge_right_edge_block_background)
        } finally {
            a.recycle()
        }
    }

    private fun checkScrollStates(horizontalScrollView: View, subscriber: ObservableEmitter<HorizontalScrollVisibilityState>?) {
        subscriber?.onNext(
                HorizontalScrollVisibilityState(horizontalScrollView.canScrollHorizontally(LEFT_EDGE),
                        horizontalScrollView.canScrollHorizontally(RIGHT_EDGE))
        )
    }

    override fun onDetachedFromWindow() {
        subscriptions.clear()
        leftEdge.clearAnimation()
        rightEdge.clearAnimation()
        super.onDetachedFromWindow()
    }

    companion object {
        const val LEFT_EDGE = -1
        const val RIGHT_EDGE = 1
        val DEFAULT_WIDTH = 48.dp
        val DEFAULT_HEIGHT = 80.dp
    }
}