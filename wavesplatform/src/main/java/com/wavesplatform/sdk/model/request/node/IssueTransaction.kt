/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model.request.node

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.crypto.WavesCrypto
import com.wavesplatform.sdk.model.request.node.IssueTransaction.Companion.MAX_ASSET_NAME_LENGTH
import com.wavesplatform.sdk.model.request.node.IssueTransaction.Companion.MAX_DECIMALS
import com.wavesplatform.sdk.model.request.node.IssueTransaction.Companion.MAX_DESCRIPTION_LENGTH
import com.wavesplatform.sdk.model.request.node.IssueTransaction.Companion.MIN_ASSET_NAME_LENGTH
import com.wavesplatform.sdk.utils.arrayWithSize
import com.wavesplatform.sdk.utils.scriptBytes
import java.nio.charset.Charset

/**
 * The Issue transaction add a new asset in blockchain.
 *
 * Issue transaction is used to give the user the possibility to issue his/her own tokens
 * on Waves blockchain. The user can define the exact amount of the issued tokens
 * and he can reissue more tokens later by enabling the reissuable flag (1- true).
 *
 * Script can be developed with [Waves Ride IDE]({https://ide.wavesplatform.com/)
 */
class IssueTransaction(
    /**
     * Name of your new asset byte length must be in [[MIN_ASSET_NAME_LENGTH],[MAX_ASSET_NAME_LENGTH]]
     */
    @SerializedName("name") val name: String,
    /**
     * Description of your new asset byte length must be in [0;[MAX_DESCRIPTION_LENGTH]]
     */
    @SerializedName("description") val description: String = "",
    /**
     * Quantity defines the total tokens supply that your asset will contain.
     */
    @SerializedName("quantity") val quantity: Long,
    /**
     * Decimals defines the number of decimals that your asset token will be divided in.
     * Max decimals is [MAX_DECIMALS]
     */
    @SerializedName("decimals") val decimals: Byte,
    /**
     * Reissuability allows for additional tokens creation that will be added
     * to the total token supply of asset.
     * A non-reissuable asset will be permanently limited to the total token supply
     * defined during the transaction.
     */
    @SerializedName("reissuable") val reissuable: Boolean,
    /**
     * A Smart Asset is an asset with an attached script that places conditions
     * on every transaction made for the token in question.
     * Each validation of a transaction by a Smart Asset's script increases the transaction fee
     * by 0.004 WAVES. For example,
     *
     * if a regular tx is made for a Smart Asset, the cost is 0.001 + 0.004 = 0.005 WAVES.
     * If an exchange transaction is made, the cost is 0.003 + 0.004 = 0.007 WAVES.
     *
     * Null - issue without script.
     *
     * You can update it later only if here in issue script != null.
     * You can't update later if set script == null now
     *
     * You can use "base64:compiledScriptStringInBase64" and just "compiledScriptStringInBase64"
     * Can't be empty string
     */
    @SerializedName("script") val script: String? = null
) : BaseTransaction(ISSUE) {

    /**
     * New Id for new asset of the blockchain
     */
    @SerializedName("id")
    var id: String? = ""

    init {
        this.fee = WAVES_ISSUE_MIN_FEE
    }

    override fun toBytes(): ByteArray {
        try {
            val reissuableBytes = if (reissuable) {
                byteArrayOf(1)
            } else {
                byteArrayOf(0)
            }

            val bytes = Bytes.concat(
                byteArrayOf(type.toByte()),
                byteArrayOf(version.toByte()),
                byteArrayOf(chainId),
                Base58.decode(senderPublicKey),
                name.toByteArray(Charset.forName("UTF-8")).arrayWithSize(),
                description.toByteArray(Charset.forName("UTF-8")).arrayWithSize(),
                Longs.toByteArray(quantity),
                byteArrayOf(decimals),
                reissuableBytes,
                Longs.toByteArray(fee),
                Longs.toByteArray(timestamp),
                scriptBytes(script)
            )

            this.id = WavesCrypto.base58encode(WavesCrypto.blake2b(bytes))

            return bytes
        } catch (e: Exception) {
            Log.e("Sign", "Can't create bytes for sign in Issue Transaction", e)
            return ByteArray(0)
        }
    }

    companion object {
        const val WAVES_ISSUE_MIN_FEE = 100000000L
        const val WAVES_ISSUE_NON_FUNGIBLE_MIN_FEE = 100000L
        const val MAX_DESCRIPTION_LENGTH = 1000
        const val MAX_ASSET_NAME_LENGTH = 16
        const val MIN_ASSET_NAME_LENGTH = 4
        const val MAX_DECIMALS = 8
    }
}
