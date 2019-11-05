/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.util

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
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
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.google.zxing.integration.android.IntentIntegrator
import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs
import com.vicpin.krealmextensions.queryFirst
import com.wavesplatform.sdk.crypto.WavesCrypto
import com.wavesplatform.sdk.crypto.WavesCrypto.Companion.calcCheckSum
import com.wavesplatform.sdk.model.request.node.*
import com.wavesplatform.sdk.model.response.ErrorResponse
import com.wavesplatform.sdk.model.response.data.AssetInfoResponse
import com.wavesplatform.sdk.model.response.data.LastTradesResponse
import com.wavesplatform.sdk.model.response.matcher.AssetPairOrderResponse
import com.wavesplatform.sdk.model.response.node.AssetBalanceResponse
import com.wavesplatform.sdk.model.response.node.HistoryTransactionResponse
import com.wavesplatform.sdk.model.response.node.OrderResponse
import com.wavesplatform.sdk.net.NetworkException
import com.wavesplatform.sdk.utils.*
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.db.AssetBalanceDb
import com.wavesplatform.wallet.v2.data.model.db.SpamAssetDb
import com.wavesplatform.wallet.v2.data.model.local.OrderStatus
import com.wavesplatform.wallet.v2.data.model.local.OrderType
import com.wavesplatform.wallet.v2.data.model.local.TransactionType
import com.wavesplatform.wallet.v2.ui.auth.qr_scanner.QrCodeScannerActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.AssetsAdapter
import okhttp3.ResponseBody
import pers.victor.ext.*
import pers.victor.ext.Ext.ctx
import pyxis.uzuki.live.richutilskt.impl.F2
import pyxis.uzuki.live.richutilskt.utils.*
import java.io.File
import java.util.*
import kotlin.arrayOf

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

inline fun <T : View> T.afterMeasured(crossinline f: T.() -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                f()
            }
        }
    })
}

fun String.formatBaseUrl(): String {
    return if (this.isNotEmpty()) {
        if (this.lastOrNull() == '/') {
            this
        } else {
            this.plus("/")
        }
    } else {
        this
    }
}

const val REQUEST_SCAN_QR_CODE = 876
fun Activity.launchQrCodeScanner(requestCode: Int = REQUEST_SCAN_QR_CODE) {
    RPermission.instance.checkPermission(this, arrayOf(Manifest.permission.CAMERA), F2 { result, permissions ->
        if (result == RPermission.PERMISSION_GRANTED) {
            IntentIntegrator(this)
                    .setRequestCode(requestCode)
                    .setOrientationLocked(true)
                    .setBeepEnabled(false)
                    .setCaptureActivity(QrCodeScannerActivity::class.java)
                    .initiateScan()
        } else {
            AlertDialog.Builder(this)
                    .create()
                    .apply {
                        setTitle(getString(R.string.common_permission_error_title))
                        setMessage(getString(R.string.common_permission_error_description))
                        setButton(
                                AlertDialog.BUTTON_POSITIVE,
                                getString(R.string.common_permission_error_settings)) { dialog, _ ->
                            launchPermissionsAppSettings()
                            dialog.dismiss()
                        }
                        setButton(
                                AlertDialog.BUTTON_NEGATIVE,
                                getString(R.string.common_permission_error_cancel)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        show()
                        makeStyled()
                    }
        }
    })
}

fun Context.launchPermissionsAppSettings() {
    val intent = Intent().apply {
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        data = Uri.fromParts("package", packageName, null)
        addCategory(Intent.CATEGORY_DEFAULT)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    }
    startActivity(intent)
}

fun View.animateVisible() {
    this.animate()
            .alpha(Constants.View.FULL_VISIBILITY)
            .setDuration(Constants.View.DEFAULT_ANIMATION_DURATION)
            .start()
}

fun View.animateInvisible() {
    this.animate()
            .alpha(Constants.View.FULL_GONE)
            .setDuration(Constants.View.DEFAULT_ANIMATION_DURATION)
            .start()
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

fun <T1 : Any, T2 : Any, R : Any> safeLet(p1: T1?, p2: T2?, block: (T1, T2) -> R?): R? {
    return if (p1 != null && p2 != null) block(p1, p2) else null
}

fun <T1 : Any, T2 : Any, T3 : Any, R : Any> safeLet(p1: T1?, p2: T2?, p3: T3?, block: (T1, T2, T3) -> R?): R? {
    return if (p1 != null && p2 != null && p3 != null) block(p1, p2, p3) else null
}

fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, R : Any> safeLet(p1: T1?, p2: T2?, p3: T3?, p4: T4?, block: (T1, T2, T3, T4) -> R?): R? {
    return if (p1 != null && p2 != null && p3 != null && p4 != null) block(p1, p2, p3, p4) else null
}

fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, T5 : Any, R : Any> safeLet(p1: T1?, p2: T2?, p3: T3?, p4: T4?, p5: T5?, block: (T1, T2, T3, T4, T5) -> R?): R? {
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


fun <V> Map<String, V>.toBundle(bundle: Bundle = Bundle()): Bundle = bundle.apply {
    forEach {
        val k = it.key
        val v = it.value
        when (v) {
            is IBinder -> putBinder(k, v)
            is Bundle -> putBundle(k, v)
            is Byte -> putByte(k, v)
            is ByteArray -> putByteArray(k, v)
            is Char -> putChar(k, v)
            is CharArray -> putCharArray(k, v)
            is CharSequence -> putCharSequence(k, v)
            is Float -> putFloat(k, v)
            is FloatArray -> putFloatArray(k, v)
            is Parcelable -> putParcelable(k, v)
            is Short -> putShort(k, v)
            is ShortArray -> putShortArray(k, v)

            else -> throw IllegalArgumentException("$v is of a type that is not currently supported")
        }
    }
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
                .withMultiline()
                .show()
    }
}

fun Activity.showSnackbar(@StringRes msg: Int, @ColorRes color: Int? = null, during: Int = Snackbar.LENGTH_LONG) {
    Snackbar.make(findViewById(android.R.id.content), getString(msg), during)
            .withColor(color)
            .withMultiline()
            .show()
}

fun View.showSnackbar(@StringRes msg: Int, @ColorRes color: Int? = null, during: Int = Snackbar.LENGTH_LONG) {
    if (context is Activity) {
        Snackbar.make(this, context.getString(msg), during)
                .withColor(color)
                .withMultiline()
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
            .withMultiline()
            .show()
}

fun showMessage(msg: String, view: View, @ColorRes color: Int? = null) {
    Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
            .withColor(color)
            .withMultiline()
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

fun Snackbar.withMultiline(line: Int = 8): Snackbar {
    val textView = view.findViewById<TextView>(android.support.design.R.id.snackbar_text)
    textView.maxLines = line
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
            bottom ?: lp.bottomMargin
    )

    layoutParams = lp
}

fun TextView.makeTextHalfBold(boldWholeValue: Boolean = false) {
    val value = this.text.toString().substringBefore(" ")
    val tokenName = if (text.indexOf(" ") != -1) {
        this.text.toString().substringAfter(" ")
    } else {
        ""
    }
    val str = SpannableStringBuilder(value)
    when {
        value.indexOf(".") != -1 && !boldWholeValue ->
            str.setSpan(StyleSpan(Typeface.BOLD), 0, value.indexOf("."),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        value.indexOf(" ") != -1 ->
            str.setSpan(StyleSpan(Typeface.BOLD), 0, value.indexOf(" "),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        else ->
            str.setSpan(StyleSpan(Typeface.BOLD), 0, value.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    this.text = str.append(" $tokenName")
}


fun find(assetId: String): AssetBalanceResponse? {
    return (queryFirst<AssetBalanceDb> { equalTo("assetId", assetId) })?.convertFromDb()
}

fun findByGatewayId(gatewayId: String): AssetBalanceResponse? { // ticker
    for (asset in EnvironmentManager.globalConfiguration.generalAssets) {
        if (asset.gatewayId.contains(gatewayId)) {
            return find(asset.assetId)
        }
    }
    return null
}

fun findInConstantsGeneralAssets(ticker: String): AssetBalanceResponse? {
    for (asset in listOf(Constants.MrtGeneralAsset, Constants.WctGeneralAsset, Constants.VstGeneralAsset)) {
        if (asset.gatewayId.contains(ticker)) {
            return find(asset.assetId)
        }
    }
    return null
}

fun AssetBalanceResponse.getMaxDigitsBeforeZero(): Int {
    return MoneyUtil.getScaledText(this.quantity ?: 0, this.getDecimals())
            .replace(",", "")
            .split(".")[0].length
}


fun AssetInfoResponse.getMaxDigitsBeforeZero(): Int {
    return MoneyUtil.getScaledText(this.quantity, this.precision)
            .replace(",", "")
            .split(".")[0].length
}

fun loadDbWavesBalance(): AssetBalanceResponse {
    return find(WavesConstants.WAVES_ASSET_ID_EMPTY)!!
}

fun getDeviceId(): String {
    return "android:${Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID)}"
}

fun Throwable.errorBody(): ErrorResponse? {
    return if (this is NetworkException) {
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
    return (prefsUtil.getValue(PrefsUtil.KEY_ENABLE_SPAM_FILTER, true)
            && isSpam(assetId))
}

fun isSpam(assetId: String?): Boolean {
    return (App.accessManager.isAuthenticated()
            && (null != queryFirst<SpamAssetDb> { equalTo("assetId", assetId) }))
}

fun AssetBalanceResponse.getItemType(): Int {
    return when {
        isSpam -> AssetsAdapter.TYPE_SPAM_ASSET
        isHidden -> AssetsAdapter.TYPE_HIDDEN_ASSET
        else -> AssetsAdapter.TYPE_ASSET
    }
}

fun restartApp() {
    val intent = Intent(App.appContext, com.wavesplatform.wallet.v2.ui.splash.SplashActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
    App.appContext.startActivity(intent)
}

fun Context.getLocalizedString(@StringRes id: Int, desiredLocale: Locale): String {
    val configuration = Configuration(resources.configuration)
    configuration.setLocale(desiredLocale)
    val localizedContext = createConfigurationContext(configuration)
    return localizedContext.resources.getString(id)
}

fun findAssetBalanceInDb(query: String?, list: List<AssetBalanceResponse>): List<AssetBalanceResponse> {
    return if (TextUtils.isEmpty(query)) {
        list.filter { !it.isSpam }
    } else {
        val queryLower = query!!.toLowerCase()
        list.filter { !it.isSpam }
                .filter {
                    it.assetId.toLowerCase().equals(queryLower)
                            || it.getName().toLowerCase().contains(queryLower)
                            || it.issueTransaction?.name?.toLowerCase()?.contains(queryLower) ?: false
                            || it.issueTransaction?.assetId?.toLowerCase()?.equals(queryLower) ?: false
                            || it.assetId == findByGatewayId(query.toUpperCase())?.assetId
                            || it.assetId == findInConstantsGeneralAssets(query.toUpperCase())?.assetId
                }
    }
}

fun isShowTicker(assetId: String?): Boolean {
    return assetId.isNullOrEmpty() || com.wavesplatform.wallet.v2.util.EnvironmentManager.globalConfiguration.generalAssets
            .plus(com.wavesplatform.wallet.v2.util.EnvironmentManager.globalConfiguration.assets)
            .any {
                it.assetId == assetId
            }
}

fun isFiat(assetId: String): Boolean {
    return Constants.defaultFiat().any { it == assetId }
}

fun isGateway(assetId: String): Boolean {
    return when {
        assetId.isWavesId() -> false
        Constants.defaultCrypto().any { it == assetId } -> true
        else -> false
    }
}

fun AssetPairOrderResponse.getStatus(): OrderStatus {
    return when (status) {
        AssetPairOrderResponse.API_STATUS_ACCEPTED -> OrderStatus.Accepted
        AssetPairOrderResponse.API_STATUS_PARTIALLY_FILLED -> OrderStatus.PartiallyFilled
        AssetPairOrderResponse.API_STATUS_CANCELLED -> OrderStatus.Cancelled
        AssetPairOrderResponse.API_STATUS_FILLED -> OrderStatus.Filled
        else -> OrderStatus.Filled
    }
}

fun AssetPairOrderResponse.getScaledFilled(amountAssetDecimals: Int?): String {
    val notScaledValue = if (getStatus() == OrderStatus.Filled) {
        amount
    } else {
        filled
    }
    return MoneyUtil.getTextStripZeros(MoneyUtil.getTextStripZeros(notScaledValue,
            amountAssetDecimals ?: 8))
}

fun AssetPairOrderResponse.getType(): OrderType {
    return type.getOrderType()
}

fun LastTradesResponse.DataResponse.ExchangeTransactionResponse.ExchangeOrderResponse.getType(): OrderType {
    return orderType.getOrderType()
}

fun OrderResponse.getType(): OrderType {
    return orderType.getOrderType()
}

fun String.getOrderType(): OrderType {
    return when (this) {
        WavesConstants.BUY_ORDER_TYPE -> OrderType.BUY
        WavesConstants.SELL_ORDER_TYPE -> OrderType.SELL
        else -> OrderType.BUY
    }
}

fun addressByPublicKey(publicKey: String): String {
    return WavesCrypto.addressFromPublicKey(
            WavesCrypto.base58decode(publicKey), EnvironmentManager.netCode)
}

fun getTransactionType(transaction: BaseTransaction, address: String, spam: Set<String>?): Int {
    val sender = addressByPublicKey(transaction.senderPublicKey)
    when {
        transaction.type == BaseTransaction.TRANSFER -> {
            transaction as TransferTransaction
            if (sender == address) {
                if (sender == transaction.recipient) {
                    return Constants.ID_SELF_TRANSFER_TYPE
                }
                return Constants.ID_SENT_TYPE
            } else {
                if (spam != null && spam.isNotEmpty() && spam.contains(transaction.assetId)) {
                    return Constants.ID_SPAM_RECEIVE_TYPE
                }
                if (transaction.recipient != address) {
                    return Constants.ID_RECEIVE_SPONSORSHIP_TYPE
                }
                return Constants.ID_RECEIVED_TYPE
            }
        }
        transaction.type == BaseTransaction.MASS_TRANSFER -> {
            transaction as MassTransferTransaction
            return if (sender == address) {
                Constants.ID_MASS_SEND_TYPE
            } else {
                if (spam != null && spam.isNotEmpty() && spam.contains(transaction.assetId)) {
                    Constants.ID_MASS_SPAM_RECEIVE_TYPE
                } else {
                    Constants.ID_MASS_RECEIVE_TYPE
                }
            }
        }
        transaction.type == BaseTransaction.CANCEL_LEASING -> {
            transaction as LeaseCancelTransaction
            return if (sender == address) {
                if (transaction.leaseId.isEmpty()) {
                    Constants.ID_UNRECOGNISED_TYPE
                } else {
                    Constants.ID_CANCELED_LEASING_TYPE
                }
            } else {
                Constants.ID_RECEIVED_TYPE
            }
        }
        transaction.type == BaseTransaction.EXCHANGE -> return Constants.ID_EXCHANGE_TYPE
        transaction.type == BaseTransaction.ISSUE -> return Constants.ID_TOKEN_GENERATION_TYPE
        transaction.type == BaseTransaction.BURN -> return Constants.ID_TOKEN_BURN_TYPE
        transaction.type == BaseTransaction.REISSUE -> return Constants.ID_TOKEN_REISSUE_TYPE
        transaction.type == BaseTransaction.CREATE_ALIAS -> return Constants.ID_CREATE_ALIAS_TYPE
        transaction.type == BaseTransaction.CREATE_LEASING -> {
            transaction as LeaseTransaction
            return if (transaction.recipient == address) {
                Constants.ID_INCOMING_LEASING_TYPE
            } else {
                Constants.ID_STARTED_LEASING_TYPE
            }
        }
        transaction.type == BaseTransaction.DATA -> return Constants.ID_DATA_TYPE
        transaction.type == BaseTransaction.ADDRESS_SCRIPT -> {
            transaction as SetScriptTransaction
            return if (transaction.script == null) {
                Constants.ID_CANCEL_ADDRESS_SCRIPT_TYPE
            } else {
                Constants.ID_SET_ADDRESS_SCRIPT_TYPE
            }
        }
        transaction.type == BaseTransaction.SPONSORSHIP -> {
            transaction as SponsorshipTransaction
            return if (transaction.minSponsoredAssetFee == null) {
                Constants.ID_CANCEL_SPONSORSHIP_TYPE
            } else {
                Constants.ID_SET_SPONSORSHIP_TYPE
            }
        }
        transaction.type == BaseTransaction.ASSET_SCRIPT -> return Constants.ID_UPDATE_ASSET_SCRIPT_TYPE
        transaction.type == BaseTransaction.SCRIPT_INVOCATION -> return Constants.ID_SCRIPT_INVOCATION_TYPE
        else -> return Constants.ID_UNRECOGNISED_TYPE
    }
}

fun getTransactionType(transaction: HistoryTransactionResponse, address: String): Int =
        if (transaction.type == BaseTransaction.TRANSFER &&
                transaction.sender != address &&
                transaction.asset?.isSpam == true) {
            Constants.ID_SPAM_RECEIVE_TYPE
        } else if (transaction.type == BaseTransaction.TRANSFER &&
                transaction.sender != address &&
                transaction.recipientAddress != address) {
            Constants.ID_RECEIVE_SPONSORSHIP_TYPE
        } else if (transaction.type == BaseTransaction.MASS_TRANSFER &&
                transaction.sender != address &&
                transaction.asset?.isSpam == true) {
            Constants.ID_MASS_SPAM_RECEIVE_TYPE
        } else if (transaction.type == BaseTransaction.CANCEL_LEASING &&
                !transaction.leaseId.isNullOrEmpty()) {
            Constants.ID_CANCELED_LEASING_TYPE
        } else if ((transaction.type == BaseTransaction.TRANSFER || transaction.type == BaseTransaction.CANCEL_LEASING) &&
                transaction.sender != address) {
            Constants.ID_RECEIVED_TYPE
        } else if (transaction.type == BaseTransaction.TRANSFER &&
                transaction.sender == transaction.recipientAddress) {
            Constants.ID_SELF_TRANSFER_TYPE
        } else if (transaction.type == BaseTransaction.TRANSFER &&
                transaction.sender == address) {
            Constants.ID_SENT_TYPE
        } else if (transaction.type == BaseTransaction.CREATE_LEASING &&
                transaction.recipientAddress != address) {
            Constants.ID_STARTED_LEASING_TYPE
        } else if (transaction.type == BaseTransaction.EXCHANGE) {
            Constants.ID_EXCHANGE_TYPE
        } else if (transaction.type == BaseTransaction.ISSUE) {
            Constants.ID_TOKEN_GENERATION_TYPE
        } else if (transaction.type == BaseTransaction.BURN) {
            Constants.ID_TOKEN_BURN_TYPE
        } else if (transaction.type == BaseTransaction.REISSUE) {
            Constants.ID_TOKEN_REISSUE_TYPE
        } else if (transaction.type == BaseTransaction.CREATE_ALIAS) {
            Constants.ID_CREATE_ALIAS_TYPE
        } else if (transaction.type == BaseTransaction.CREATE_LEASING &&
                transaction.recipientAddress == address) {
            Constants.ID_INCOMING_LEASING_TYPE
        } else if (transaction.type == BaseTransaction.MASS_TRANSFER &&
                transaction.sender == address) {
            Constants.ID_MASS_SEND_TYPE
        } else if (transaction.type == BaseTransaction.MASS_TRANSFER &&
                transaction.sender != address) {
            Constants.ID_MASS_RECEIVE_TYPE
        } else if (transaction.type == BaseTransaction.DATA) {
            Constants.ID_DATA_TYPE
        } else if (transaction.type == BaseTransaction.ADDRESS_SCRIPT) {
            if (transaction.script == null) {
                Constants.ID_CANCEL_ADDRESS_SCRIPT_TYPE
            } else {
                Constants.ID_SET_ADDRESS_SCRIPT_TYPE
            }
        } else if (transaction.type == BaseTransaction.SPONSORSHIP) {
            if (transaction.minSponsoredAssetFee == null) {
                Constants.ID_CANCEL_SPONSORSHIP_TYPE
            } else {
                Constants.ID_SET_SPONSORSHIP_TYPE
            }
        } else if (transaction.type == BaseTransaction.ASSET_SCRIPT) {
            Constants.ID_UPDATE_ASSET_SCRIPT_TYPE
        } else if (transaction.type == BaseTransaction.SCRIPT_INVOCATION) {
            Constants.ID_SCRIPT_INVOCATION_TYPE
        } else {
            Constants.ID_UNRECOGNISED_TYPE
        }

fun getTransactionAmount(transaction: HistoryTransactionResponse, decimals: Int = 8, round: Boolean = true): String {

    var sign = "-"
    if (transaction.transactionType() == TransactionType.MASS_SPAM_RECEIVE_TYPE ||
            transaction.transactionType() == TransactionType.MASS_RECEIVE_TYPE) {
        sign = "+"
    }

    return sign + if (transaction.transfers.isNotEmpty()) {
        val sumString = if (round) {
            getScaledAmount(transaction.transfers.sumByLong { it.amount }, decimals)
        } else {
            MoneyUtil.getScaledText(
                    transaction.transfers.sumByLong { it.amount }, transaction.asset)
                    .stripZeros()
        }
        if (sumString.isEmpty()) {
            ""
        } else {
            sumString
        }
    } else {
        if (round) {
            getScaledAmount(transaction.amount, decimals)
        } else {
            MoneyUtil.getScaledText(transaction.amount, transaction.asset).stripZeros()
        }
    }
}

fun HistoryTransactionResponse.isSponsorshipTransaction(): Boolean {
    return transactionType() == TransactionType.RECEIVE_SPONSORSHIP_TYPE ||
            transactionType() == TransactionType.CANCEL_SPONSORSHIP_TYPE
}

fun HistoryTransactionResponse.transactionType(): TransactionType {
    return TransactionType.getTypeById(this.transactionTypeId)
}

fun String?.isValidVostokAddress(): Boolean {
    if (this.isNullOrEmpty()) return false
    return try {
        val bytes = WavesCrypto.base58decode(this)
        if (bytes.size == WavesCrypto.ADDRESS_LENGTH &&
                bytes[0] == WavesCrypto.ADDRESS_VERSION &&
                bytes[1] == EnvironmentManager.vostokNetCode) {
            val checkSum = Arrays.copyOfRange(bytes,
                    bytes.size - WavesCrypto.CHECK_SUM_LENGTH, bytes.size)
            val checkSumGenerated = calcCheckSum(
                    bytes.copyOf(bytes.size - WavesCrypto.CHECK_SUM_LENGTH))
            Arrays.equals(checkSum, checkSumGenerated)
        } else {
            false
        }
    } catch (e: Exception) {
        false
    }
}

fun String?.isValidErgoAddress(): Boolean {
    if (this.isNullOrEmpty()) {
        return false
    }
    return this.matches(Regex("^9[a-km-zA-HJ-NP-Z1-9]{5,}"))
}

fun mapCorrectPairs(settingsIdsPairs: List<String>, pairs: List<Pair<String, String>>)
        : List<Pair<String, String>> {
    return pairs.map { pair ->
        correctPair(settingsIdsPairs, pair)
    }
}

fun correctPair(settingsIdsPairs: List<String>, pair: Pair<String, String>): Pair<String, String> {
    val amountIndex = settingsIdsPairs.indexOf(pair.first)
    val priceIndex = settingsIdsPairs.indexOf(pair.second)

    val amount: String
    val price: String

    val isFirstInList = amountIndex != -1
    val isSecondInList = priceIndex != -1

    if (isFirstInList && isSecondInList) {
        if (amountIndex > priceIndex) {
            amount = pair.first
            price = pair.second
        } else {
            amount = pair.second
            price = pair.first
        }
    } else if (isFirstInList && !isSecondInList) {
        amount = pair.second
        price = pair.first
    } else if (!isFirstInList && isSecondInList) {
        amount = pair.first
        price = pair.second
    } else {
        val amountBytes = WavesCrypto.base58decode(input = pair.first).toHexString()
        val priceBytes = WavesCrypto.base58decode(input = pair.second).toHexString()

        if (amountBytes > priceBytes) {
            amount = pair.first
            price = pair.second
        } else {
            amount = pair.second
            price = pair.first
        }
    }
    return Pair(amount, price)
}

private fun ByteArray.toHexString(): String {
    return this.joinToString("") {
        java.lang.String.format("%02x", it)
    }
}