package com.wavesplatform.wallet.v2.ui.custom

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import com.wavesplatform.wallet.R
import timber.log.Timber

class AvatarView : android.support.v7.widget.AppCompatImageView {

    private var defaultBorderColor: Int = 0
    private var defaultBorderWidth: Int = 0

    private var borderColor: Int = 0
    private var borderWidth: Int = 0
    private var textSizePercentage: Int = 0

    private var viewSize: Int = 0
    private var customdrawable: Drawable? = null

    internal var circleRadius: Int = 0
    internal var circleCenterXValue: Int = 0
    internal var circleCenterYValue: Int = 0

    private val borderPaint = Paint()
    private val mainPaint = Paint()
    private var circleRect: Rect? = null


    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    public override fun onDraw(canvas: Canvas) {
        saveBasicValues(canvas)

        if (viewSize == 0) {
            return
        }

        val bitmap = cutIntoCircle(drawableToBitmap(customdrawable)) ?: return

        canvas.translate(circleCenterXValue.toFloat(), circleCenterYValue.toFloat())

        //Draw Border
        canvas.drawCircle((circleRadius + borderWidth).toFloat(), (circleRadius + borderWidth).toFloat(), (circleRadius + borderWidth).toFloat(), borderPaint)

        canvas.drawBitmap(bitmap, 0f, 0f, null)
    }

    fun textSizePercentage(): Int {
        return textSizePercentage
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        setDefaultBorderValues()

        if (attrs != null) {
            val typedArray = context.theme.obtainStyledAttributes(
                    attrs,
                    R.styleable.AvatarView,
                    0, 0)
            try {
                configureBorderValues(typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        borderPaint.isAntiAlias = true
        borderPaint.style = Paint.Style.FILL
        borderPaint.color = borderColor

        mainPaint.isAntiAlias = true
        mainPaint.color = resources.getColor(R.color.av_bitmap_background_color)
        mainPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    }

    private fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null) {
            return null
        }

        try {
            val bitmap = Bitmap.createBitmap(viewSize, viewSize, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, viewSize, viewSize)
            drawable.draw(canvas)

            return bitmap
        } catch (error: OutOfMemoryError) {
            Timber.d(error, "OutOfMemoryError occurred while generating bitmap")
            return null
        }

    }

    private fun cutIntoCircle(bitmap: Bitmap?): Bitmap? {
        if (bitmap == null) {
            return null
        }

        try {
            val output = Bitmap.createBitmap(viewSize, viewSize, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)

            canvas.drawARGB(0, 0, 0, 0)
            canvas.drawCircle((circleRadius + borderWidth).toFloat(), (circleRadius + borderWidth).toFloat(), circleRadius.toFloat(), borderPaint)

            canvas.drawBitmap(bitmap, circleRect, circleRect!!, mainPaint)
            return output
        } catch (error: OutOfMemoryError) {
            Timber.d(error, "OutOfMemoryError occurred while generating bitmap")
            return null
        }

    }

    private fun setDefaultBorderValues() {
        defaultBorderColor = resources.getColor(R.color.av_default_border)
        defaultBorderWidth = resources.getDimensionPixelSize(R.dimen.av_default_border_width)
    }

    private fun configureBorderValues(typedArray: TypedArray) {
        borderColor = typedArray.getColor(R.styleable.AvatarView_av_border_color, defaultBorderColor)
        borderWidth = typedArray.getDimensionPixelSize(R.styleable.AvatarView_av_border_width, defaultBorderWidth)
    }

    private fun saveBasicValues(canvas: Canvas) {
        val viewHeight = canvas.height
        val viewWidth = canvas.width

        viewSize = Math.min(viewWidth, viewHeight)

        circleCenterXValue = (viewWidth - viewSize) / 2
        circleCenterYValue = (viewHeight - viewSize) / 2
        circleRadius = (viewSize - borderWidth * 2) / 2

        circleRect = Rect(0, 0, viewSize, viewSize)

        maximizeAvailableBorderSize()

        if (viewSize != 0) {
            customdrawable = drawable
        }
    }

    internal fun maximizeAvailableBorderSize() {
        if (viewSize / 3 < borderWidth) {
            borderWidth = viewSize / 3
        }
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        invalidate()
    }
}