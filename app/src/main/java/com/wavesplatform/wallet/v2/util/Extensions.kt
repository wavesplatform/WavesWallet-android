package com.wavesplatform.wallet.v2.util

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.app.Activity
import android.app.ActivityManager
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.annotation.IdRes
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatImageView
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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
import com.google.common.primitives.Bytes
import com.google.common.primitives.Shorts
import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.data.model.remote.response.TransactionType
import pers.victor.ext.activityManager
import pers.victor.ext.app
import pers.victor.ext.clipboardManager
import pers.victor.ext.findColor
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

fun Context.isMyServiceRunning(serviceClass: Class<*>): Boolean {
    for (service in activityManager.getRunningServices(Integer.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}

fun getActionBarHeight(): Int {
    val tv = TypedValue()
    return if (app.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
        TypedValue.complexToDimensionPixelSize(tv.data, app.resources.displayMetrics);
    }else{
        0
    }
}

fun EditText.applySpaceFilter() {
    this.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
        source.toString().filterNot { it.isWhitespace() }
    })
}

fun Context.notAvailable() {
    toast(getString(R.string.common_msg_in_development))
}

fun ByteArray.arrayWithSize(): ByteArray {
    return Bytes.concat(Shorts.toByteArray(size.toShort()), this)
}

fun Activity.openUrlWithChromeTab(url: String) {
    SimpleChromeCustomTabs.getInstance()
            .withFallback {
                openUrlWithIntent(url)
            }.withIntentCustomizer {
                it.withToolbarColor(findColor(R.color.submit400))
            }
            .navigateTo(Uri.parse(url), this)
}

fun Fragment.openUrlWithChromeTab(url: String) {
    SimpleChromeCustomTabs.getInstance()
            .withFallback {
                activity?.openUrlWithIntent(url)
            }.withIntentCustomizer {
                it.withToolbarColor(findColor(R.color.submit400))
            }
            .navigateTo(Uri.parse(url), activity)

}

fun Activity.openUrlWithIntent(url: String) {
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(browserIntent)
}

fun Transaction.transactionType(): TransactionType {
    return TransactionType.getTypeById(this.transactionTypeId)
}

fun Long.toKKType(): String {
    if (this / 1000000000 >= 1) return String.format(app.getString(R.string.common_kkk_quantity), this / 1000000000)
    if (this / 1000000 >= 1) return String.format(app.getString(R.string.common_kk_quantity), this / 1000000)
    if (this / 1000 > 0) return String.format(app.getString(R.string.common_k_quantity), this / 1000)
    return this.toString()
}

fun TransactionType.icon(): Drawable? {
    return ContextCompat.getDrawable(app, this.image)
}

fun TransactionType.title(): String {
    return app.getString(this.title)
}

fun AlertDialog.makeStyled() {
    val titleTextView = this.findViewById<TextView>(R.id.alertTitle)
    val buttonPositive = this.findViewById<Button>(android.R.id.button1)
    val buttonNegative = this.findViewById<Button>(android.R.id.button2)
    buttonPositive?.typeface = ResourcesCompat.getFont(this.context, R.font.roboto_medium)
    buttonNegative?.typeface = ResourcesCompat.getFont(this.context, R.font.roboto_medium)
    titleTextView?.typeface = ResourcesCompat.getFont(this.context, R.font.roboto_medium)

    buttonPositive?.setTextColor(findColor(R.color.submit300))
    buttonNegative?.setTextColor(findColor(R.color.submit300))
}

fun Context.isAppOnForeground(): Boolean {
    val appProcesses: MutableList<ActivityManager.RunningAppProcessInfo>? = activityManager.runningAppProcesses
            ?: return false
    val packageName = packageName
    appProcesses?.forEach {
        if (it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                && it.processName.equals(packageName)) {
            return true
        }
    }
    return false
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
        this.window.decorView.systemUiVisibility = if (pIsDark) {
            lFlags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        } else {
            lFlags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
}

fun String.stripZeros(): String {
    if (this == "0.0") return this
    return if (!this.contains(".")) this else this.replace("0*$".toRegex(), "").replace("\\.$".toRegex(), "")
}


fun Fragment.showSuccess(@StringRes msgId: Int, @IdRes viewId: Int) {
    showSuccess(getString(msgId), viewId)
}

fun Fragment.showSuccess(msg: String, @IdRes viewId: Int) {
    showMessage(msg, viewId, R.color.success500)
}

fun Fragment.showError(@StringRes msgId: Int, @IdRes viewId: Int) {
    showError(getString(msgId), viewId)
}

fun Fragment.showError(msg: String, @IdRes viewId: Int, @ColorRes color: Int? = null) {
    showMessage(msg, viewId, R.color.error400)
}

fun Fragment.showMessage(msg: String, @IdRes viewId: Int, @ColorRes color: Int? = null) {
    view.notNull { v ->
        Snackbar.make(v.findViewById(viewId), msg, Snackbar.LENGTH_LONG)
                .withColor(color)
                .show()
    }
}

fun Activity.showSnackbar(@StringRes msg: Int, @ColorRes color: Int? = null, during: Int = Snackbar.LENGTH_LONG) {
    Snackbar.make(findViewById(android.R.id.content), getString(msg), during)
            .withColor(color)
            .show()
}

fun View.showSnackbar(@StringRes msg: Int, @ColorRes color: Int? = null, during: Int = Snackbar.LENGTH_LONG) {
    if (context is Activity) {
        Snackbar.make(this, context.getString(msg), during)
                .withColor(color)
                .show()
    }
}

fun Activity.showSuccess(@StringRes msgId: Int, @IdRes viewId: Int) {
    showSuccess(getString(msgId), viewId)
}

fun Activity.showSuccess(msg: String, @IdRes viewId: Int) {
    showMessage(msg, viewId, R.color.success500)
}

fun Activity.showError(@StringRes msgId: Int, @IdRes viewId: Int) {
    showMessage(getString(msgId), viewId, R.color.error400)
}

fun Activity.showError(msg: String, @IdRes viewId: Int, @ColorRes color: Int? = null) {
    showMessage(msg, viewId, color)
}

fun Activity.showMessage(msg: String, @IdRes viewId: Int, @ColorRes color: Int? = null) {
    Snackbar.make(findViewById(viewId), msg, Snackbar.LENGTH_LONG)
            .withColor(color)
            .show()
}


fun ImageView.copyToClipboard(text: String, copyIcon: Int = R.drawable.ic_copy_18_black) {
    clipboardManager.primaryClip = ClipData.newPlainText(this.context.getString(R.string.app_name), text)
    showSnackbar(R.string.common_copied_to_clipboard, R.color.success500_0_94, Snackbar.LENGTH_SHORT)

    this.notNull { image ->
        image.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.ic_check_18_success_400))
        runDelayed(1500) {
            this.context.notNull { image.setImageDrawable(ContextCompat.getDrawable(it, copyIcon)) }
        }
    }

}

fun TextView.copyToClipboard(imageView: AppCompatImageView? = null, copyIcon: Int = R.drawable.ic_copy_18_black) {
    clipboardManager.primaryClip = ClipData.newPlainText(this.context.getString(R.string.app_name), this.text.toString())
    showSnackbar(R.string.common_copied_to_clipboard, R.color.success500_0_94, Snackbar.LENGTH_SHORT)

    imageView.notNull { image ->
        image.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.ic_check_18_success_400))
        runDelayed(1500) {
            this.context.notNull { image.setImageDrawable(ContextCompat.getDrawable(it, copyIcon)) }
        }
    }
}

inline fun <T> Iterable<T>.sumByLong(selector: (T) -> Long): Long {
    var sum = 0L
    for (element in this) {
        sum += selector(element)
    }
    return sum
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
    this.post {
        val options = RequestOptions()
                .override(this.width, this.height)

        if (centerCrop) options.transform(CenterCrop())

        Glide.with(this)
                .asBitmap()
                .load(url)
                .apply(options)
                .into(this)
    }
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
    this.post {
        val options = RequestOptions()
                .override(this.width, this.height)

        if (centerCrop) options.transform(CenterCrop())

        Glide.with(this)
                .asBitmap()
                .load(drawableRes)
                .apply(options)
                .into(this)
    }
}

fun ImageView.loadImage(file: File?, centerCrop: Boolean = true, circleCrop: Boolean = false, deleteImmediately: Boolean = true) {
    this.post {
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
    }
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

fun Snackbar.withColor(@ColorRes colorInt: Int?): Snackbar {
    colorInt.notNull {
        this.view.setBackgroundColor(findColor(it))
    }
    return this
}

inline fun <reified T : Any> newIntent(context: Context): Intent = Intent(context, T::class.java)

inline fun <reified T : Any> newClearIntent(context: Context): Intent {
    val intent = Intent(context, T::class.java)
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
    val textBefore = this.text.toString().substringBefore(" ")
    val textAfter = if (text.indexOf(" ") != -1) {
        this.text.toString().substringAfter(" ")
    } else {
        ""
    }
    val str = SpannableStringBuilder(textBefore)
    if (textBefore.indexOf(".") != -1) {
        str.setSpan(StyleSpan(Typeface.BOLD), 0, textBefore.indexOf("."), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    } else if (textBefore.indexOf(" ") != -1) {
        str.setSpan(StyleSpan(Typeface.BOLD), 0, textBefore.indexOf(" "), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    } else {
        str.setSpan(StyleSpan(Typeface.BOLD), 0, textBefore.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    this.text = str.append(" $textAfter")
}