package com.wavesplatform.wallet.v2.util

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.app.Activity
import android.app.ActivityManager
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.res.TypedArray
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.data.model.remote.response.TransactionType
import pers.victor.ext.activityManager
import pers.victor.ext.app
import pers.victor.ext.clipboardManager
import pers.victor.ext.toast
import pyxis.uzuki.live.richutilskt.utils.runDelayed
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
    toast(getString(R.string.common_msg_in_development))
}

fun Transaction.transactionType() : TransactionType{
    return TransactionType.getTypeById(this.transactionTypeId)
}

fun TransactionType.icon(): Drawable? {
    return ContextCompat.getDrawable(app, this.image)
}

fun TransactionType.title(): String {
    return app.getString(this.title)
}

fun Context.isAppOnForeground(): Boolean {
    val appProcesses: MutableList<ActivityManager.RunningAppProcessInfo>? = activityManager.runningAppProcesses
            ?: return false
    val packageName = getPackageName();
    appProcesses?.forEach {
        if (it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && it.processName.equals(packageName)) {
            return true;
        }
    }
    return false;
}

fun Context.getToolBarHeight(): Int {
    val styledAttributes = getTheme().obtainStyledAttributes(
            intArrayOf(android.R.attr.actionBarSize))
    val mActionBarSize = styledAttributes.getDimension(0, 0f).toInt()
    styledAttributes.recycle()
    return mActionBarSize
}

fun ClosedRange<Int>.random() =
        Random().nextInt((endInclusive + 1) - start) + start

fun TextView.makeLinks(links: Array<String>, clickableSpans: Array<ClickableSpan>) {
    val spannableString = SpannableString(this.text)

    for (i in links.indices) {
        val clickableSpan = clickableSpans[i]
        val link = links[i]

        val startIndexOfLink = this.text.indexOf(link)

        spannableString.setSpan(clickableSpan, startIndexOfLink, startIndexOfLink + link.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    this.movementMethod = LinkMovementMethod.getInstance()
    this.setText(spannableString, TextView.BufferType.SPANNABLE)
}

fun Activity.setSystemBarTheme(pIsDark: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // Fetch the current flags.
        val lFlags = this.window.decorView.systemUiVisibility
        // Update the SystemUiVisibility dependening on whether we want a Light or Dark theme.
        this.window.decorView.systemUiVisibility = if (pIsDark) lFlags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() else lFlags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
}

fun TextView.copyToClipboard(imageView: AppCompatImageView? = null, copyIcon: Int = R.drawable.ic_copy_18_black) {
    clipboardManager.primaryClip = ClipData.newPlainText(this.context.getString(R.string.app_name), this.text.toString())
    toast(this.context.getString(R.string.common_copied_to_clipboard))

    imageView.notNull { image ->
        image.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.ic_check_18_success_400))
        runDelayed(1500, {
            this.context.notNull { image.setImageDrawable(ContextCompat.getDrawable(it, copyIcon)) }
        })
    }
}

fun ImageView.copyToClipboard(text: String, copyIcon: Int = R.drawable.ic_copy_18_black) {
    clipboardManager.primaryClip = ClipData.newPlainText(this.context.getString(R.string.app_name), text)
    toast(this.context.getString(R.string.common_copied_to_clipboard))

    this.notNull { image ->
        image.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.ic_check_18_success_400))
        runDelayed(1500, {
            this.context.notNull { image.setImageDrawable(ContextCompat.getDrawable(it, copyIcon)) }
        })
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
    if (text.indexOf(".") != -1){
        str.setSpan(StyleSpan(Typeface.BOLD), 0, text.indexOf("."), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
    this.text = str
}