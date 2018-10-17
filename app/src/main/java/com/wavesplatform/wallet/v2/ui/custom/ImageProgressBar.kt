package com.wavesplatform.wallet.v2.ui.custom

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.LinearLayout
import com.wavesplatform.wallet.R
import pers.victor.ext.app
import pers.victor.ext.dp2px
import pers.victor.ext.findColorStateList
import pers.victor.ext.findDrawable
import android.view.Gravity


class ImageProgressBar : LinearLayout {

    private var imageProgress: AppCompatImageView = AppCompatImageView(app)
    private var textProgress: AppCompatTextView = AppCompatTextView(app)

    private var mContext: Context = app
    private var animator: ObjectAnimator? = null
    private var stopAnimation: Boolean = false

    private var progressTextColorResource: ColorStateList? = findColorStateList(R.color.disabled500)
    private var progressImageResource: Drawable? = findDrawable(R.drawable.ic_loader_24_submit_400)
    private var progressTextResource: String = app.getString(R.string.common_load_more_status)
    private var progressDurationResource: Int = DEFAULT_DURATION

    companion object {
        const val DEFAULT_DURATION = 750
    }

    constructor(context: Context) : super(context) {
        mContext = context
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        mContext = context
        handelAttributes(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        mContext = context
        handelAttributes(attrs)
    }

    private fun handelAttributes(attrs: AttributeSet) {
        orientation = LinearLayout.VERTICAL
        val typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.ImageProgressBar, 0, 0)

        // load attributes
        try {
            progressImageResource = typedArray.getDrawable(R.styleable.ImageProgressBar_progress_image)
            progressDurationResource = typedArray.getInt(R.styleable.ImageProgressBar_progress_duration, DEFAULT_DURATION)
            progressTextResource = typedArray.getString(R.styleable.ImageProgressBar_progress_text)
            progressTextColorResource = typedArray.getColorStateList(R.styleable.ImageProgressBar_progress_text_color)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        typedArray.recycle()

        configureProgressAndStart()
    }

    private fun configureProgressAndStart() {
        // configure gravity
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT)
        params.gravity = Gravity.CENTER_HORIZONTAL

        textProgress.layoutParams = params
        imageProgress.layoutParams = params

        // configure progress image and text from attributes
        textProgress.text = progressTextResource
        textProgress.textSize = 12f
        textProgress.setTextColor(progressTextColorResource)
        textProgress.setPadding(0, dp2px(14), 0, 0)
        imageProgress.setImageDrawable(progressImageResource)

        //  add views to layout
        this.addView(imageProgress)
        this.addView(textProgress)

        // configure animation and start
        animator = ObjectAnimator.ofFloat(imageProgress,
                "rotation", 0f, 360f)
        animator?.duration = progressDurationResource.toLong()
        animator?.repeatCount = ObjectAnimator.INFINITE
        animator?.repeatMode = ObjectAnimator.RESTART
        animator?.interpolator = AccelerateInterpolator()
        animator?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {
                if (!stopAnimation) {
                    animator?.start()
                }
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        })
        animator?.start()
    }

    fun hide() {
        visibility = View.GONE
        stopAnimation = true
        animator?.end()
        animator?.cancel()
    }

    fun show() {
        visibility = View.VISIBLE
        stopAnimation = false
        animator?.start()
    }
}