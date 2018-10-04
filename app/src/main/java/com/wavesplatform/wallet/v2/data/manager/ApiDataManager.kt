package com.wavesplatform.wallet.v2.data.manager

import com.vicpin.krealmextensions.queryAllAsSingle
import com.vicpin.krealmextensions.queryFirst
import com.vicpin.krealmextensions.save
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.Alias
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetInfo
import com.wavesplatform.wallet.v2.util.notNull
import io.reactivex.Observable
import javax.inject.Inject

class ApiDataManager @Inject constructor() : DataManager() {

    fun loadAliases(): Observable<List<Alias>> {
        return apiService.aliases(getAddress())
                .map {
                    val aliases = it.data.mapTo(ArrayList()) {
                        it.alias.own = true
                        return@mapTo it.alias
                    }
                    aliases.saveAll()
                    return@map aliases
                }
    }

    fun loadAlias(alias: String): Observable<Alias> {
        val localAlias = queryFirst<Alias> { equalTo("alias", alias) }

        if (localAlias != null) {
            return Observable.just(localAlias)
        } else {
            return apiService.alias(alias)
                    .map {
                        it.alias.own = false
                        it.alias.save()
                        return@map it.alias
                    }
        }
    }

    fun assetsInfoByIds(ids: List<String?>): Observable<List<AssetInfo>> {
        return apiService.assetsInfoByIds(ids)
                .map { it ->
                    val assetsInfo = it.data.mapTo(ArrayList()) { assetInfoData ->
                        val defaultAsset = Constants.defaultAssets.firstOrNull { it.assetId == assetInfoData.assetInfo.id }

                        defaultAsset.notNull { assetBalance ->
                            assetBalance.getName().notNull {
                                assetInfoData.assetInfo.name = it
                            }

                        }

                        return@mapTo assetInfoData.assetInfo
                    }
                    assetsInfo.saveAll()
                    return@map assetsInfo
                }
    }

}
