package com.wavesplatform.wallet.v1.api;

import com.google.gson.GsonBuilder;
import com.wavesplatform.wallet.v1.crypto.Base58;
import com.wavesplatform.wallet.v1.crypto.CryptoProvider;
import com.wavesplatform.wallet.v1.crypto.PrivateKeyAccount;
import com.wavesplatform.wallet.v1.data.auth.WavesWallet;
import com.wavesplatform.wallet.v1.payload.Transaction;
import com.wavesplatform.wallet.v1.request.IssueTransactionRequest;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.wavesplatform.wallet.v1.request.TransferTransactionRequest.SignatureLength;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NodeManagerTest {

    @Before
    public void setUp() {
    }

    @Test
    public void lastTransactions() throws Exception {
        NodeManager.get().loadBalancesAndTransactions().subscribe(() -> {
            List<Transaction> txs = NodeManager.get().transactions;
            System.out.println(txs);
        });
    }

    @Test
    public void signature() throws Exception {
        byte[] seed = "seed".getBytes();
        PrivateKeyAccount acc = new PrivateKeyAccount(seed);
        byte[] msg = "message".getBytes();
        String sigStr = "425CPC8fBPyNpcdfykNDTx9fk1B3vzVoaDYEVuzN6sTeG1LJgSu1EQFRAZ2HukTjwm1FChZcAF6CccKZgWa6R78m";

        byte[] genSig = CryptoProvider.get().
                calculateSignature(CryptoProvider.get().getRandom(SignatureLength), acc.getPrivateKey(), msg);
        assertTrue(CryptoProvider.get().verifySignature(acc.getPublicKey(), msg, Base58.decode(sigStr)));

    }

    @Test
    public void hashes() throws Exception {
        PrivateKeyAccount pk = new PrivateKeyAccount(Base58.decode("KFWy4MpQgRcaEAdjwr9KenkSWCKzEUCzK9SXWCFGD4KWYsXiKBhmjW2Dma996W5XV5esBJTELoTjF88C6QBNVRKenMjCzYWinbvWTfcbUfB5YjjxVVhrt9FUKRM"));
        assertEquals("EtujXZmPthSG8YUwvF88seDcw4WH6ZPN3tRJZ6w1mev1", pk.getPublicKeyStr());
    }

    @Test
    public void empty() throws Exception {
        NodeManager nm = NodeManager.createInstance("");
        System.out.println(nm.getAddress());
    }

    @Test
    public void broadcast() throws Exception {
        /*WavesWallet wavesWallet = new WavesWallet(
                "trial appear battle what fiber hello weasel grunt spare heavy produce beach one friend sad".getBytes());
        NodeManager.createInstance(wavesWallet.getPublicKeyStr());
        TransferTransactionRequest tx = new TransferTransactionRequest(
                null,
                NodeManager.get().getPublicKeyStr(),
                "3NBVqYXrapgJP9atQccdBPAgJPwHDKkh6A8",
                100000000L,
                1486838172523L,
                100000L,
                "attachment");
        tx.sign("".getBytes());
        System.out.println(new GsonBuilder()
                .setPrettyPrinting()
                .create().toJson(tx));
        NodeManager.get().broadcastTransfer(tx).subscribe((resp) -> {
            System.out.println(resp);
        }, err -> {
            System.out.println(err);
        });*/
    }

    @Test
    public void broadcastIssue() throws Exception {
        WavesWallet wavesWallet = new WavesWallet(
                "trial appear battle what fiber hello weasel grunt spare heavy produce beach one friend sad".getBytes());
        NodeManager.createInstance(wavesWallet.getPublicKeyStr());
        IssueTransactionRequest tx = new IssueTransactionRequest(
                NodeManager.get().getPublicKeyStr(),
                "gnt2",
                "",
                6000000L,
                (byte)6,
                true,
                System.currentTimeMillis());

        tx.sign(wavesWallet.getPrivateKey());
        System.out.println(new GsonBuilder()
                .setPrettyPrinting()
                .create().toJson(tx));
        NodeManager.get().broadcastIssue(tx).subscribe((resp) -> {
            System.out.println(resp);
        }, err -> {
            System.out.println(err);
        });
    }

}
