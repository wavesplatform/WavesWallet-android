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
import android.provider.Settings
import android.support.annotation.ColorRes
import android.support.annotation.IdRes
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.AppCompatTextView
import android.text.*
import android.text.format.DateUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
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
import com.vicpin.krealmextensions.queryFirst
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.exception.RetrofitException
import com.wavesplatform.wallet.v2.data.model.remote.response.*
import okhttp3.ResponseBody
import pers.victor.ext.*
import pers.victor.ext.Ext.ctx
import pyxis.uzuki.live.richutilskt.utils.asDateString
import pyxis.uzuki.live.richutilskt.utils.runDelayed
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*

val filterStartWithDot = InputFilter { source, start, end, dest, dstart, dend ->
    if (dest.isNullOrEmpty() && source.startsWith(".")) {
        return@InputFilter "0."
    }
    null
}

val filterEmptySpace = InputFilter { source, start, end, dest, dstart, dend ->
    if (dest.isNullOrEmpty() && source.startsWith(" ")) {
        return@InputFilter ""
    }
    null
}

/**
 * Created by anonymous on 13.09.17.
 */

fun EditText.applyFilterStartWithDot() {
    this.filters = arrayOf(filterStartWithDot)
}

fun EditText.applyFilterStartEmptySpace() {
    this.filters = arrayOf(filterEmptySpace)
}

fun Context.isNetworkConnection(): Boolean {
    val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = cm.activeNetworkInfo
    return activeNetwork != null && activeNetwork.isConnectedOrConnecting
}

fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
    for (service in activityManager.getRunningServices(Integer.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}

fun Long.currentDateAsTimeSpanString(context: Context): String {
    val currentTime = this

    if (currentTime == 0L) {
        return context.getString(R.string.dex_last_update_value,
                context.getString(R.string.dex_last_update_not_known))
    }

    // get current date as string
    val timeDayRelative = DateUtils.getRelativeTimeSpanString(
            currentTime, currentTime, DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE)

    // get hour in 24 hour time
    val timeHour = currentTime.asDateString("HH:mm")

    return context.getString(R.string.dex_last_update_value, "$timeDayRelative, $timeHour")
}

fun String.isWaves(): Boolean {
    return this.toLowerCase() == Constants.wavesAssetInfo.name.toLowerCase()
}

fun getWavesDexFee(fee: Long): BigDecimal {
    return MoneyUtil.getScaledText(fee, Constants.wavesAssetInfo.precision).clearBalance().toBigDecimal()
}

fun String.isWavesId(): Boolean {
    return this.toLowerCase() == Constants.wavesAssetInfo.id
}

fun ByteArray.arrayWithSize(): ByteArray {
    return Bytes.concat(Shorts.toByteArray(size.toShort()), this)
}

fun String.clearBalance(): String {
    return this.stripZeros().replace(MoneyUtil.DEFAULT_SEPARATOR_THIN_SPACE.toString(), "")
}

fun View.makeBackgroundWithRippleEffect() {
    val typedArray = context.obtainStyledAttributes(intArrayOf(R.attr.selectableItemBackground))
    val backgroundResource = typedArray.getResourceId(0, 0)
    this.isClickable = true
    this.setBackgroundResource(backgroundResource)
    typedArray.recycle()
}

fun getDeviceName(): String {
    val manufacturer = Build.MANUFACTURER
    val model = Build.MODEL
    return if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
        model.capitalize()
    } else {
        manufacturer.capitalize() + " " + model
    }
}

fun deleteRecursive(fileOrDirectory: File) {
    if (fileOrDirectory.isDirectory)
        for (child in fileOrDirectory.listFiles()!!)
            deleteRecursive(child)

    fileOrDirectory.delete()
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

fun TransactionType.icon(): Drawable? {
    return ContextCompat.getDrawable(app, this.image)
}

fun AlertDialog.makeStyled() {
    val titleTextView = this.findViewById<TextView>(R.id.alertTitle)
    val buttonPositive = this.findViewById<Button>(android.R.id.button1)
    val buttonNegative = this.findViewById<Button>(android.R.id.button2)

    try {
        buttonPositive?.typeface = ResourcesCompat.getFont(this.context, R.font.roboto_medium)
        buttonNegative?.typeface = ResourcesCompat.getFont(this.context, R.font.roboto_medium)
        titleTextView?.typeface = ResourcesCompat.getFont(this.context, R.font.roboto_medium)
    } catch (e: Throwable) {
        buttonPositive?.typeface = Typeface.DEFAULT
        buttonNegative?.typeface = Typeface.DEFAULT
        titleTextView?.typeface = Typeface.DEFAULT
    }

    buttonPositive?.setTextColor(findColor(R.color.submit300))
    buttonNegative?.setTextColor(findColor(R.color.submit300))
}

fun Context.isAppOnForeground(): Boolean {
    val appProcesses: MutableList<ActivityManager.RunningAppProcessInfo>? = activityManager.runningAppProcesses
            ?: return false
    val packageName = packageName
    appProcesses?.forEach {
        if (it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                it.processName.equals(packageName)) {
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

fun Number.roundTo(numFractionDigits: Int?) = String.format("%.${numFractionDigits}f", toDouble()).toDouble()

fun Double.roundToDecimals(numDecimalPlaces: Int?): Double {
    return if (numDecimalPlaces != null) {
        val factor = Math.pow(10.0, numDecimalPlaces.toDouble())
        Math.round(this * factor) / factor
    } else {
        this
    }
}

fun ClosedRange<Int>.random() =
        Random().nextInt((endInclusive + 1) - start) + start

fun TextView.makeLinks(links: Array<String>, clickableSpans: Array<ClickableSpan>) {
    val spannableString = SpannableString(this.text)

    for (i in links.indices) {
        val clickableSpan = clickableSpans[i]
        val link = links[i]

        val startIndexOfLink = this.text.indexOf(link)

        if (startIndexOfLink > -1) {
            spannableString.setSpan(clickableSpan, startIndexOfLink, startIndexOfLink + link.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
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

fun Fragment.showError(msg: String, @IdRes viewId: Int) {
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

fun Activity.showError(msg: String, @IdRes viewId: Int, @ColorRes color: Int? = R.color.error400) {
    showMessage(msg, viewId, color)
}

fun Activity.showMessage(msg: String, @IdRes viewId: Int, @ColorRes color: Int? = null) {
    Snackbar.make(findViewById(viewId), msg, Snackbar.LENGTH_LONG)
            .withColor(color)
            .show()
}

fun showMessage(msg: String, view: View, @ColorRes color: Int? = null) {
    Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
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

fun View.copyToClipboard(
    text: String,
    textView: AppCompatTextView,
    copyIcon: Int = R.drawable.ic_copy_18_black,
    copyColor: Int = R.color.black
) {
    clipboardManager.primaryClip = ClipData.newPlainText(this.context.getString(R.string.app_name), text)
    showSnackbar(R.string.common_copied_to_clipboard, R.color.success500_0_94, Snackbar.LENGTH_SHORT)

    textView.notNull { view ->
        view.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(this.context, R.drawable.ic_check_18_success_400),
                null, null, null)
        textView.setTextColor(ContextCompat.getColor(this.context, R.color.success500_0_94))
        runDelayed(1500) {
            this.context.notNull {
                view.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(it, copyIcon), null, null, null)
                textView.setTextColor(ContextCompat.getColor(it, copyColor))
            }
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
        return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)
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
    noinline init: Intent.() -> Unit = {}
) {

    var intent = newIntent<T>(this)
    if (options != null) intent.putExtras(options)

    if (clear) {
        setResult(Activity.RESULT_CANCELED)
        finishAffinity()
        intent = newClearIntent<T>(this)
    }

    intent.init()
    if (requestCode != -1) {
        startActivityForResult(intent, requestCode)
    } else {
        startActivity(intent)
    }
    if (withoutAnimation) {
        overridePendingTransition(0, 0)
    }
}

inline fun <reified T : Any> Fragment.launchActivity(
    requestCode: Int = -1,
    clear: Boolean = false,
    withoutAnimation: Boolean = false,
    options: Bundle? = null,
    noinline init: Intent.() -> Unit = {}
) {

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
    if (withoutAnimation) {
        activity?.overridePendingTransition(0, 0)
        // todo activity?.overridePendingTransition(R.anim.start_new_show,  R.anim.start_current_hide)
    }
}

inline fun <reified T : Any> Context.launchActivity(
    options: Bundle? = null,
    clear: Boolean = false,
    noinline init: Intent.() -> Unit = {}
) {

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

fun findMyOrder(first: Order, second: Order, address: String?): Order {
    return if (first.sender == second.sender) {
        if (first.timestamp > second.timestamp) {
            first
        } else {
            second
        }
    } else if (first.sender == address) {
        first
    } else if (second.sender == address) {
        second
    } else {
        if (first.timestamp > second.timestamp) {
            first
        } else {
            second
        }
    }
}

fun loadDbWavesBalance(): AssetBalance {
    return queryFirst<AssetBalance> { equalTo("assetId", Constants.WAVES_ASSET_ID_EMPTY) }
            ?: Constants.find(Constants.WAVES_ASSET_ID_EMPTY)!!
}

fun getDeviceId(): String {
    return "android:${Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID)}"
}

fun Throwable.errorBody(): ErrorResponse? {
    return if (this is RetrofitException) {
        this.getErrorBodyAs(ErrorResponse::class.java)
    } else {
        null
    }
}

fun ResponseBody.clone(): ResponseBody {
    var bufferClone = this.source().buffer()?.clone()
    return ResponseBody.create(this.contentType(), this.contentLength(), bufferClone)
}

fun ErrorResponse.isSmartError(): Boolean {
    return this.error in 305..307
}

fun AssetInfo.getTicker(): String {

    if (this.id.isWavesId()) {
        return Constants.wavesAssetInfo.name
    }

    return this.ticker ?: this.name
}

fun getScaledAmount(amount: Long, decimals: Int): String {
    val absAmount = Math.abs(amount)
    val value = BigDecimal.valueOf(absAmount, decimals)
    if (amount == 0L) {
        return "0"
    }

    val sign = if (amount < 0) "-" else ""

    return sign + when {
        value >= MoneyUtil.ONE_B -> value.divide(MoneyUtil.ONE_B, 1, RoundingMode.FLOOR)
                .toPlainString().stripZeros() + "B"
        value >= MoneyUtil.ONE_M -> value.divide(MoneyUtil.ONE_M, 1, RoundingMode.FLOOR)
                .toPlainString().stripZeros() + "M"
        value >= MoneyUtil.ONE_K -> value.divide(MoneyUtil.ONE_K, 1, RoundingMode.FLOOR)
                .toPlainString().stripZeros() + "k"
        else -> MoneyUtil.createFormatter(decimals).format(BigDecimal.valueOf(absAmount, decimals))
                .stripZeros() + ""
    }
}

fun Context.showAlertAboutScriptedAccount(buttonOnClickListener: () -> Unit = { }) {
    val alertDialogBuilder = AlertDialog.Builder(this, R.style.DialogBackgroundTheme).create()

    fun getAlertView(): View {
        val view = inflate(R.layout.dialog_account_scripted_error_layout)

        view.findViewById<AppCompatButton>(R.id.button_confirm).click {
            alertDialogBuilder.dismiss()
            buttonOnClickListener.invoke()
        }

        return view
    }

    alertDialogBuilder.requestWindowFeature(Window.FEATURE_NO_TITLE)
    val wmlp = alertDialogBuilder.window.attributes
    wmlp.gravity = Gravity.BOTTOM

    alertDialogBuilder.setCancelable(false)
    alertDialogBuilder.setView(getAlertView())
    alertDialogBuilder.show()
}

fun isSpamConsidered(assetId: String?, prefsUtil: PrefsUtil): Boolean {
    return (prefsUtil.getValue(PrefsUtil.KEY_ENABLE_SPAM_FILTER, true) &&
            (null != queryFirst<SpamAsset> {
        equalTo("assetId", assetId)
    }))
}

fun isShowTicker(assetId: String?): Boolean {
    return EnvironmentManager.globalConfiguration.generalAssetIds.any {
        it.assetId == assetId || assetId.isNullOrEmpty()
    }
}