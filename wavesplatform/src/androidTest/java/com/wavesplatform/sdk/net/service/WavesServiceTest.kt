package com.wavesplatform.net.service

import android.support.test.InstrumentationRegistry
import com.wavesplatform.sdk.WavesSdk
import com.wavesplatform.sdk.crypto.WavesCrypto
import com.wavesplatform.sdk.model.request.node.*
import com.wavesplatform.sdk.utils.Environment
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.sdk.utils.WavesConstants
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.TimeUnit

class WavesServiceTest {

    @Test
    fun sendTransferTransactionTest() {
        WavesSdk.init(InstrumentationRegistry.getTargetContext(), Environment.TEST_NET)

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
        WavesSdk.init(InstrumentationRegistry.getTargetContext(), Environment.TEST_NET)

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
        WavesSdk.init(InstrumentationRegistry.getTargetContext(), Environment.TEST_NET)

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
        WavesSdk.init(InstrumentationRegistry.getTargetContext(), Environment.TEST_NET)

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
        WavesSdk.init(InstrumentationRegistry.getTargetContext(), Environment.TEST_NET)

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
        WavesSdk.init(InstrumentationRegistry.getTargetContext(), Environment.TEST_NET)

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
        WavesSdk.init(InstrumentationRegistry.getTargetContext(), Environment.TEST_NET)

        val transferTransaction = DataTransaction(mutableListOf(
            DataTransaction.Data("key0", "string", "This is Data TX"),
            DataTransaction.Data("key1", "integer", 100),
            DataTransaction.Data("key2", "integer", -100),
            DataTransaction.Data("key3", "boolean", true),
            DataTransaction.Data("key4", "boolean", false),
            DataTransaction.Data("key5", "binary", "SGVsbG8h")
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
        WavesSdk.init(InstrumentationRegistry.getTargetContext(), Environment.TEST_NET)

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
        WavesSdk.init(InstrumentationRegistry.getTargetContext(), Environment.TEST_NET)

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
        WavesSdk.init(InstrumentationRegistry.getTargetContext(), Environment.TEST_NET)

        val transfers = mutableListOf(
            MassTransferTransaction.Transfer("3Mps7CZqB9nUbEirYyCMMoA7VbqrxLvJFSB", 1),
            MassTransferTransaction.Transfer("3Mq6WcupmXPVAzEB8DmXXiiT3kNFynebu6h", 1))

        val transferTransaction = MassTransferTransaction(
            "BHar7QeZLmHkGqQnvBRWjyHaNKJUstYBaDrPQ64cjJL9",
            transfers = transfers,
            attachment = "SGVsbG8h") // todo check

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
        WavesSdk.init(InstrumentationRegistry.getTargetContext(), Environment.TEST_NET)

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
        WavesSdk.init(InstrumentationRegistry.getTargetContext(), Environment.TEST_NET)

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
        WavesSdk.init(InstrumentationRegistry.getTargetContext(), Environment.TEST_NET)

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