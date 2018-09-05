package com.wavesplatform.wallet.v2.data.manager

import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.wallet.v2.data.model.remote.response.Alias
import io.reactivex.Observable
import javax.inject.Inject

class ApiDataManager @Inject constructor() : DataManager() {

    fun loadAliases(): Observable<List<Alias>> {
        return apiService.aliases(getAddress())
                .map {
                    val aliases = it.data.mapTo(ArrayList()) {
                        it.alias
                    }
                    aliases.saveAll()
                    return@map aliases
                }
    }

    fun loadAlias(alias: String): Observable<Any> {
        return apiService.alias(alias)
    }
}
