package com.wavesplatform.wallet.v1.wallet.v2.util;

import com.wavesplatform.sdk.utils.TransactionUtil;
import com.wavesplatform.wallet.v2.data.model.service.cofigs.GlobalTransactionCommissionResponse;
import com.wavesplatform.sdk.net.model.response.TransactionResponse;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class CommissionTest {

    @Test
    public void checkCommissions() {
        GlobalTransactionCommissionResponse commission = new GlobalTransactionCommissionResponse();

        GlobalTransactionCommissionResponse.ParamsResponse params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.TRANSFER);
        params.setSmartAccount(true);
        params.setSmartAsset(false);
        assertEquals(500000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.TRANSFER);
        params.setSmartAccount(false);
        params.setSmartAsset(false);
        assertEquals(100000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.TRANSFER);
        params.setSmartAccount(true);
        params.setSmartAsset(true);
        assertEquals(900000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.ISSUE);
        params.setSmartAccount(false);
        assertEquals(100000000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.ISSUE);
        params.setSmartAccount(true);
        assertEquals(100400000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.REISSUE);
        params.setSmartAccount(true);
        assertEquals(100400000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.REISSUE);
        params.setSmartAccount(false);
        assertEquals(100000000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.BURN);
        params.setSmartAccount(false);
        params.setSmartAsset(false);
        assertEquals(100000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.BURN);
        params.setSmartAccount(true);
        params.setSmartAsset(false);
        assertEquals(500000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.BURN);
        params.setSmartAccount(true);
        params.setSmartAsset(true);
        assertEquals(900000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.LEASE);
        params.setSmartAccount(false);
        params.setSmartAsset(true);
        assertEquals(100000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.LEASE);
        params.setSmartAccount(true);
        params.setSmartAsset(true);
        assertEquals(500000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.LEASE_CANCEL);
        params.setSmartAccount(false);
        params.setSmartAsset(true);
        assertEquals(100000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.LEASE_CANCEL);
        params.setSmartAccount(true);
        params.setSmartAsset(true);
        assertEquals(500000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.CREATE_ALIAS);
        params.setSmartAccount(false);
        params.setSmartAsset(true);
        assertEquals(100000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.CREATE_ALIAS);
        params.setSmartAccount(true);
        params.setSmartAsset(true);
        assertEquals(500000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.MASS_TRANSFER);
        params.setSmartAccount(false);
        params.setSmartAsset(false);
        params.setTransfersCount(1);
        assertEquals(200000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.MASS_TRANSFER);
        params.setSmartAccount(false);
        params.setSmartAsset(true);
        params.setTransfersCount(1);
        assertEquals(600000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.MASS_TRANSFER);
        params.setSmartAccount(true);
        params.setSmartAsset(true);
        params.setTransfersCount(1);
        assertEquals(1000000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.MASS_TRANSFER);
        params.setSmartAccount(false);
        params.setSmartAsset(false);
        params.setTransfersCount(2);
        assertEquals(200000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.MASS_TRANSFER);
        params.setSmartAccount(false);
        params.setSmartAsset(true);
        params.setTransfersCount(2);
        assertEquals(600000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.MASS_TRANSFER);
        params.setSmartAccount(true);
        params.setSmartAsset(true);
        params.setTransfersCount(2);
        assertEquals(1000000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.MASS_TRANSFER);
        params.setSmartAccount(false);
        params.setSmartAsset(false);
        params.setTransfersCount(3);
        assertEquals(300000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.MASS_TRANSFER);
        params.setSmartAccount(false);
        params.setSmartAsset(true);
        params.setTransfersCount(3);
        assertEquals(700000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.MASS_TRANSFER);
        params.setSmartAccount(true);
        params.setSmartAsset(true);
        params.setTransfersCount(3);
        assertEquals(1100000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.DATA);
        params.setSmartAccount(false);
        params.setSmartAsset(true);
        params.setBytesCount(1025);
        assertEquals(200000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.DATA);
        params.setSmartAccount(true);
        params.setSmartAsset(true);
        params.setBytesCount(2049);
        assertEquals(700000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.ADDRESS_SCRIPT);
        params.setSmartAccount(false);
        params.setSmartAsset(true);
        assertEquals(1000000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.ADDRESS_SCRIPT);
        params.setSmartAccount(true);
        params.setSmartAsset(true);
        assertEquals(1400000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.SPONSORSHIP);
        params.setSmartAccount(false);
        params.setSmartAsset(true);
        assertEquals(100000000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.SPONSORSHIP);
        params.setSmartAccount(true);
        params.setSmartAsset(true);
        assertEquals(100400000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.ASSET_SCRIPT);
        params.setSmartAccount(false);
        params.setSmartAsset(true);
        assertEquals(100000000L, TransactionUtil.Companion.countCommission(commission, params));

        params = new GlobalTransactionCommissionResponse.ParamsResponse();
        params.setTransactionType(TransactionResponse.ASSET_SCRIPT);
        params.setSmartAccount(true);
        params.setSmartAsset(true);
        assertEquals(100400000L, TransactionUtil.Companion.countCommission(commission, params));
    }
}
