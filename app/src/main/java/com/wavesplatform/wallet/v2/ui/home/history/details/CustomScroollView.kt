package com.wavesplatform.wallet.v2.ui.home.history.details

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ScrollView

class CustomScroollView : ScrollView {
    var view: View? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {}

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        val action = ev?.action
        when (action) {
            MotionEvent.ACTION_DOWN ->
                // Disallow ScrollView to intercept touch events.
                view?.parent?.requestDisallowInterceptTouchEvent(true)

            MotionEvent.ACTION_UP ->
                // Allow ScrollView to intercept touch events.
                view?.parent?.requestDisallowInterceptTouchEvent(false)
        }

        // Handle MapView's touch events.
        super.onTouchEvent(ev)
        return true
    }
}
