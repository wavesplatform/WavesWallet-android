package com.wavesplatform.wallet.v2.util

import android.app.Activity
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
import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs
import com.vicpin.krealmextensions.queryFirst
import com.wavesplatform.sdk.Constants
import com.wavesplatform.sdk.net.model.response.AssetBalance
import com.wavesplatform.sdk.net.model.response.ErrorResponse
import com.wavesplatform.sdk.net.model.response.TransactionType
import com.wavesplatform.sdk.utils.EnvironmentManager
import com.wavesplatform.sdk.utils.notNull
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.exception.RetrofitException
import com.wavesplatform.wallet.v2.data.model.db.AssetBalanceDb
import com.wavesplatform.wallet.v2.data.model.db.SpamAssetDb
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.AssetsAdapter
import okhttp3.ResponseBody
import pers.victor.ext.*
import pers.victor.ext.Ext.ctx
import pyxis.uzuki.live.richutilskt.utils.asDateString
import pyxis.uzuki.live.richutilskt.utils.runDelayed
import java.io.File

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

/**
 * @param action constant from EditorInfo
 * @see android.view.inputmethod.EditorInfo
 */
fun EditText.onAction(action: Int, runAction: () -> Unit) {
    this.setOnEditorActionListener { v, actionId, event ->
        return@setOnEditorActionListener when (actionId) {
            action -> {
                runAction.invoke()
                true
            }
            else -> false
        }
    }
}

fun <T1: Any, T2: Any, R: Any> safeLet(p1: T1?, p2: T2?, block: (T1, T2)->R?): R? {
    return if (p1 != null && p2 != null) block(p1, p2) else null
}
fun <T1: Any, T2: Any, T3: Any, R: Any> safeLet(p1: T1?, p2: T2?, p3: T3?, block: (T1, T2, T3)->R?): R? {
    return if (p1 != null && p2 != null && p3 != null) block(p1, p2, p3) else null
}
fun <T1: Any, T2: Any, T3: Any, T4: Any, R: Any> safeLet(p1: T1?, p2: T2?, p3: T3?, p4: T4?, block: (T1, T2, T3, T4)->R?): R? {
    return if (p1 != null && p2 != null && p3 != null && p4 != null) block(p1, p2, p3, p4) else null
}
fun <T1: Any, T2: Any, T3: Any, T4: Any, T5: Any, R: Any> safeLet(p1: T1?, p2: T2?, p3: T3?, p4: T4?, p5: T5?, block: (T1, T2, T3, T4, T5)->R?): R? {
    return if (p1 != null && p2 != null && p3 != null && p4 != null && p5 != null) block(p1, p2, p3, p4, p5) else null
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


fun Context.getToolBarHeight(): Int {
    val styledAttributes = getTheme().obtainStyledAttributes(
            intArrayOf(android.R.attr.actionBarSize))
    val mActionBarSize = styledAttributes.getDimension(0, 0f).toInt()
    styledAttributes.recycle()
    return mActionBarSize
}

fun Double.roundToDecimals(numDecimalPlaces: Int?): Double {
    return if (numDecimalPlaces != null) {
        val factor = Math.pow(10.0, numDecimalPlaces.toDouble())
        Math.round(this * factor) / factor
    } else {
        this
    }
}

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
    startActivity(intent, options)
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
    when {
        textBefore.indexOf(".") != -1 ->
            str.setSpan(StyleSpan(Typeface.BOLD), 0, textBefore.indexOf("."),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        textBefore.indexOf(" ") != -1 ->
            str.setSpan(StyleSpan(Typeface.BOLD), 0, textBefore.indexOf(" "),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        else -> str.setSpan(StyleSpan(Typeface.BOLD), 0, textBefore.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    this.text = str.append(" $textAfter")
}

fun loadDbWavesBalance(): AssetBalance {
    return find(Constants.WAVES_ASSET_ID_EMPTY)!!
}

fun find(assetId: String): AssetBalance? {
    return (queryFirst<AssetBalanceDb> { equalTo("assetId", assetId) })?.convertFromDb()
}

fun findByGatewayId(gatewayId: String): AssetBalance? { // ticker
    for (asset in EnvironmentManager.globalConfiguration.generalAssetIds) {
        if (asset.gatewayId == gatewayId) {
            return find(asset.assetId)
        }
    }
    return null
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
    val bufferClone = this.source().buffer()?.clone()
    return ResponseBody.create(this.contentType(), this.contentLength(), bufferClone)
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
            (null != queryFirst<SpamAssetDb> {
                equalTo("assetId", assetId)
            }))
}

fun AssetBalance.getItemType(): Int {
    return when {
        isSpam -> AssetsAdapter.TYPE_SPAM_ASSET
        isHidden -> AssetsAdapter.TYPE_HIDDEN_ASSET
        else -> AssetsAdapter.TYPE_ASSET
    }
}

fun restartApp() {
    val intent = Intent(App.getAppContext(), com.wavesplatform.wallet.v2.ui.splash.SplashActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
    App.getAppContext().startActivity(intent)
}