/*
 * Created by Eduard Zaydel on 15/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.custom

import android.content.Context
import android.graphics.Bitmap
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.wavesplatform.wallet.R
import kotlinx.android.synthetic.main.content_top_info_alert.view.*
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.visiable

class InfoAlert : FrameLayout {

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
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.content_top_info_alert, this, true)
    }

    fun setTitle(title: String) {
        text_alert_title.text = title
    }

    fun setTitle(@StringRes title: Int) {
        text_alert_title.text = context?.getString(title)
    }

    fun setDescription(description: String) {
        text_alert_description.text = description
    }

    fun setDescription(@StringRes description: Int) {
        text_alert_description.text = context?.getString(description)
    }

    fun setIcon(icon: Bitmap) {
        image_alert_icon.setImageBitmap(icon)
    }

    fun setIcon(@DrawableRes icon: Int) {
        image_alert_icon.setImageResource(icon)
    }

    fun setActionIcon(icon: Bitmap) {
        image_alert_action.setImageBitmap(icon)
    }

    fun setActionIcon(@DrawableRes icon: Int) {
        image_alert_action.setImageResource(icon)
    }

    fun onActionIconClick(click: () -> Unit) {
        image_alert_action.click {
            click.invoke()
        }
    }

    fun onAlertClick(click: () -> Unit) {
        relative_root_alert.click {
            click.invoke()
        }
    }

    fun show() {
        this.visiable()
    }

    fun hide() {
        this.gone()
    }
}
