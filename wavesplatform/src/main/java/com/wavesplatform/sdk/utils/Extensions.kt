package com.wavesplatform.sdk.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.*
import android.text.format.DateUtils
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.google.common.primitives.Bytes
import com.google.common.primitives.Shorts
import com.wavesplatform.sdk.Constants
import com.wavesplatform.sdk.model.response.*
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*

val filterStartWithDot = InputFilter { source, start, end, dest, dstart, dend ->
    if (dest.isNullOrEmpty() && source.startsWith(".")){
        return@InputFilter "0."
    }
    null
}

/**
 * Created by anonymous on 13.09.17.
 */

fun EditText.applyFilterStartWithDot(){
    this.filters = arrayOf(filterStartWithDot)
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
    return this.stripZeros().replace(",", "")
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


fun Activity.openUrlWithIntent(url: String) {
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(browserIntent)
}

fun Transaction.transactionType(): TransactionType {
    return TransactionType.getTypeById(this.transactionTypeId)
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


fun String.stripZeros(): String {
    if (this == "0.0") return this
    return if (!this.contains(".")) this else this.replace("0*$".toRegex(), "").replace("\\.$".toRegex(), "")
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
    if (withoutAnimation) {
        activity?.overridePendingTransition(0, 0)
        // todo activity?.overridePendingTransition(R.anim.start_new_show,  R.anim.start_current_hide)
    }
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
        value >= MoneyUtil.ONE_M -> value.divide(MoneyUtil.ONE_M, 1, RoundingMode.HALF_EVEN)
                .toPlainString().stripZeros() + "M"
        value >= MoneyUtil.ONE_K -> value.divide(MoneyUtil.ONE_K, 1, RoundingMode.HALF_EVEN)
                .toPlainString().stripZeros() + "k"
        else -> MoneyUtil.createFormatter(decimals).format(BigDecimal.valueOf(absAmount, decimals))
                .stripZeros() + ""
    }
}