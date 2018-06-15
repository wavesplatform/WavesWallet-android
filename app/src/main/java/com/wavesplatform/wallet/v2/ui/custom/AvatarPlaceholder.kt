package com.wavesplatform.wallet.v2.ui.custom

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.annotation.IntRange
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import pers.victor.ext.sp2px


class AvatarPlaceholder @JvmOverloads constructor(var context: Context, name: String, @param:IntRange private var textSize: Int = DEFAULT_TEXT_SIZE, defaultString: String = DEFAULT_PLACEHOLDER_STRING) : Drawable() {

    private val textPaint: Paint
    private val backgroundPaint: Paint
    private var placeholderBounds: RectF? = null

    private val avatarText: String
    private val defaultString: String

    private var textStartXPoint: Float = 0.toFloat()
    private var textStartYPoint: Float = 0.toFloat()

    init {
        this.defaultString = resolveStringWhenNoName(defaultString)
        this.avatarText = convertNameToAvatarText(name)

        textPaint = Paint()
        textPaint.isAntiAlias = true
        textPaint.color = Color.parseColor("white")
        textPaint.typeface = ResourcesCompat.getFont(context, R.font.roboto)

        backgroundPaint = Paint()
        backgroundPaint.isAntiAlias = true
        backgroundPaint.style = Paint.Style.FILL
        backgroundPaint.color = convertStringToColor(name)
    }

    override fun draw(canvas: Canvas) {
        if (placeholderBounds == null) {
            placeholderBounds = RectF(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat())
            setAvatarTextValues()
        }

        canvas.drawRect(placeholderBounds!!, backgroundPaint)
        canvas.drawText(avatarText, textStartXPoint, textStartYPoint, textPaint)
    }

    override fun setAlpha(alpha: Int) {
        textPaint.alpha = alpha
        backgroundPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        textPaint.colorFilter = colorFilter
        backgroundPaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    private fun setAvatarTextValues() {
        textPaint.textSize = textSize.toFloat()
        textStartXPoint = calculateTextStartXPoint()
        textStartYPoint = calculateTextStartYPoint()
    }

    private fun calculateTextStartXPoint(): Float {
        val stringWidth = textPaint.measureText(avatarText)
        return bounds.width() / 2f - stringWidth / 2f
    }

    private fun calculateTextStartYPoint(): Float {
        return bounds.height() / 2f - (textPaint.ascent() + textPaint.descent()) / 2f
    }

    private fun resolveStringWhenNoName(stringWhenNoName: String): String {
        return if (!stringWhenNoName.isNullOrEmpty()) stringWhenNoName else DEFAULT_PLACEHOLDER_STRING
    }

    private fun convertNameToAvatarText(name: String): String {
        return if (!name.isNullOrEmpty()) name.substring(0, 1).toUpperCase() else defaultString
    }

    private fun convertStringToColor(text: String): Int {
        return if (text.isNullOrEmpty()) ContextCompat.getColor(context, R.color.persist) else {
            var alphabetColor = Constants.alphabetColor[avatarText.substring(0, 1).toLowerCase()]
            if (alphabetColor == null) {
                ContextCompat.getColor(context, R.color.persist)
            } else {
                ContextCompat.getColor(context, alphabetColor)
            }
        }
    }

    companion object {
        val DEFAULT_PLACEHOLDER_STRING = "-"
        val DEFAULT_TEXT_SIZE = sp2px(24)
    }
}