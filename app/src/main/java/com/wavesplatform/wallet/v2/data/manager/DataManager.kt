package com.wavesplatform.wallet.v2.data.manager

import android.util.Log
import com.wavesplatform.wallet.v1.payload.*
import com.wavesplatform.wallet.v1.request.ReissueTransactionRequest
import com.wavesplatform.wallet.v2.data.helpers.PublicKeyAccountHelper
import com.wavesplatform.wallet.v2.data.local.PreferencesHelper
import com.wavesplatform.wallet.v2.data.remote.AppService
import io.reactivex.Observable
import io.reactivex.functions.Function4
import java.util.*
import javax.inject.Inject

class DataManager @Inject constructor(val mAppService: AppService, val mPreferencesHelper: PreferencesHelper, val publicKeyAccountHelper: PublicKeyAccountHelper) {

    var assetBalances = AssetBalances()
    var transactions: List<Transaction> = ArrayList()
    var pendingTransactions: List<Transaction> = ArrayList()
    var pendingAssets: MutableList<AssetBalance> = ArrayList()

    fun loadBalancesAndTransactions(): Observable<Any> {
        Log.d("TEST_BALANCE", ("pendingTransactions "+publicKeyAccountHelper.publicKeyAccount==null).toString())
        Log.d("TEST_BALANCE", ("pendingTransactions adres"+publicKeyAccountHelper.publicKeyAccount?.address==null).toString())
        return Observable.zip(mAppService.wavesBalance(publicKeyAccountHelper.publicKeyAccount?.address!!), mAppService.assetsBalance(publicKeyAccountHelper.publicKeyAccount?.address!!), mAppService.transactionList(publicKeyAccountHelper.publicKeyAccount?.address!!, 50).map { t -> t[0] }, unconfirmedTransactionsWithOwnFilter(), Function4<WavesBalance, AssetBalances, List<Transaction>, List<Transaction>, Any> { bal, abs, txs, pending ->
            wavesAsset.balance = bal.balance
            assetBalances = abs
            assetBalances.balances.sortWith(Comparator { o1, o2 -> o1.assetId.compareTo(o2.assetId) })
            assetBalances.balances.add(0, wavesAsset)
            pendingTransactions = pending
            transactions = txs

            updatePendingTxs()
            updatePendingBalances()

            Any()
        })
    }

    private fun unconfirmedTransactionsWithOwnFilter(): Observable<List<Transaction>> {
        return mAppService.unconfirmedTransactions()
                .flatMap {
                    Observable.fromIterable(it)
                            .map {
                                if (it.isOwn) it.isPending = true
                                it
                            }
                            .toList()
                            .toObservable()
                }
    }

    fun broadcastIssue(tx: ReissueTransactionRequest): Observable<ReissueTransactionRequest> {
        return mAppService.broadcastReissue(tx)
    }

    fun getTransactionsInfo(asset: String): Observable<TransactionsInfo> {
        return mAppService.getTransactionsInfo(asset)
    }

    var wavesAsset: AssetBalance = object : AssetBalance() {
        init {
            assetId = null
            quantity = 100000000L * 100000000L
            issueTransaction = IssueTransaction()
            issueTransaction.decimals = 8
            issueTransaction.quantity = quantity
            issueTransaction.name = "WAVES"
        }
    }

    private fun updatePendingTxs() {
        for (tx in this.transactions) {
            val i = pendingTransactions.iterator() as MutableIterator<Transaction>
            while (i.hasNext()) {
                if (tx.id == i.next().id) {
                    i.remove()
                }
            }
        }
    }

    private fun updatePendingBalances() {
        for (ab in this.assetBalances.balances) {
            val i = pendingAssets.iterator()
            while (i.hasNext()) {
                if (ab.isAssetId(i.next().assetId)) {
                    i.remove()
                }
            }
        }
    }

}
