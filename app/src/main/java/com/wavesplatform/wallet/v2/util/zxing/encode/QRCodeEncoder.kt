/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.util.zxing.encode

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.telephony.PhoneNumberUtils

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.wavesplatform.wallet.v2.util.zxing.Contents

import java.util.EnumMap

class QRCodeEncoder(data: String?, bundle: Bundle?, type: String, format: String, dimension: Int) {

    private var dimension = Integer.MIN_VALUE
    var contents: String? = null
        private set
    var displayContents: String? = null
        private set
    var title: String? = null
        private set
    private var format: BarcodeFormat? = null
    private var encoded = false

    init {
        this.dimension = dimension
        encoded = encodeContents(data, bundle, type, format)
    }

    private fun encodeContents(data: String?, bundle: Bundle?, type: String, formatString: String?): Boolean {
        // Default to QR_CODE if no format given.
        format = null
        if (formatString != null) {
            try {
                format = BarcodeFormat.valueOf(formatString)
            } catch (iae: IllegalArgumentException) {
                // Ignore it then
            }

        }
        if (format == null || format == BarcodeFormat.QR_CODE) {
            this.format = BarcodeFormat.QR_CODE
            encodeQRCodeContents(data, bundle, type)
        } else if (data != null && data.length > 0) {
            contents = data
            displayContents = data
            title = "Text"
        }
        return contents != null && contents!!.length > 0
    }

    private fun encodeQRCodeContents(data: String?, bundle: Bundle?, type: String) {
        var data = data
        when (type) {
            Contents.Type.TEXT -> if (data != null && data.isNotEmpty()) {
                contents = data
                displayContents = data
                title = "Text"
            }
            Contents.Type.EMAIL -> {
                data = trim(data)
                if (data != null) {
                    contents = "mailto:$data"
                    displayContents = data
                    title = "E-Mail"
                }
            }
            Contents.Type.PHONE -> {
                data = trim(data)
                if (data != null) {
                    contents = "tel:$data"
                    displayContents = PhoneNumberUtils.formatNumber(data)
                    title = "Phone"
                }
            }
            Contents.Type.SMS -> {
                data = trim(data)
                if (data != null) {
                    contents = "sms:$data"
                    displayContents = PhoneNumberUtils.formatNumber(data)
                    title = "SMS"
                }
            }
            Contents.Type.LOCATION -> if (bundle != null) {
                // These must use Bundle.getFloat(), not getDouble(), it's part of the API.
                val latitude = bundle.getFloat("LAT", java.lang.Float.MAX_VALUE)
                val longitude = bundle.getFloat("LONG", java.lang.Float.MAX_VALUE)
                if (latitude != java.lang.Float.MAX_VALUE && longitude != java.lang.Float.MAX_VALUE) {
                    contents = "geo:$latitude,$longitude"
                    displayContents = "$latitude,$longitude"
                    title = "Location"
                }
            }
        }
    }

    @Throws(WriterException::class)
    fun encodeAsBitmap(): Bitmap? {
        if (!encoded) return null

        var hints: MutableMap<EncodeHintType, Any>? = null
        val encoding = guessAppropriateEncoding(contents!!)
        if (encoding != null) {
            hints = EnumMap(EncodeHintType::class.java)
            hints[EncodeHintType.CHARACTER_SET] = encoding
        }
        val writer = MultiFormatWriter()
        val result = writer.encode(contents, format!!, dimension, dimension, hints)
        val width = result.width
        val height = result.height
        val pixels = IntArray(width * height)
        // All are 0, or black, by default
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (result.get(x, y)) BLACK else Color.TRANSPARENT
            }
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    companion object {
        private val WHITE = -0x1
        private val BLACK = -0x1000000

        private fun guessAppropriateEncoding(contents: CharSequence): String? {
            // Very crude at the moment
            for (i in 0 until contents.length) {
                if (contents[i].toInt() > 0xFF) {
                    return "UTF-8"
                }
            }
            return null
        }

        private fun trim(s: String?): String? {
            if (s == null) {
                return null
            }
            val result = s.trim { it <= ' ' }
            return if (result.length == 0) null else result
        }

        private fun escapeMECARD(input: String?): String? {
            if (input == null || input.indexOf(':') < 0 && input.indexOf(';') < 0) {
                return input
            }
            val length = input.length
            val result = StringBuilder(length)
            for (i in 0 until length) {
                val c = input[i]
                if (c == ':' || c == ';') {
                    result.append('\\')
                }
                result.append(c)
            }
            return result.toString()
        }
    }
}
