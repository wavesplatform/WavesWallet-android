/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.utils

import com.wavesplatform.sdk.model.response.AssetBalanceResponse
import com.wavesplatform.sdk.model.response.AssetInfoResponse
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class MoneyUtil private constructor() {
    private val wavesFormat: DecimalFormat
    private val formatsMap: MutableList<DecimalFormat>

    init {
        wavesFormat = createFormatter(8)
        formatsMap = ArrayList()
        for (i in 0..8) {
            formatsMap.add(createFormatter(i))
        }
    }

    fun getFormatter(decimals: Int): DecimalFormat {
        return formatsMap[decimals]
    }

    companion object {

        val ONE_B = BigDecimal(1000000000)
        val ONE_M = BigDecimal(1000000)
        val ONE_K = BigDecimal(1000)

        private val instance = MoneyUtil()

        const val DEFAULT_SEPARATOR_THIN_SPACE = '\u2009'
        const val DEFAULT_SEPARATOR_COMMA = ','

        fun createFormatter(decimals: Int): DecimalFormat {
            val formatter = NumberFormat.getInstance(Locale.US) as DecimalFormat
            formatter.maximumFractionDigits = decimals
            formatter.minimumFractionDigits = decimals
            formatter.isParseBigDecimal = true
            return formatter
        }

        fun get(): MoneyUtil {
            return instance
        }

        fun getScaledPrice(amount: Long, amountDecimals: Int, priceDecimals: Int): String {
            return get().getFormatter(priceDecimals).format(BigDecimal.valueOf(amount, 8 + priceDecimals - amountDecimals))
        }

        fun getScaledText(amount: Long, decimals: Int): String {
            return try {
                get().getFormatter(decimals).format(
                        BigDecimal.valueOf(amount, decimals))
            } catch (e: Exception) {
                get().getFormatter(8).format(
                        BigDecimal.valueOf(amount, decimals))
            }

        }

        fun getTextStripZeros(amount: String): String {
            if (amount == "0.0") return amount
            return if (!amount.contains(".")) amount else amount.replace("0*$".toRegex(), "").replace("\\.$".toRegex(), "")
        }

        fun getTextStripZeros(amount: Long, decimals: Int): String {
            val formatter = NumberFormat.getInstance(Locale.US) as DecimalFormat
            formatter.maximumFractionDigits = decimals
            formatter.minimumFractionDigits = 1
            formatter.isParseBigDecimal = true
            return formatter.format(
                    BigDecimal.valueOf(amount, decimals))
        }

        fun getFormattedTotal(amount: Double, decimals: Int): String {
            val formatter = NumberFormat.getInstance(Locale.US) as DecimalFormat
            formatter.maximumFractionDigits = decimals
            formatter.minimumFractionDigits = 0
            formatter.isParseBigDecimal = true
            return formatter.format(amount)
        }

        fun getScaledText(amount: Long?, ab: AssetBalanceResponse?): String {
            return getScaledText(amount!!, ab?.getDecimals() ?: 8)
        }

        fun getScaledText(amount: Long?, assetInfo: AssetInfoResponse?): String {
            return getScaledText(amount!!, assetInfo?.precision ?: 8)
        }

        fun getDisplayWaves(amount: Long): String {
            return get().wavesFormat.format(BigDecimal.valueOf(amount, 8))
        }

        fun getUnscaledValue(amount: String, ab: AssetBalanceResponse?): Long {
            return getUnscaledValue(amount, ab?.getDecimals() ?: 8)
        }

        fun getUnscaledValue(amount: String?, decimals: Int): Long {
            return getUnscaledValue(amount, decimals, RoundingMode.HALF_EVEN)
        }

        fun getUnscaledValue(amount: String?, decimals: Int, roundingMod: RoundingMode): Long {
            if (amount == null)
                return 0L
            return try {
                val value = get().getFormatter(decimals).parse(amount)
                (value as? BigDecimal)?.setScale(decimals,
                        roundingMod)?.unscaledValue()?.toLong() ?: 0L
            } catch (ex: Exception) {
                0L
            }
        }
    }
}
