package com.wavesplatform.wallet.v2.util

import android.text.InputFilter
import android.text.Spanned

class DecimalDigitsInputFilter(private val mMaxIntegerDigitsLength: Int,
                               private val mMaxDigitsAfterLength: Int,
                               private val mMax: Double) : InputFilter {

    override fun filter(source: CharSequence,
                        start: Int,
                        end: Int,
                        dest: Spanned,
                        dStart: Int,
                        dEnd: Int): CharSequence? {
        val allText = getAllText(source, dest, dStart)
        val onlyDigitsText = getOnlyDigitsPart(allText)
        if (allText.isEmpty()) {
            return null
        } else {
            val enteredValue: Double
            try {
                enteredValue = java.lang.Double.parseDouble(onlyDigitsText)
            } catch (e: NumberFormatException) {
                return ""
            }
            return checkMaxValueRule(enteredValue, onlyDigitsText)
        }
    }


    private fun checkMaxValueRule(enteredValue: Double, onlyDigitsText: String): CharSequence? {
        return if (enteredValue > mMax) {
            ""
        } else {
            handleInputRules(onlyDigitsText)
        }
    }

    private fun handleInputRules(onlyDigitsText: String): CharSequence? {
        return if (isDecimalDigit(onlyDigitsText)) {
            checkRuleForDecimalDigits(onlyDigitsText)
        } else {
            checkRuleForIntegerDigits(onlyDigitsText.length)
        }
    }

    private fun isDecimalDigit(onlyDigitsText: String): Boolean {
        return onlyDigitsText.contains(DOT)
    }

    private fun checkRuleForDecimalDigits(onlyDigitsPart: String): CharSequence? {
        val afterDotPart = onlyDigitsPart
                .substring(onlyDigitsPart.indexOf(DOT), onlyDigitsPart.length - 1)
        return if (afterDotPart.length > mMaxDigitsAfterLength) {
            ""
        } else {
            null
        }
    }

    private fun checkRuleForIntegerDigits(allTextLength: Int): CharSequence? {
        return if (allTextLength > mMaxIntegerDigitsLength) {
            ""
        } else null
    }

    private fun getOnlyDigitsPart(text: String): String {
        return text.replace("[^0-9?!\\.]".toRegex(), "")
    }

    private fun getAllText(source: CharSequence, dest: Spanned, dStart: Int): String {
        var allText = ""
        if (dest.toString().isNotEmpty()) {
            allText = if (source.toString().isEmpty()) {
                deleteCharAtIndex(dest, dStart)
            } else {
                StringBuilder(dest).insert(dStart, source).toString()
            }
        }
        return allText
    }

    private fun deleteCharAtIndex(dest: Spanned, dStart: Int): String {
        val builder = StringBuilder(dest)
        if (dStart > 0 && dStart <= builder.lastIndex) {
            builder.deleteCharAt(dStart)
        }
        return builder.toString()
    }

    companion object {
        private const val DOT = "."
    }
}