package com.wavesplatform.wallet.api;

import android.support.annotation.VisibleForTesting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wavesplatform.wallet.crypto.PublicKeyAccount;
import com.wavesplatform.wallet.payload.AssetBalance;
import com.wavesplatform.wallet.payload.AssetBalances;
import com.wavesplatform.wallet.payload.ExchangeTransaction;
import com.wavesplatform.wallet.payload.IssueTransaction;
import com.wavesplatform.wallet.payload.MassTransferTransaction;
import com.wavesplatform.wallet.payload.PaymentTransaction;
import com.wavesplatform.wallet.payload.ReissueTransaction;
import com.wavesplatform.wallet.payload.Transaction;
import com.wavesplatform.wallet.payload.TransactionsInfo;
import com.wavesplatform.wallet.payload.TransferTransaction;
import com.wavesplatform.wallet.request.IssueTransactionRequest;
import com.wavesplatform.wallet.request.ReissueTransactionRequest;
import com.wavesplatform.wallet.request.TransferTransactionRequest;
import com.wavesplatform.wallet.ui.auth.EnvironmentManager;
import com.wavesplatform.wallet.util.PrefsUtil;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import io.reactivex.Completable;
import io.reactivex.Observable;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class NodeManager {

    private static NodeManager instance;
    private final NodeApi service;
    private PrefsUtil prefsUtil;
    public AssetBalances assetBalances = new AssetBalances();
    public AssetBalances deletedAssetBalances = new AssetBalances();
    public List<Transaction> transactions = new ArrayList<>();
    public List<Transaction> pendingTransactions = new ArrayList<>();
    public List<AssetBalance> pendingAssets = new ArrayList<>();
    private final PublicKeyAccount publicKeyAccount;

    public static NodeManager get() {
        return instance;
    }

    public static NodeManager createInstance(String pubKey) {
        try {
            instance = new NodeManager(pubKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    @VisibleForTesting
    public static NodeManager createTestInstance(NodeApi service, String pubKey) {
        try {
            instance = new NodeManager(service, pubKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    private NodeManager(String pubKey) throws PublicKeyAccount.InvalidPublicKey {
        service = getService();
        this.publicKeyAccount = new PublicKeyAccount(pubKey);
    }

    // Added for testing purposes only
    private NodeManager(NodeApi service, String pubKey) throws PublicKeyAccount.InvalidPublicKey {
        this.service = service;
        this.publicKeyAccount = new PublicKeyAccount(pubKey);
    }

    private NodeApi getService() {
        final RuntimeTypeAdapterFactory<Transaction> typeFactory = RuntimeTypeAdapterFactory
                .of(Transaction.class, "type")
                .registerSubtype(PaymentTransaction.class, "2")
                .registerSubtype(IssueTransaction.class, "3")
                .registerSubtype(TransferTransaction.class, "4")
                .registerSubtype(ReissueTransaction.class, "5")
                .registerSubtype(ExchangeTransaction.class, "7")
                .registerSubtype(MassTransferTransaction.class, "11")
                .registerDefaultSubtype(Transaction.class, "0");

        Gson gson = new GsonBuilder().registerTypeAdapterFactory(typeFactory).create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(EnvironmentManager.get().current().getNodeUrl())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        return retrofit.create(NodeApi.class);
    }

    public AssetBalance wavesAsset = new AssetBalance() {{
        assetId = null;
        quantity = 100000000L * 100000000L;
        issueTransaction = new IssueTransaction();
        issueTransaction.decimals = 8;
        issueTransaction.quantity = quantity;
        issueTransaction.name = "WAVES";
    }};

    private List<Transaction> filterOwnTransactions(List<Transaction> txs) {
        List<Transaction> own = new ArrayList<>();
        for (Transaction tx : txs) {
            if (tx.isOwn()) {
                tx.isPending = true;
                own.add(tx);
            }
        }

        return own;
    }

    private List<Transaction> filterSpamTransactions(List<Transaction> txs, Set<String> spam) {
        if (!prefsUtil.getValue(PrefsUtil.KEY_DISABLE_SPAM_FILTER, false)) {
            List<Transaction> filtered = new ArrayList<>();
            for (Transaction tx : txs) {
                if (!spam.contains(tx.getAssetId())) {
                    filtered.add(tx);
                }
            }
            return filtered;
        } else {
            return txs;
        }
    }

    public Completable loadBalancesAndTransactions() {
        return Observable.zip(service.wavesBalance(getAddress()),
                service.assetsBalance(getAddress()),
                service.transactionList(getAddress(), 100).map(r -> r.get(0)),
                service.unconfirmedTransactions(),
                SpamManager.get().getSpamAssets(),
                (bal, abs, txs, pending, spam) -> {
                    wavesAsset.balance = bal.balance;
                    List<AssetBalance> filteredBalances = new ArrayList<>();
                    if (!prefsUtil.getValue(PrefsUtil.KEY_DISABLE_SPAM_FILTER, false)){
                        for (AssetBalance ab : abs.balances) {
                            if (!spam.contains(ab.assetId))
                                filteredBalances.add(ab);
                        }
                    } else {
                        filteredBalances = abs.balances;
                    }
                    abs.balances = filteredBalances;
                    this.assetBalances = abs;
                    Collections.sort(this.assetBalances.balances, (o1, o2) -> o1.assetId.compareTo(o2.assetId));
                    this.assetBalances.balances.add(0, wavesAsset);
                    this.pendingTransactions = filterOwnTransactions(pending);
                    this.transactions = filterSpamTransactions(txs, spam);

                    updatePendingTxs();
                    updatePendingBalances();

                    return Pair.of(abs, txs);
                }).ignoreElements().andThen(loadMissingAssets());
    }

    private Completable loadMissingAssets() {
        Set<String> allAssets = this.assetBalances.getAllAssets();
        Set<String> missingAssets = new HashSet<>();
        for (Transaction tx : this.transactions) {
            if (!allAssets.contains(tx.getAssetId())) {
                missingAssets.add(tx.getAssetId());
            }
        }

        for (Transaction tx : this.pendingTransactions) {
            if (!allAssets.contains(tx.getAssetId())) {
                missingAssets.add(tx.getAssetId());
            }
        }

        return Observable.fromIterable(missingAssets)
                .flatMap(service::getTransactionsInfo)
                .toList().doOnSuccess(tis -> {
                    this.deletedAssetBalances = new AssetBalances();
                    for (TransactionsInfo ti : tis) {
                        this.deletedAssetBalances.balances.add(AssetBalance.fromTransactionInfo(ti));
                    }
                }).toCompletable();
    }

    private void updatePendingTxs() {
        for (Transaction tx : this.transactions) {
            Iterator<Transaction> i = pendingTransactions.iterator();
            while (i.hasNext()) {
                if (tx.id.equals(i.next().id)) {
                    i.remove();
                }
            }
        }
    }

    private void updatePendingBalances() {
        for (AssetBalance ab : this.assetBalances.balances) {
            Iterator<AssetBalance> i = pendingAssets.iterator();
            while (i.hasNext()) {
                if (ab.isAssetId(i.next().assetId)) {
                    i.remove();
                }
            }
        }
    }

    public Observable<TransactionsInfo> getTransactionsInfo(final String asset) {
        return service.getTransactionsInfo(asset);
    }

    public List<AssetBalance> getAllAssets() {
        List<AssetBalance> all = new ArrayList<>();
        all.addAll(pendingAssets);
        all.addAll(assetBalances.balances);
        return all;
    }

    public String getAssetName(String assetId) {
        String existing = assetBalances.getAssetName(assetId);
        return existing != null ? existing : deletedAssetBalances.getAssetName(assetId);
    }

    public String getPublicKeyStr() {
        return publicKeyAccount.getPublicKeyStr();
    }

    public Observable<TransferTransactionRequest> broadcastTransfer(TransferTransactionRequest tx) {
        return service.broadcastTransfer(tx);
    }

    public Observable<IssueTransactionRequest> broadcastIssue(IssueTransactionRequest tx) {
        return service.broadcastIssue(tx);
    }

    public Observable<ReissueTransactionRequest> broadcastReissue(ReissueTransactionRequest tx) {
        return service.broadcastReissue(tx);
    }

    public void updateBalance(TransferTransactionRequest tx) {
        for (AssetBalance ab : assetBalances.balances) {
            if (ab.isAssetId(tx.assetId)) ab.balance -= tx.amount;
            if (ab.assetId == null) ab.balance -= tx.fee;
        }
    }

    public AssetBalance getAssetBalance(String assetId) {
        for (AssetBalance ab : assetBalances.balances) {
            if (ab.isAssetId(assetId)) return ab;
        }
        for (AssetBalance ab : deletedAssetBalances.balances) {
            if (ab.isAssetId(assetId)) return ab;
        }
        return wavesAsset;
    }

    public List<Transaction> getAssetTransactions(AssetBalance ab) {
        List<Transaction> txs = new ArrayList<>();
        for (Transaction tx : transactions) {
            if (tx.isForAsset(ab.assetId)) txs.add(tx);
        }
        return txs;
    }

    public List<Transaction> getPendingAssetTransactions(AssetBalance ab) {
        List<Transaction> txs = new ArrayList<>();
        for (Transaction tx : pendingTransactions) {
            if (tx.isForAsset(ab.assetId)) txs.add(tx);
        }
        return txs;
    }

    public void addPendingTransaction(Transaction tx) {
        pendingTransactions.add(0, tx);
    }

    public void addPendingAsset(AssetBalance ab) {
        pendingAssets.add(0, ab);
    }

    public long getWavesBalance() {
        return getAssetBalance(null).balance;
    }

    public void setPrefsUtil(PrefsUtil prefsUtil) {
        this.prefsUtil = prefsUtil;
    }

    public String getAddress() {
        return publicKeyAccount.getAddress();
    }
}
