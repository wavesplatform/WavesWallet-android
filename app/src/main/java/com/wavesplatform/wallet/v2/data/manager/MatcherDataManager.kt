package com.wavesplatform.wallet.v2.data.manager

import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v1.crypto.Base58
import com.wavesplatform.wallet.v1.crypto.CryptoProvider
import com.wavesplatform.wallet.v2.data.manager.base.BaseDataManager
import com.wavesplatform.wallet.v2.util.notNull
import io.reactivex.Observable
import pers.victor.ext.currentTimeMillis
import javax.inject.Inject

class MatcherDataManager @Inject constructor() : BaseDataManager() {

    fun loadReservedBalances(): Observable<Map<String, Long>> {
        val timestamp = currentTimeMillis
        var signature = ""
        App.getAccessManager().getWallet()?.privateKey.notNull { privateKey ->
            val bytes = Bytes.concat(Base58.decode(getPublicKeyStr()),
                    Longs.toByteArray(timestamp))
            signature = Base58.encode(CryptoProvider.sign(privateKey, bytes))
        }
        return matcherService.loadReservedBalances(getPublicKeyStr(), timestamp, signature)
    }

}
