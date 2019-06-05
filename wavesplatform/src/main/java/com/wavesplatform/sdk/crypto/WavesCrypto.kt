package com.wavesplatform.sdk.crypto

import com.wavesplatform.sdk.utils.*
import org.apache.commons.codec.binary.Base64
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

    fun blake2b(input: Bytes): Bytes
    fun keccak(input: Bytes): Bytes
    fun sha256(input: Bytes): Bytes

    fun base58encode(input: Bytes): String
    fun base58decode(input: String): Bytes
    fun base64encode(input: Bytes): String
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

    companion object : WavesCrypto  {

        override fun blake2b(input: Bytes): Bytes {
            return Hash.blake2b(input)
        }

        override fun keccak(input: Bytes): Bytes {
            return Hash.keccak(input)
        }

        override fun sha256(input: Bytes): Bytes {
            return Sha256.hash(input)
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
            return addressFromPublicKey(Base58.decode(publicKey))
        }

        override fun addressBySeed(seed: Seed, chainId: String?): Address {
            return addressFromPublicKey(publicKey(seed))
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
            // todo что значит publicKey вместе с address?
            if (address.isEmpty() || chainId == null) {
                return false
            }
            return try {
                val bytes = Base58.decode(address)
                if (bytes.size == ADDRESS_LENGTH &&
                        bytes[0] == ADDRESS_VERSION &&
                        bytes[1] == chainId[0].toByte()) {
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