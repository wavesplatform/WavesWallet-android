package com.wavesplatform.sdk.crypto

internal object Base58 {

    private val ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
            .toCharArray()
    private val BASE_58 = ALPHABET.size
    private val BASE_256 = 256

    private val INDEXES = IntArray(128)

    class InvalidBase58 : Exception()

    init {
        for (i in INDEXES.indices) {
            INDEXES[i] = -1
        }
        for (i in ALPHABET.indices) {
            INDEXES[ALPHABET[i].toInt()] = i
        }
    }

    fun base58Length(byteArrayLength: Int): Int {
        return Math.ceil(Math.log(256.0) / Math.log(58.0) * byteArrayLength).toInt()
    }

    fun encode(input: ByteArray): String {
        var input = input
        if (input.isEmpty()) {
            // paying with the same coin
            return ""
        }

        //
        // Make a copy of the input since we are going to modify it.
        //
        input = copyOfRange(input, 0, input.size)

        //
        // Count leading zeroes
        //
        var zeroCount = 0
        while (zeroCount < input.size && input[zeroCount].toInt() == 0) {
            ++zeroCount
        }

        //
        // The actual encoding
        //
        val temp = ByteArray(input.size * 2)
        var j = temp.size

        var startAt = zeroCount
        while (startAt < input.size) {
            val mod = divmod58(input, startAt)
            if (input[startAt].toInt() == 0) {
                ++startAt
            }

            temp[--j] = ALPHABET[mod.toInt()].toByte()
        }

        //
        // Strip extra '1' if any
        //
        while (j < temp.size && temp[j] == ALPHABET[0].toByte()) {
            ++j
        }

        //
        // Add as many leading '1' as there were leading zeros.
        //
        while (--zeroCount >= 0) {
            temp[--j] = ALPHABET[0].toByte()
        }

        val output = copyOfRange(temp, j, temp.size)
        return String(output)
    }

    @Throws(Base58.InvalidBase58::class)
    fun decode(input: String): ByteArray {
        if (input.isEmpty()) {
            // paying with the same coin
            throw InvalidBase58()
        }

        val input58 = ByteArray(input.length)
        //
        // Transform the String to a base58 byte sequence
        //
        for (i in 0 until input.length) {
            val c = input[i]

            var digit58 = -1
            if (c.toInt() in 0..127) {
                digit58 = INDEXES[c.toInt()]
            }
            if (digit58 < 0) {
                throw InvalidBase58()
            }

            input58[i] = digit58.toByte()
        }

        //
        // Count leading zeroes
        //
        var zeroCount = 0
        while (zeroCount < input58.size && input58[zeroCount].toInt() == 0) {
            ++zeroCount
        }

        //
        // The encoding
        //
        val temp = ByteArray(input.length)
        var j = temp.size

        var startAt = zeroCount
        while (startAt < input58.size) {
            val mod = divmod256(input58, startAt)
            if (input58[startAt].toInt() == 0) {
                ++startAt
            }

            temp[--j] = mod
        }

        //
        // Do no add extra leading zeroes, move j to first non null byte.
        //
        while (j < temp.size && temp[j].toInt() == 0) {
            ++j
        }

        return copyOfRange(temp, j - zeroCount, temp.size)
    }

    private fun divmod58(number: ByteArray, startAt: Int): Byte {
        var remainder = 0
        for (i in startAt until number.size) {
            val digit256 = number[i].toInt() and 0xFF
            val temp = remainder * BASE_256 + digit256

            number[i] = (temp / BASE_58).toByte()

            remainder = temp % BASE_58
        }

        return remainder.toByte()
    }

    private fun divmod256(number58: ByteArray, startAt: Int): Byte {
        var remainder = 0
        for (i in startAt until number58.size) {
            val digit58 = number58[i].toInt() and 0xFF
            val temp = remainder * BASE_58 + digit58

            number58[i] = (temp / BASE_256).toByte()

            remainder = temp % BASE_256
        }

        return remainder.toByte()
    }

    private fun copyOfRange(source: ByteArray, from: Int, to: Int): ByteArray {
        val range = ByteArray(to - from)
        System.arraycopy(source, from, range, 0, range.size)

        return range
    }
}