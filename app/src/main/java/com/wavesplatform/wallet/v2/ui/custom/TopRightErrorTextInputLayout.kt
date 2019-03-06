package com.wavesplatform.wallet.v2.ui.custom

import android.content.Context
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.Gravity
import android.widget.RelativeLayout
import android.widget.TextView
import com.wavesplatform.wallet.R
import pers.victor.ext.dp2px
import pers.victor.ext.gone
import pers.victor.ext.sp2px

class TopRightErrorTextInputLayout(context: Context, attrs: AttributeSet) : TextInputLayout(context, attrs) {
    var topRightAlignedErrorText: TextView = TextView(context)

    init {
        topRightAlignedErrorText.setTextColor(ContextCompat.getColor(context, R.color.error500))
        topRightAlignedErrorText.textSize = 12f
        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        params.rightMargin = dp2px(4)
        topRightAlignedErrorText.layoutParams = params
        topRightAlignedErrorText.gravity = Gravity.END
        addView(topRightAlignedErrorText)

        this.post {
            topRightAlignedErrorText.post {
                topRightAlignedErrorText.translationY = -this.height + topRightAlignedErrorText.height.toFloat() - sp2px(2)
            }
            if (childCount == 3) {
                getChildAt(1).gone()
            }
        }
    }

    override fun setError(error: CharSequence?) {
        topRightAlignedErrorText.text = error
        if (error.isNullOrEmpty()) {
            super.setError(error)
        } else {
            super.setError(" ")
        }
    }
}