package com.wavesplatform.sdk.crypto

import com.wavesplatform.sdk.utils.*
import org.apache.commons.codec.binary.Base64
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

typealias Bytes = ByteArray
typealias PublicKey = String
typealias PrivateKey = String
typealias Seed = String
typealias Address = String

const val PUBLIC_KEY_LENGTH = 32
const val PRIVATE_KEY_LENGTH = 32
const val SIGNATURE_LENGTH = 64

const val MAIN_NET_CHAIN_ID = 87
const val TEST_NET_CHAIN_ID = 84

interface KeyPair {
    val publicKey: PublicKey
    val privateKey: PrivateKey
}

interface WavesCrypto {

    /**
     * BLAKE2 are cryptographic hash function
     *
     * @param input byte array of input data
     * @return byte array of hash values
     */
    fun blake2b(input: Bytes): Bytes
    /**
     * Keccak are secure hash algorithm
     *
     * @param input byte array of input data
     * @return byte array of hash values
     */
    fun keccak(input: Bytes): Bytes
    /**
     * SHA-256 are cryptographic hash function
     *
     * @param input byte array of input data
     * @return byte array of hash values
     */
    fun sha256(input: Bytes): Bytes

    /**
     * Base58 binary-to-text function used to represent large integers as alphanumeric text.
     * Compared to Base64, the following similar-looking letters are omitted:
     * 0 (zero), O (capital o), I (capital i) and l (lower case L) as well
     * as the non-alphanumeric characters + (plus) and / (slash)
     *
     * @param input byte array to encode
     * @return encoded string
     */
    fun base58encode(input: Bytes): String

    /**
     * Base58 text-to-binary function used to restore data encoded by Base58 @see WavesCrypto.base58encode
     *
     * @param input encoded Base58 string
     * @return decoded byte array
     */
    fun base58decode(input: String): Bytes
    /**
     * SHA-256 are cryptographic hash function
     * @param input byte array of input data
     * @return byte array of hash values
     */
    fun base64encode(input: Bytes): String
    /**
     * SHA-256 are cryptographic hash function
     * @param input byte array of input data
     * @return byte array of hash values
     */
    fun base64decode(input: String): Bytes

    fun keyPair(seed: Seed): KeyPair
    fun publicKey(seed: Seed): PublicKey
    fun privateKey(seed: Seed): PrivateKey

    fun addressByPublicKey(publicKey: PublicKey, chainId: String?): Address
    fun addressBySeed(seed: Seed, chainId: String?): Address

    fun randomSeed(): Seed

    fun signBytesWithPrivateKey(bytes: Bytes, privateKey: PrivateKey): Bytes
    fun signBytesWithSeed(bytes: Bytes, seed: Seed): Bytes

    fun verifySignature(publicKey: PublicKey, bytes: Bytes, signature: Bytes): Boolean
    fun verifyPublicKey(publicKey: PublicKey): Boolean
    fun verifyAddress(address: Address, chainId: String? = null, publicKey: PublicKey? = null): Boolean

    companion object : WavesCrypto {

        override fun blake2b(input: Bytes): Bytes {
            return Hash.blake2b(input)
        }

        override fun keccak(input: Bytes): Bytes {
            return Hash.keccak(input)
        }

        override fun sha256(input: Bytes): Bytes {
            return Hash.sha256(input)
        }

        override fun base58encode(input: Bytes): String {
            return Base58.encode(input)
        }

        override fun base58decode(input: String): Bytes {
            return Base58.decode(input)
        }

        override fun base64encode(input: Bytes): String {
            return Base64.encodeBase64String(input)
        }

        override fun base64decode(input: String): Bytes {
            return Base64.decodeBase64(input)
        }

        override fun keyPair(seed: Seed): KeyPair {
            val account = PrivateKeyAccount(seed.toByteArray(Charsets.UTF_8))
            return object : KeyPair {
                override val publicKey: PublicKey
                    get() = account.publicKeyStr
                override val privateKey: PrivateKey
                    get() = account.privateKeyStr
            }
        }

        override fun publicKey(seed: Seed): PublicKey {
            val account = PrivateKeyAccount(seed.toByteArray(Charsets.UTF_8))
            return account.publicKeyStr
        }

        override fun privateKey(seed: Seed): PrivateKey {
            val account = PrivateKeyAccount(seed.toByteArray(Charsets.UTF_8))
            return account.privateKeyStr
        }

        override fun addressByPublicKey(publicKey: PublicKey, chainId: String?): Address {
            return when {
                chainId == null ->
                    addressFromPublicKey(Base58.decode(publicKey))
                chainId.length == 1 ->
                    addressFromPublicKey(Base58.decode(publicKey), chainId[0].toByte())
                else ->
                    "Unknown address"
            }
        }

        override fun addressBySeed(seed: Seed, chainId: String?): Address {
            return addressByPublicKey(publicKey(seed), chainId)
        }

        override fun randomSeed(): Seed {
            return WalletManager.createWalletSeed()
        }

        override fun signBytesWithPrivateKey(bytes: Bytes, privateKey: PrivateKey): Bytes {
            return CryptoProvider.sign(privateKey.toByteArray(Charsets.UTF_8), bytes)
        }

        override fun signBytesWithSeed(bytes: Bytes, seed: Seed): Bytes {
            val account = PrivateKeyAccount(seed.toByteArray(Charsets.UTF_8))
            return CryptoProvider.sign(account.privateKey, bytes)
        }

        override fun verifySignature(publicKey: PublicKey, bytes: Bytes, signature: Bytes): Boolean {
            return CryptoProvider.get().verifySignature(base58decode(publicKey), bytes, signature)
        }

        override fun verifyPublicKey(publicKey: PublicKey): Boolean {
            val publicKeyDecode = base58decode(publicKey)
            return publicKeyDecode.size == PUBLIC_KEY_LENGTH
        }

        override fun verifyAddress(address: Address, chainId: String?, publicKey: PublicKey?): Boolean {
            if (address.isEmpty()) {
                return false
            }

            val bytes = Base58.decode(address)

            if (chainId != null) {
                if (chainId.length == 1) {
                    if (chainId[0].toByte() != bytes[1]) {
                        return false
                    }
                } else {
                    return false
                }
            }

            if (publicKey != null
                    && addressByPublicKey(publicKey, chainId) != address) {
                return false
            }

            return try {
                if (bytes.size == ADDRESS_LENGTH
                        && bytes[0] == ADDRESS_VERSION) {
                    val checkSum = Arrays.copyOfRange(bytes, bytes.size - CHECK_SUM_LENGTH, bytes.size)
                    val checkSumGenerated = calcCheckSum(bytes.copyOf(bytes.size - CHECK_SUM_LENGTH))
                    Arrays.equals(checkSum, checkSumGenerated)
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }
    }
}