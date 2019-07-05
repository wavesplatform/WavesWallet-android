package com.wavesplatform.sdk.net.service

import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import com.wavesplatform.sdk.WavesSdk
import com.wavesplatform.sdk.crypto.WavesCrypto
import com.wavesplatform.sdk.model.request.node.*
import com.wavesplatform.sdk.utils.Environment
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.sdk.utils.WavesConstants
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Development test for checking Node transactions. Not for auto-testing
 */
@LargeTest
class WavesServiceTest {

    @Before
    fun initWavesSdk() {
        WavesSdk.init(InstrumentationRegistry.getTargetContext(), Environment.TEST_NET)
    }

    @Test
    fun sendTransferTransactionTest() {
        val transaction = TransferTransaction(
            assetId = WavesConstants.WAVES_ASSET_ID_EMPTY,
            recipient = "3Mq6WcupmXPVAzEB8DmXXiiT3kNFynebu6h",
            amount = 1,
            fee = WavesConstants.WAVES_MIN_FEE,
            attachment = WavesCrypto.base58encode("Hello!".toByteArray()),
            feeAssetId = WavesConstants.WAVES_ASSET_ID_EMPTY
        )

        transaction.sign(seed = SEED)

        WavesSdk.service()
            .getNode()
            .transactionsBroadcast(transaction)
            .compose(RxUtil.applyObservableDefaultSchedulers())
            .subscribe({
                Assert.assertEquals(true, true)
            }, {
                Assert.assertEquals(true, false)
            })

        TimeUnit.SECONDS.sleep(5)
    }

    @Test
    fun sendInvokeTransactionsTest() {
        val args = mutableListOf(
            InvokeScriptTransaction.Arg("string", "Some string!"),
            InvokeScriptTransaction.Arg("integer", 128L),
            InvokeScriptTransaction.Arg("integer", -127L),
            InvokeScriptTransaction.Arg("boolean", true),
            InvokeScriptTransaction.Arg("boolean", false),
            InvokeScriptTransaction.Arg("binary", "base64:VGVzdA=="))

        val call = InvokeScriptTransaction.Call(
            function = "testarg",
            args = args
        )

        val payment = mutableListOf(
            InvokeScriptTransaction.Payment(
                assetId = null,
                amount = 1L))

        val transaction = InvokeScriptTransaction(
            dApp = "3Mv9XDntij4ZRE1XiNZed6J74rncBpiYNDV",
            call = call,
            payment = payment)

        transaction.fee = 500000L
        transaction.sign(SEED)

        WavesSdk.service()
            .getNode()
            .transactionsBroadcast(transaction)
            .compose(RxUtil.applyObservableDefaultSchedulers())
            .subscribe({
                Assert.assertEquals(true, true)
            }, {
                Assert.assertEquals(true, false)
            })

        TimeUnit.SECONDS.sleep(5)
    }

    @Test
    fun sendAliasTransactionsTest() {
        val transferTransaction = AliasTransaction("letnyayapechalka")

        transferTransaction.sign(seed = SEED)

        WavesSdk.service()
            .getNode()
            .transactionsBroadcast(transferTransaction)
            .compose(RxUtil.applyObservableDefaultSchedulers())
            .subscribe({
                Assert.assertEquals(true, true)
            }, {
                Assert.assertEquals(true, false)
            })

        TimeUnit.SECONDS.sleep(5)
    }

    @Test
    fun sendBurnTransactionsTest() {
        val transferTransaction = BurnTransaction("EZvjPdTR6YEpvAx2fkYGtN8vLZrWo3cYCMJ2BX8DTP9k", 1)

        transferTransaction.sign(seed = SEED)

        WavesSdk.service()
            .getNode()
            .transactionsBroadcast(transferTransaction)
            .compose(RxUtil.applyObservableDefaultSchedulers())
            .subscribe({
                Assert.assertEquals(true, true)
            }, {
                Assert.assertEquals(true, false)
            })

        TimeUnit.SECONDS.sleep(5)
    }

    @Test
    fun sendLeasingTransactionsTest() {
        val transferTransaction = LeaseTransaction("3Mq6WcupmXPVAzEB8DmXXiiT3kNFynebu6h", 1)

        transferTransaction.sign(seed = SEED)

        WavesSdk.service()
            .getNode()
            .transactionsBroadcast(transferTransaction)
            .compose(RxUtil.applyObservableDefaultSchedulers())
            .subscribe({
                Assert.assertEquals(true, true)
            }, {
                Assert.assertEquals(true, false)
            })

        TimeUnit.SECONDS.sleep(5)
    }

    @Test
    fun sendCancelLeasingTransactionsTest() {
        val transferTransaction = LeaseCancelTransaction("C8TYbzvYv7qRPfPFzbHaCrKmM5pwHKHUMGWZeGBCJaFL")

        transferTransaction.sign(seed = SEED)

        WavesSdk.service()
            .getNode()
            .transactionsBroadcast(transferTransaction)
            .compose(RxUtil.applyObservableDefaultSchedulers())
            .subscribe({
                Assert.assertEquals(true, true)
            }, {
                Assert.assertEquals(true, false)
            })

        TimeUnit.SECONDS.sleep(5)
    }

    @Test
    fun sendDataTransactionsTest() {
        val transferTransaction = DataTransaction(mutableListOf(
            DataTransaction.Data("key0", "string", "This is Data TX"),
            DataTransaction.Data("key1", "integer", 100),
            DataTransaction.Data("key2", "integer", -100),
            DataTransaction.Data("key3", "boolean", true),
            DataTransaction.Data("key4", "boolean", false),
            DataTransaction.Data("key5", "binary", "SGVsbG8h") // base64 binary string
        ))

        transferTransaction.sign(seed = SEED)

        WavesSdk.service()
            .getNode()
            .transactionsBroadcast(transferTransaction)
            .compose(RxUtil.applyObservableDefaultSchedulers())
            .subscribe({
                Assert.assertEquals(true, true)
            }, {
                Assert.assertEquals(true, false)
            })

        TimeUnit.SECONDS.sleep(5)
    }


    @Test
    fun sendIssueTransactionsTest() {
        val transferTransaction = IssueTransaction(
            "New Asset",
            "Details of Asset",
            100_000_000L,
            8,
            true,
            "AwZd0cYf") // scripts: AwZd0cYf = true and AweHXCN1 = false (You can't it change anymore)

        transferTransaction.fee = 100000000

        transferTransaction.sign(seed = SEED)

        WavesSdk.service()
            .getNode()
            .transactionsBroadcast(transferTransaction)
            .compose(RxUtil.applyObservableDefaultSchedulers())
            .subscribe({
                Assert.assertEquals(true, true)
            }, {
                Assert.assertEquals(true, false)
            })

        TimeUnit.SECONDS.sleep(5)
    }


    @Test
    fun sendReissueTransactionsTest() {
        val transferTransaction = ReissueTransaction(
            "5HiZ9n8oEBL495nMrpg57As7ujMwqmFn2hU3nYqv1Qx9",
            100_000_000L,
            true)

        transferTransaction.fee = 100000000

        transferTransaction.sign(seed = SEED)

        WavesSdk.service()
            .getNode()
            .transactionsBroadcast(transferTransaction)
            .compose(RxUtil.applyObservableDefaultSchedulers())
            .subscribe({
                Assert.assertEquals(true, true)
            }, {
                Assert.assertEquals(true, false)
            })

        TimeUnit.SECONDS.sleep(5)
    }


    @Test
    fun sendMassTransferTransactionsTest() {
        val transfers = mutableListOf(
            MassTransferTransaction.Transfer("3Mps7CZqB9nUbEirYyCMMoA7VbqrxLvJFSB", 1),
            MassTransferTransaction.Transfer("3Mq6WcupmXPVAzEB8DmXXiiT3kNFynebu6h", 1))

        val transferTransaction = MassTransferTransaction(
            "BHar7QeZLmHkGqQnvBRWjyHaNKJUstYBaDrPQ64cjJL9",
            transfers = transfers,
            attachment = WavesCrypto.base58encode("Hello attachment!".toByteArray()))

        transferTransaction.fee = 200000

        transferTransaction.sign(seed = SEED)

        WavesSdk.service()
            .getNode()
            .transactionsBroadcast(transferTransaction)
            .compose(RxUtil.applyObservableDefaultSchedulers())
            .subscribe({
                Assert.assertEquals(true, true)
            }, {
                Assert.assertEquals(true, false)
            })

        TimeUnit.SECONDS.sleep(5)
    }

    @Test
    fun sendSponsorshipTransactionsTest() {
        val transferTransaction = SponsorshipTransaction(
            "BHar7QeZLmHkGqQnvBRWjyHaNKJUstYBaDrPQ64cjJL9",
            2) // 0 for cancel

        transferTransaction.fee = 100000000

        transferTransaction.sign(seed = SEED)

        WavesSdk.service()
            .getNode()
            .transactionsBroadcast(transferTransaction)
            .compose(RxUtil.applyObservableDefaultSchedulers())
            .subscribe({
                Assert.assertEquals(true, true)
            }, {
                Assert.assertEquals(true, false)
            })

        TimeUnit.SECONDS.sleep(5)
    }

    @Test
    fun sendSetAssetScriptTransactionsTest() {
        val transferTransaction = SetAssetScriptTransaction(
            "FxmduD1XBq45inE2EoBoE3fuQZEuhsenWjBcAr8X3Pp8",
            "AweHXCN1") // scripts: AwZd0cYf = true and AweHXCN1 = false (You can't it change anymore)

        transferTransaction.fee = 100000000

        transferTransaction.sign(seed = SEED)

        WavesSdk.service()
            .getNode()
            .transactionsBroadcast(transferTransaction)
            .compose(RxUtil.applyObservableDefaultSchedulers())
            .subscribe({
                Assert.assertEquals(true, true)
            }, {
                Assert.assertEquals(true, false)
            })

        TimeUnit.SECONDS.sleep(5)
    }

    @Test
    fun sendSetScriptTransactionsTest() {
        val transaction = SetScriptTransaction(
            null) // script: AwZd0cYf = true

        transaction.fee = 100000000

        transaction.sign(seed = SEED)

        WavesSdk.service()
            .getNode()
            .transactionsBroadcast(transaction)
            .compose(RxUtil.applyObservableDefaultSchedulers())
            .subscribe({
                Assert.assertEquals(true, true)
            }, {
                Assert.assertEquals(true, false)
            })

        TimeUnit.SECONDS.sleep(5)
    }

    companion object {
        const val SEED = "tomorrow puppy car cabin treat ticket weapon soda slush camp idea mountain name erupt broom"
    }
}