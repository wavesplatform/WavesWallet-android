package com.wavesplatform.wallet.v2.util

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.app.Activity
import android.content.ClipData.newIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.icu.lang.UProperty.INT_START
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.wavesplatform.wallet.R
import pers.victor.ext.dp2px
import pyxis.uzuki.live.richutilskt.utils.toast
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by anonymous on 13.09.17.
 */
fun Context.isNetworkConnection(): Boolean {
    val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = cm.activeNetworkInfo
    return activeNetwork != null && activeNetwork.isConnectedOrConnecting
}

fun Context.notAvailable() {
    toast(getString(R.string.msg_in_development))
}


fun Activity.setSystemBarTheme(pIsDark: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // Fetch the current flags.
        val lFlags = this.window.decorView.systemUiVisibility
        // Update the SystemUiVisibility dependening on whether we want a Light or Dark theme.
        this.window.decorView.systemUiVisibility = if (pIsDark) lFlags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() else lFlags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
}

fun <T : Any> T?.notNull(f: (it: T) -> Unit) {
    if (this != null) f(this)
}

fun String?.getAge(): String {
    if (this.isNullOrEmpty()) return ""

    val dob = Calendar.getInstance()
    val today = Calendar.getInstance()

    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    dob.time = sdf.parse(this)

    var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)

    if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
        age--
    }

    val ageInt = age

    return ageInt.toString()
}


fun ImageView.loadImage(url: String?, centerCrop: Boolean = true) {
    this.post({
        val options = RequestOptions()
                .override(this.width, this.height)

        if (centerCrop) options.transform(CenterCrop())

        Glide.with(this)
                .asBitmap()
                .load(url)
                .apply(options)
                .into(this)
    })
}

fun Context.getViewScaleAnimator(from: View, target: View, additionalPadding: Int = 0): Animator {
    // height resize animation
    val animatorSet = AnimatorSet()
    val desiredHeight = from.height
    val currentHeight = target.height
    val heightAnimator = ValueAnimator.ofInt(currentHeight, desiredHeight - additionalPadding)
    heightAnimator.addUpdateListener { animation ->
        val params = target.layoutParams as ViewGroup.LayoutParams
        params.height = animation.animatedValue as Int
        target.layoutParams = params
    }
    animatorSet.play(heightAnimator)

    // width resize animation
    val desiredWidth = from.width
    val currentWidth = target.width
    val widthAnimator = ValueAnimator.ofInt(currentWidth, desiredWidth - additionalPadding)
    widthAnimator.addUpdateListener { animation ->
        val params = target.layoutParams as ViewGroup.LayoutParams
        params.width = animation.animatedValue as Int
        target.layoutParams = params
    }
    animatorSet.play(widthAnimator)
    return animatorSet
}

fun ImageView.loadImage(drawableRes: Int?, centerCrop: Boolean = true) {
    this.post({
        val options = RequestOptions()
                .override(this.width, this.height)

        if (centerCrop) options.transform(CenterCrop())

        Glide.with(this)
                .asBitmap()
                .load(drawableRes)
                .apply(options)
                .into(this)
    })
}

fun ImageView.loadImage(file: File?, centerCrop: Boolean = true, circleCrop: Boolean = false, deleteImmediately: Boolean = true) {
    this.post({
        val options = RequestOptions()
                .override(this.width, this.height)

        if (centerCrop) options.transform(CenterCrop())
        if (circleCrop) options.transform(CircleCrop())

        Glide.with(this)
                .load(file)
                .apply(options)
                .listener(object : RequestListener<Drawable> {
                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        this@loadImage.setImageDrawable(resource)
                        if (deleteImmediately) file?.delete()
                        return true
                    }

                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        return true
                    }

                })
                .into(this)
    })
}


@SuppressWarnings("deprecation")
fun Context.fromHtml(source: String): Spanned {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
    } else {
        return Html.fromHtml(source)
    }
}

/**
 * Extensions for simpler launching of Activities
 */

inline fun <reified T : Any> Activity.launchActivity(
        requestCode: Int = -1,
        clear: Boolean = false,
        withoutAnimation: Boolean = false,
        options: Bundle? = null,
        noinline init: Intent.() -> Unit = {}) {

    var intent = newIntent<T>(this)
    if (options != null) intent.putExtras(options)

    if (clear) {
        intent = newClearIntent<T>(this)
    }

    intent.init()
    if (requestCode != -1) {
        startActivityForResult(intent, requestCode)
    } else {
        startActivity(intent)
    }
    if (withoutAnimation) overridePendingTransition(0, 0)
}

inline fun <reified T : Any> Fragment.launchActivity(
        requestCode: Int = -1,
        clear: Boolean = false,
        withoutAnimation: Boolean = false,
        options: Bundle? = null,
        noinline init: Intent.() -> Unit = {}) {

    var intent = newIntent<T>(activity!!)
    if (options != null) intent.putExtras(options)

    if (clear) {
        intent = newClearIntent<T>(activity!!)
    }

    intent.init()
    if (requestCode != -1) {
        startActivityForResult(intent, requestCode)
    } else {
        startActivity(intent)
    }
    if (withoutAnimation) activity?.overridePendingTransition(0, 0)
}

inline fun <reified T : Any> Context.launchActivity(
        options: Bundle? = null,
        clear: Boolean = false,
        noinline init: Intent.() -> Unit = {}) {

    var intent = newIntent<T>(this)
    if (options != null) intent.putExtras(options)

    if (clear) {
        intent = newClearIntent<T>(this)
    }


    intent.init()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        startActivity(intent, options)
    } else {
        startActivity(intent)
    }
}

inline fun <reified T : Any> newIntent(context: Context): Intent = Intent(context, T::class.java)

inline fun <reified T : Any> newClearIntent(context: Context): Intent {
    var intent = Intent(context, T::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    return intent
}

fun View.setMargins(
        left: Int? = null,
        top: Int? = null,
        right: Int? = null,
        bottom: Int? = null
) {
    val lp = layoutParams as? ViewGroup.MarginLayoutParams
            ?: return

    lp.setMargins(
            left ?: lp.leftMargin,
            top ?: lp.topMargin,
            right ?: lp.rightMargin,
            bottom ?: lp.rightMargin
    )

    layoutParams = lp
}

fun TextView.makeTextHalfBold() {
    val text = this.text.toString()
    val str = SpannableStringBuilder(text)
    str.setSpan(StyleSpan (Typeface.BOLD), 0, text.indexOf("."), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    this.text = str
}