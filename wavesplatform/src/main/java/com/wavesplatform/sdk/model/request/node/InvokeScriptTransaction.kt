package com.wavesplatform.sdk.model.request.node

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Ints
import com.google.common.primitives.Longs
import com.google.common.primitives.Shorts
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.utils.SignUtil
import com.wavesplatform.sdk.utils.arrayWithIntSize
import com.wavesplatform.sdk.utils.arrayWithSize
import kotlinx.android.parcel.Parcelize
import java.nio.charset.Charset

/***
 * Invoke script transaction is a transaction that invokes functions of the dApp script.
 * dApp contains compiled functions  developed with [Waves Ride IDE]({https://ide.wavesplatform.com/)
 * You can invoke one of them by name with some arguments.
 */
class InvokeScriptTransaction(
        /**
         * Asset id instead Waves for transaction commission withdrawal
         */
        @SerializedName("feeAssetId") var feeAssetId: String? = null,
        /**
         * dApp – address or alias of contract with function on RIDE language
         */
        @SerializedName("dApp") var dApp: String,
        /**
         * Function name in dApp with array of arguments
         */
        @SerializedName("call") var call: Call?,
        /**
         * Payment for function of dApp. Now it works with only one payment.
         */
        @SerializedName("payment") var payment: List<Payment> = mutableListOf())
    : BaseTransaction(SCRIPT_INVOCATION) {

    override fun toBytes(): ByteArray {

        return try {
            Bytes.concat(
                    byteArrayOf(type.toByte()),
                    byteArrayOf(version.toByte()),
                    byteArrayOf(chainId),
                    Base58.decode(senderPublicKey),
                    TransferTransaction.getRecipientBytes(dApp),
                    functionCallArray(),
                    paymentsArray(), // now it works with only one
                    Longs.toByteArray(fee),
                    SignUtil.arrayOption(feeAssetId ?: ""),
                    Longs.toByteArray(timestamp))
        } catch (e: Exception) {
            Log.e("Sign", "Can't create bytes for sign in Script Invocation Transaction", e)
            ByteArray(0)
        }
    }

    override fun sign(seed: String): String {
        version = 1
        signature = super.sign(seed)
        return signature ?: ""
    }

    private fun functionCallArray(): ByteArray {
        return if (call == null) {
            byteArrayOf(0)
        } else {
            val optionalCall: Byte = 1
            // Special bytes 9 and 1 to indicate function call. Used in Serde serializer
            val functionUseArray = Bytes.concat(byteArrayOf(optionalCall, 9, 1))
            Bytes.concat(functionUseArray, functionArray(), argsArray())
        }
    }

    private fun functionArray(): ByteArray {
        return call!!.function
                .toByteArray(Charset.forName("UTF-8"))
                .arrayWithIntSize()
    }

    private fun argsArray(): ByteArray {
        var array = byteArrayOf()
        for (arg in call!!.args) {
            array = when (arg.type) {
                "integer" -> getIntegerBytes(arg, array)
                "binary" -> getBinaryBytes(arg, array)
                "string" -> getStringBytes(arg, array)
                "boolean" -> getBooleanBytes(arg, array)
                else -> array
            }
        }

        val lengthBytes = Ints.toByteArray(call!!.args.size)
        return Bytes.concat(lengthBytes, array)
    }

    private fun getBooleanBytes(arg: Arg, array: ByteArray): ByteArray {
        val value = arg.value as Boolean
        val byte: Byte = if (value) 6 else 7
        return Bytes.concat(array, byteArrayOf(byte))
    }

    private fun getStringBytes(arg: Arg, array: ByteArray) =
            Bytes.concat(array, DataTransaction.stringValue(2, arg.value as String, true))

    private fun getBinaryBytes(arg: Arg, array: ByteArray): ByteArray {
        return Bytes.concat(
                array,
                DataTransaction.binaryValue(1, (arg.value as String)
                        .replace("base64:", ""), true))
    }

    private fun getIntegerBytes(arg: Arg, array: ByteArray): ByteArray {
        val longValue: Long = if (arg.value is Int) {
            (arg.value as Int).toLong()
        } else {
            arg.value as Long
        }
        return Bytes.concat(array, DataTransaction.integerValue(0, longValue))
    }

    private fun paymentsArray(): ByteArray {
        var array = byteArrayOf()
        for (paymentItem in payment) {
            val amount = Longs.toByteArray(paymentItem.amount)
            val assetId = if (paymentItem.assetId.isNullOrEmpty()) {
                byteArrayOf(1)
            } else {
                SignUtil.arrayOption(paymentItem.assetId!!)
            }
            array = Bytes.concat(array, Bytes.concat(amount, assetId))
        }
        val lengthBytes = Shorts.toByteArray(payment.size.toShort())
        return Bytes.concat(lengthBytes, array.arrayWithSize())
    }

    /**
     * Payment for function of dApp. Now it works with only one payment.
     */
    class Payment(
            /**
             * Amount in satoshi
             */
            @SerializedName("amount") var amount: Long,
            /**
             * Asset Id in Waves blockchain
             */
            @SerializedName("assetId") var assetId: String? = null) : Parcelable {

        private constructor(parcel: Parcel) : this(
                amount = parcel.readLong(),
                assetId = parcel.readString()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeLong(amount)
            parcel.writeString(assetId)
        }

        override fun describeContents() = 0

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<Payment> {
                override fun createFromParcel(parcel: Parcel) = Payment(parcel)
                override fun newArray(size: Int) = arrayOfNulls<Payment>(size)
            }
        }
    }

    /**
     * Call the function from dApp (address or alias) with typed arguments
     */
    @Parcelize
    class Call(
            /**
             * Function unique name
             */
            @SerializedName("function") var function: String,
            /**
             * List of arguments
             */
            @SerializedName("args") var args: List<Arg> = mutableListOf()) : Parcelable

    /**
     * Argumens for the [Call.function] in [Call]
     */
    class Arg(
            /**
             * Type can be of four types - integer(0), boolean(1), binary array(2) and string(3).
             */
            @SerializedName("type") var type: String?,
            /**
             * Value can of four types - integer(0), boolean(1), binary array(2) and string(3).
             * And it depends on type.
             */
            @SerializedName("value") var value: Any?) : Parcelable {

        private constructor(parcel: Parcel) : this(
                type = parcel.readString(),
                value = parcel.readValue(Any::class.java.classLoader)
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(type)
            parcel.writeValue(value)
        }

        override fun describeContents() = 0

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<Arg> {
                override fun createFromParcel(parcel: Parcel) = Arg(parcel)
                override fun newArray(size: Int) = arrayOfNulls<Arg>(size)
            }
        }
    }
}