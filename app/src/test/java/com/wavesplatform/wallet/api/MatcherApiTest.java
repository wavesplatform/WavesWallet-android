package com.wavesplatform.wallet.api;

import com.wavesplatform.wallet.payload.AssetBalances;
import com.wavesplatform.wallet.payload.Transaction;
import com.wavesplatform.wallet.payload.TransactionsInfo;
import com.wavesplatform.wallet.payload.WavesBalance;
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

public class MatcherApiTest {

    private static final String ADDR = "3N5sUvKLnEUBwk7WFCSjJs8VoiLiuqTs29v";
    private static final String PUB_KEY = "EtujXZmPthSG8YUwvF88seDcw4WH6ZPN3tRJZ6w1mev1";

    private NodeApi service;

    @Before
    public void setUp() {
        service = Mockito.mock(NodeApi.class);
        PrefsUtil prefsUtil = Mockito.mock(PrefsUtil.class);
        Mockito.when(prefsUtil.getEnvironment()).thenReturn("env_testnet");
        Mockito.when(prefsUtil.getValue(Mockito.eq(PrefsUtil.KEY_DISABLE_SPAM_FILTER), Mockito.anyBoolean())).thenReturn(false);
        EnvironmentManager.init(prefsUtil, null);
        NodeManager.createTestInstance(service, PUB_KEY);
        NodeManager.get().setPrefsUtil(prefsUtil);

        WavesBalance wavesBalance = SerializeUtils.serializeJsonFromAssets(MockJson.WAVES_BALANCE.getPath(), MockJson.WAVES_BALANCE.getType());
        List<List<Transaction>> transactionsList = SerializeUtils.serializeJsonFromAssets(MockJson.TRANSACTIONS.getPath(), MockJson.TRANSACTIONS.getType());
        AssetBalances assetBalances = SerializeUtils.serializeJsonFromAssets(MockJson.ASSETS_BALANCE.getPath(), MockJson.ASSETS_BALANCE.getType());
        List<Transaction> unconfirmed = SerializeUtils.serializeJsonFromAssets(MockJson.UNCONFIRMED_TRANSACTIONS.getPath(), MockJson.UNCONFIRMED_TRANSACTIONS.getType());
        TransactionsInfo transactionsInfo = SerializeUtils.serializeJsonFromAssets(MockJson.TRANSACTIONS_INFO.getPath(), MockJson.TRANSACTIONS_INFO.getType());
        Mockito.when(service.transactionList(Mockito.eq(ADDR), Mockito.anyInt())).thenReturn(Observable.just(transactionsList));
        Mockito.when(service.wavesBalance(Mockito.eq(ADDR))).thenReturn(Observable.just(wavesBalance));
        Mockito.when(service.assetsBalance(Mockito.eq(ADDR))).thenReturn(Observable.just(assetBalances));
        Mockito.when(service.unconfirmedTransactions()).thenReturn(Observable.just(unconfirmed));
        Mockito.when(service.getTransactionsInfo(Mockito.anyString())).thenReturn(Observable.just(transactionsInfo));
    }

    @Test
    public void assetsBalance() {
        NodeManager.get().loadBalancesAndTransactions()
                .subscribe(() -> {
                    AssetBalances balances = NodeManager.get().assetBalances;
                    assertEquals(13, balances.balances.size());
                    assertEquals(ADDR, balances.address);
                    List<Transaction> txs = NodeManager.get().transactions;
                    assertEquals(5, txs.size());
                });
    }

    @Test
    public void lastTransactions() {
        service.transactionList(ADDR, 50).
                blockingSubscribe(txs -> {
                    assertEquals(1, txs.size());
                    assertTrue(txs.get(0).size() > 0);
                });
    }

    @Test
    public void doubleRequest() {
        Observable.zip(service.assetsBalance(ADDR),
                service.transactionList(ADDR, 50).map(r -> r.get(0)),
                (abs, txs) -> abs.address + " : " + txs.get(0).id)
                .blockingSubscribe(res -> assertEquals(res, ADDR + " : 8fpFrkTbgNtuCd3PEejhpP8KTGGTEwqPVWcB164MB1mK"));
    }

    @Test
    public void mergeObservables() {
        service.assetsBalance(ADDR)
                .doOnNext(abs -> {
                    assertEquals(abs.address, ADDR);
                    assertEquals(abs.balances.get(0).balance, 99934100);
                }).ignoreElements()
                .mergeWith(
                        service.transactionList(ADDR, 50).map(r -> r.get(0))
                                .doOnNext(txs -> assertEquals(txs.get(0).type, 7)).ignoreElements()
                )
                .test().assertComplete();
    }
}