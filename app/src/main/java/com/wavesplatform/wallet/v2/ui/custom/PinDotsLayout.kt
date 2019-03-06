package com.wavesplatform.wallet.v2.ui.custom

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout

import com.wavesplatform.wallet.R
import kotlinx.android.synthetic.main.pass_code_dots_layout.view.*
import pers.victor.ext.children

class PinDotsLayout : LinearLayout {
    private var listener: OnDotsFilledListener? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
    }

    private fun init() {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.BOTTOM

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.pass_code_dots_layout, this, true)
    }

    fun fillDot(position: Int) {
        val dot = linear_dots.getChildAt(position)
        if (dot != null) {
            (dot as AppCompatImageView).setImageResource(R.drawable.shape_circle_dot_submit400)
            listener?.onDotFilled(position)
        }
    }

    fun emptyDot(position: Int) {
        val dot = linear_dots.getChildAt(position)
        if (dot != null) {
            (dot as AppCompatImageView).setImageResource(R.drawable.shape_circle_dot_basic100)
            listener?.onDotFilled(position)
        }
    }

    fun clearDots() {
        linear_dots.children.forEach {
            (it as AppCompatImageView).setImageResource(R.drawable.shape_circle_dot_basic100)
        }
        listener?.onDotsCleared()
    }

    fun passCodesNotMatches() {
        linear_dots.children.forEach {
            (it as AppCompatImageView).setImageResource(R.drawable.shape_circle_dot_error400)
        }
    }

    fun passCodeRemoved(position: Int) {
        emptyDot(position)
    }

    interface OnDotsFilledListener {
        fun onDotFilled(position: Int)

        fun onDotsCleared()
    }
}
