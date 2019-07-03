package com.wavesplatform.sdk.crypto

import org.junit.Assert
import org.junit.Test

class WavesCryptoTest {

    @Test
    fun addressBySeed() {
        Assert.assertEquals(ADDRESS, WavesCrypto.addressBySeed(SEED, TEST_NET_CHAIN_ID))
    }

    @Test
    fun publicKey() {
        Assert.assertEquals(WavesCrypto.publicKey(SEED), PUBLIC_KEY)
    }

    @Test
    fun privateKey() {
        Assert.assertEquals(WavesCrypto.privateKey(SEED), PRIVATE_KEY)
    }

    @Test
    fun keyPair() {
        val keyPair = WavesCrypto.keyPair(SEED)
        Assert.assertEquals(WavesCrypto.publicKey(SEED), keyPair.publicKey)
        Assert.assertEquals(WavesCrypto.privateKey(SEED), keyPair.privateKey)
    }

    @Test
    fun addressByPublicKey() {
        Assert.assertEquals(ADDRESS, WavesCrypto.addressByPublicKey(PUBLIC_KEY, TEST_NET_CHAIN_ID))
    }

    @Test
    fun randomSeed() {
        val randomSeed = WavesCrypto.randomSeed()
        Assert.assertEquals(randomSeed.matches("[a-zA-Z ]+".toRegex()), true)
        Assert.assertEquals(randomSeed.length > 25, true)
        val words = randomSeed.split(" ")
        Assert.assertEquals(words.size, 15)
    }

    @Test
    fun verifySignature() {
        Assert.assertEquals(WavesCrypto.verifySignature(
            PUBLIC_KEY,
            RANDOM_BYTES,
            WavesCrypto.signBytesWithPrivateKey(RANDOM_BYTES, PRIVATE_KEY)), true)

        Assert.assertEquals(WavesCrypto.verifySignature(
            PUBLIC_KEY,
            RANDOM_BYTES,
            WavesCrypto.signBytesWithSeed(RANDOM_BYTES, SEED)), true)
    }

    @Test
    fun verifyPublicKey() {
        Assert.assertEquals(WavesCrypto.verifyPublicKey(PUBLIC_KEY), true)
    }

    @Test
    fun verifyAddress() {
        Assert.assertEquals(WavesCrypto.verifyAddress(ADDRESS, TEST_NET_CHAIN_ID, PUBLIC_KEY), true)
    }

    @Test
    fun base58() {
        Assert.assertEquals(WavesCrypto.base58decode(WavesCrypto.base58encode(RANDOM_BYTES)).contentEquals(RANDOM_BYTES),
            true)
        Assert.assertEquals(WavesCrypto.base58encode(WavesCrypto.base58decode(ADDRESS)), ADDRESS)
    }

    @Test
    fun base64() {
        Assert.assertEquals(WavesCrypto.base64decode(WavesCrypto.base64encode(RANDOM_BYTES)).contentEquals(RANDOM_BYTES),
            true)
        Assert.assertEquals(WavesCrypto.base64encode(WavesCrypto.base64decode("base64$RANDOM_STRING=")),
            "base64$RANDOM_STRING")
    }

    companion object {
        const val SEED =
            "chronic comic else cool seat filter amount banner bottom spice cup figure exact elephant copper"
        const val ADDRESS = "3MxZem69rYm4osMoUo5KLKq89nYoMMhi29e"
        const val PUBLIC_KEY = "HPoP3v8pj1yRJZdvXzibGV1RoCbuEzRnxAxJ24a81hFJ"
        const val PRIVATE_KEY = "4HimGuCggEJ7m19aGMGLBVsqyaSnsk1zsjLWiFimQM3Q"
        const val TEST_NET_CHAIN_ID = WavesCrypto.TEST_NET_CHAIN_ID.toString()
        val RANDOM_BYTES = byteArrayOf(56, 127, 57, -24, 0, 77, 33, -14, 14, 69, 55, 5, 110, -1, 12)
        val RANDOM_STRING = "slll000OOOjdgnlfsjdgnsqwertyulslkdmzxcvbnm"
    }
}