package com.wavesplatform.wallet.v1.api;

import org.junit.Before;
import org.junit.Test;

import io.reactivex.Observable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MatherApiTest {
    private NodeApi service;

    @Before
    public void setUp() {
        NodeManager.createInstance("3N5sUvKLnEUBwk7WFCSjJs8VoiLiuqTs29v");
    }

    @Test
    public void assetsBalance() throws Exception {
        try {
            NodeManager.get().loadBalancesAndTransactions().blockingGet();
        } catch (Throwable t) {
            System.out.println(t);
        }
    }

    @Test
    public void lastTransactions() throws Exception {
        service.transactionList("3N5sUvKLnEUBwk7WFCSjJs8VoiLiuqTs29v", 50).
                blockingSubscribe(txs -> {
                    assertEquals(1, txs.size());
                    assertTrue(txs.get(0).size() > 0);
                });
    }

    @Test
    public void doubleRequest() throws Exception {
        Observable.zip(service.assetsBalance("3N5sUvKLnEUBwk7WFCSjJs8VoiLiuqTs29v"),
                service.transactionList("3N5sUvKLnEUBwk7WFCSjJs8VoiLiuqTs29v", 50).map(r -> r.get(0)),
                (abs, txs) -> abs.address + "txs: " + txs).blockingSubscribe(res -> {
            System.out.println(res);
        });
    }

    @Test
    public void mergeObservables() {
        Long[] gData = new Long[]{1L, 2L};

        //Observable.fromArray(gData)
        //       .concatMap(data -> {

        service.assetsBalance("3N5sUvKLnEUBwk7WFCSjJs8VoiLiuqTs29v").doOnNext(abs -> {
            System.out.println(abs);
        }).ignoreElements().mergeWith(
                service.transactionList("3N5sUvKLnEUBwk7WFCSjJs8VoiLiuqTs29v", 50).map(r -> r.get(0))
                        .doOnNext(txs -> {
                            System.out.println(txs);
                        }).ignoreElements()
        ).subscribe(() -> {
            System.out.println("Done");
        });
    }

}