package com.wavesplatform.wallet.api;

import com.wavesplatform.wallet.crypto.Base58;
import com.wavesplatform.wallet.crypto.CryptoProvider;
import com.wavesplatform.wallet.crypto.PrivateKeyAccount;
import com.wavesplatform.wallet.data.auth.WavesWallet;
import com.wavesplatform.wallet.payload.AssetBalances;
import com.wavesplatform.wallet.payload.Transaction;
import com.wavesplatform.wallet.payload.TransactionsInfo;
import com.wavesplatform.wallet.payload.WavesBalance;
import com.wavesplatform.wallet.request.IssueTransactionRequest;
import com.wavesplatform.wallet.request.TransferTransactionRequest;
import com.wavesplatform.wallet.ui.auth.EnvironmentManager;
import com.wavesplatform.wallet.util.PrefsUtil;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import io.reactivex.Observable;
import util.MockJson;
import util.SerializeUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NodeManagerTest {

    private static final String SEED_WORDS = "trial appear battle what fiber hello weasel grunt spare heavy produce beach one friend sad";
    private static final String PUB_KEY = "EtujXZmPthSG8YUwvF88seDcw4WH6ZPN3tRJZ6w1mev1";
    private static final String ADDR = "3N5sUvKLnEUBwk7WFCSjJs8VoiLiuqTs29v";
    private static final String BASE58_SEED = Base58.encode(SEED_WORDS.getBytes());

    private NodeApi service;
    private SpamManager.SpamApi spamService;
    private WavesWallet wavesWallet;

    @Before
    public void setUp() {
        service = Mockito.mock(NodeApi.class);
        spamService = Mockito.mock(SpamManager.SpamApi.class);
        PrefsUtil prefsUtil = Mockito.mock(PrefsUtil.class);
        Mockito.when(prefsUtil.getEnvironment()).thenReturn("env_testnet");
        Mockito.when(prefsUtil.getValue(Mockito.eq(PrefsUtil.KEY_DISABLE_SPAM_FILTER), Mockito.anyBoolean())).thenReturn(false);
        EnvironmentManager.init(prefsUtil, null);
        NodeManager.createTestInstance(service, PUB_KEY);
        NodeManager.get().setPrefsUtil(prefsUtil);
        wavesWallet = new WavesWallet(SEED_WORDS.getBytes());
        SpamManager.createTestInstance(spamService);

        WavesBalance wavesBalance = SerializeUtils.serializeJsonFromAssets(MockJson.WAVES_BALANCE.getPath(), MockJson.WAVES_BALANCE.getType());
        List<List<Transaction>> transactionsList = SerializeUtils.serializeJsonFromAssets(MockJson.TRANSACTIONS.getPath(), MockJson.TRANSACTIONS.getType());
        AssetBalances assetBalances = SerializeUtils.serializeJsonFromAssets(MockJson.ASSETS_BALANCE.getPath(), MockJson.ASSETS_BALANCE.getType());
        List<Transaction> unconfirmed = SerializeUtils.serializeJsonFromAssets(MockJson.UNCONFIRMED_TRANSACTIONS.getPath(), MockJson.UNCONFIRMED_TRANSACTIONS.getType());
        TransactionsInfo transactionsInfo = SerializeUtils.serializeJsonFromAssets(MockJson.TRANSACTIONS_INFO.getPath(), MockJson.TRANSACTIONS_INFO.getType());
        String scamTokens = SerializeUtils.getStringAsset(MockJson.SCAM_TOKENS.getPath());
        Mockito.when(service.transactionList(Mockito.eq(ADDR), Mockito.anyInt())).thenReturn(Observable.just(transactionsList));
        Mockito.when(service.wavesBalance(Mockito.eq(ADDR))).thenReturn(Observable.just(wavesBalance));
        Mockito.when(service.assetsBalance(Mockito.eq(ADDR))).thenReturn(Observable.just(assetBalances));
        Mockito.when(service.unconfirmedTransactions()).thenReturn(Observable.just(unconfirmed));
        Mockito.when(service.getTransactionsInfo(Mockito.anyString())).thenReturn(Observable.just(transactionsInfo));
        Mockito.when(service.broadcastTransfer(Mockito.any(TransferTransactionRequest.class))).thenReturn(Observable.just(new TransferTransactionRequest()));
        Mockito.when(service.broadcastIssue(Mockito.any(IssueTransactionRequest.class))).thenReturn(Observable.just(new IssueTransactionRequest("Z", "", "", 0, (byte) 0, false, 0)));
        Mockito.when(spamService.getSpamAssets()).thenReturn(Observable.just(scamTokens));
    }

    @Test
    public void lastTransactions() {
        NodeManager.get().loadBalancesAndTransactions()
                .subscribe(() -> assertEquals(7, NodeManager.get().transactions.get(0).type));
    }

    @Test
    public void signature() throws Exception {
        PrivateKeyAccount acc = new PrivateKeyAccount("seed".getBytes());
        byte[] msg = "message".getBytes();
        String sigStr = Base58.encode(CryptoProvider.sign(acc.getPrivateKey(), msg));
        assertTrue(CryptoProvider.get().verifySignature(acc.getPublicKey(), msg, Base58.decode(sigStr)));
    }

    @Test
    public void hashes() throws Exception {
        PrivateKeyAccount pk = new PrivateKeyAccount(Base58.decode(BASE58_SEED));
        assertEquals(PUB_KEY, pk.getPublicKeyStr());
    }

    @Test
    public void empty() {
        NodeManager nm = NodeManager.createTestInstance(service, "Z");
        assertEquals("3NBb35QMPKnSWPLHKT52kKyVZbwbEDvNf2N", nm.getAddress());
    }

    @Test
    public void broadcastTransfer() throws Exception {
        TransferTransactionRequest tx = new TransferTransactionRequest(
                null,
                NodeManager.get().getPublicKeyStr(),
                "3NBVqYXrapgJP9atQccdBPAgJPwHDKkh6A8",
                100000000L,
                1486838172523L,
                100000L,
                "attachment");
        tx.sign(wavesWallet.getPrivateKey());
        NodeManager.get().broadcastTransfer(tx)
                .test().assertComplete();
    }

    @Test
    public void broadcastIssue() throws Exception {
        IssueTransactionRequest tx = new IssueTransactionRequest(
                NodeManager.get().getPublicKeyStr(),
                "gnt2",
                "",
                6000000L,
                (byte)6,
                true,
                System.currentTimeMillis());

        tx.sign(wavesWallet.getPrivateKey());
        NodeManager.get().broadcastIssue(tx)
                .test().assertComplete();
    }

    @Test
    public void spamAssets() {
        SpamManager.get().getSpamAssets()
                .blockingSubscribe(spam -> assertEquals(22, spam.size()));
    }
}
