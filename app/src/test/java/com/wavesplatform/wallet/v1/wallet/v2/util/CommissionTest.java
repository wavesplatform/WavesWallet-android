package com.wavesplatform.wallet.v1.wallet.v2.util;

import com.wavesplatform.sdk.utils.TransactionUtil;
import com.wavesplatform.sdk.model.response.GlobalTransactionCommission;
import com.wavesplatform.sdk.model.response.Transaction;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class CommissionTest {

    @Test
    public void checkCommissions() {
        GlobalTransactionCommission commission = new GlobalTransactionCommission();

        GlobalTransactionCommission.Params params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.TRANSFER);
        params.setSmartAccount(true);
        params.setSmartAsset(false);
        assertEquals(500000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.TRANSFER);
        params.setSmartAccount(false);
        params.setSmartAsset(false);
        assertEquals(100000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.TRANSFER);
        params.setSmartAccount(true);
        params.setSmartAsset(true);
        assertEquals(900000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.ISSUE);
        params.setSmartAccount(false);
        assertEquals(100000000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.ISSUE);
        params.setSmartAccount(true);
        assertEquals(100400000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.REISSUE);
        params.setSmartAccount(true);
        assertEquals(100400000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.REISSUE);
        params.setSmartAccount(false);
        assertEquals(100000000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.BURN);
        params.setSmartAccount(false);
        params.setSmartAsset(false);
        assertEquals(100000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.BURN);
        params.setSmartAccount(true);
        params.setSmartAsset(false);
        assertEquals(500000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.BURN);
        params.setSmartAccount(true);
        params.setSmartAsset(true);
        assertEquals(900000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.LEASE);
        params.setSmartAccount(false);
        params.setSmartAsset(true);
        assertEquals(100000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.LEASE);
        params.setSmartAccount(true);
        params.setSmartAsset(true);
        assertEquals(500000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.LEASE_CANCEL);
        params.setSmartAccount(false);
        params.setSmartAsset(true);
        assertEquals(100000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.LEASE_CANCEL);
        params.setSmartAccount(true);
        params.setSmartAsset(true);
        assertEquals(500000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.CREATE_ALIAS);
        params.setSmartAccount(false);
        params.setSmartAsset(true);
        assertEquals(100000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.CREATE_ALIAS);
        params.setSmartAccount(true);
        params.setSmartAsset(true);
        assertEquals(500000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.MASS_TRANSFER);
        params.setSmartAccount(false);
        params.setSmartAsset(false);
        params.setTransfersCount(1);
        assertEquals(200000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.MASS_TRANSFER);
        params.setSmartAccount(false);
        params.setSmartAsset(true);
        params.setTransfersCount(1);
        assertEquals(600000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.MASS_TRANSFER);
        params.setSmartAccount(true);
        params.setSmartAsset(true);
        params.setTransfersCount(1);
        assertEquals(1000000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.MASS_TRANSFER);
        params.setSmartAccount(false);
        params.setSmartAsset(false);
        params.setTransfersCount(2);
        assertEquals(200000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.MASS_TRANSFER);
        params.setSmartAccount(false);
        params.setSmartAsset(true);
        params.setTransfersCount(2);
        assertEquals(600000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.MASS_TRANSFER);
        params.setSmartAccount(true);
        params.setSmartAsset(true);
        params.setTransfersCount(2);
        assertEquals(1000000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.MASS_TRANSFER);
        params.setSmartAccount(false);
        params.setSmartAsset(false);
        params.setTransfersCount(3);
        assertEquals(300000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.MASS_TRANSFER);
        params.setSmartAccount(false);
        params.setSmartAsset(true);
        params.setTransfersCount(3);
        assertEquals(700000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.MASS_TRANSFER);
        params.setSmartAccount(true);
        params.setSmartAsset(true);
        params.setTransfersCount(3);
        assertEquals(1100000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.DATA);
        params.setSmartAccount(false);
        params.setSmartAsset(true);
        params.setBytesCount(1025);
        assertEquals(200000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.DATA);
        params.setSmartAccount(true);
        params.setSmartAsset(true);
        params.setBytesCount(2049);
        assertEquals(700000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.ADDRESS_SCRIPT);
        params.setSmartAccount(false);
        params.setSmartAsset(true);
        assertEquals(1000000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.ADDRESS_SCRIPT);
        params.setSmartAccount(true);
        params.setSmartAsset(true);
        assertEquals(1400000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.SPONSORSHIP);
        params.setSmartAccount(false);
        params.setSmartAsset(true);
        assertEquals(100000000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.SPONSORSHIP);
        params.setSmartAccount(true);
        params.setSmartAsset(true);
        assertEquals(100400000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.ASSET_SCRIPT);
        params.setSmartAccount(false);
        params.setSmartAsset(true);
        assertEquals(100000000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommission.Params();
        params.setTransactionType(Transaction.ASSET_SCRIPT);
        params.setSmartAccount(true);
        params.setSmartAsset(true);
        assertEquals(100400000L, TransactionUtil.Companion.countCommission(commission, params));
    }
}
