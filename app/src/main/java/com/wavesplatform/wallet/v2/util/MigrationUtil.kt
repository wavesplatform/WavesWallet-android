package com.wavesplatform.wallet.v2.util

import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.sdk.model.response.MarketResponse
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.model.db.MarketResponseDb
import com.wavesplatform.wallet.v2.data.model.userdb.AddressBookUser
import com.wavesplatform.wallet.v2.data.model.userdb.AssetBalanceStore
import io.realm.DynamicRealm
import io.realm.Realm
import io.realm.RealmConfiguration
import java.io.File
import javax.inject.Inject

class MigrationUtil @Inject constructor() {

    companion object {

        private const val KEY_AB_NAMES = "address_book_names"
        private const val KEY_AB_ADDRESSES = "address_book_addresses"

        @JvmStatic
        fun checkOldAddressBook(prefs: PrefsUtil, guid: String) {
            if (prefs.has(guid + KEY_AB_NAMES) &&
                    prefs.has(guid + KEY_AB_ADDRESSES)) {
                val names = prefs.getGlobalValueList(
                        guid + KEY_AB_NAMES)
                val addresses = prefs.getGlobalValueList(
                        guid + KEY_AB_ADDRESSES)
                if (names.isNotEmpty() && addresses.isNotEmpty()
                        && names.size == addresses.size) {
                    val addressBookUsers = mutableListOf<AddressBookUser>()
                    for (i in 0 until names.size) {
                        val addressBookUser = AddressBookUser(addresses[i], names[i])
                        addressBookUsers.add(addressBookUser)
                    }
                    addressBookUsers.saveAll()
                    prefs.removeGlobalValue(guid + KEY_AB_NAMES)
                    prefs.removeGlobalValue(guid + KEY_AB_ADDRESSES)
                }
            } else {
                prefs.removeGlobalValue(guid + KEY_AB_NAMES)
                prefs.removeGlobalValue(guid + KEY_AB_ADDRESSES)
            }
        }

        @JvmStatic
        fun checkPrevDbAndRename(address: String?, guid: String) {
            val oldDbFile = File(Realm.getDefaultConfiguration()?.realmDirectory,
                    String.format("%s.realm", address))
            val newDbFile = File(Realm.getDefaultConfiguration()?.realmDirectory,
                    String.format("%s.realm", guid))
            if (oldDbFile.exists()) {
                val oldDbLockFile = File(Realm.getDefaultConfiguration()?.realmDirectory,
                        String.format("%s.realm.lock", address))
                val newDbLockFile = File(Realm.getDefaultConfiguration()?.realmDirectory,
                        String.format("%s.realm.lock", guid))
                val oldDbManagementFile = File(Realm.getDefaultConfiguration()?.realmDirectory,
                        String.format("%s.realm.management", address))
                val newDbManagementFile = File(Realm.getDefaultConfiguration()?.realmDirectory,
                        String.format("%s.realm.management", guid))

                oldDbFile.renameTo(newDbFile)
                oldDbLockFile.renameTo(newDbLockFile)
                oldDbManagementFile.renameTo(newDbManagementFile)
            }
        }

        @JvmStatic
        fun copyPrefDataFromDb(guid: String) {
            val initConfig = RealmConfiguration.Builder()
                    .name(String.format("%s.realm", guid))
                    .build()
            if (Realm.getGlobalInstanceCount(initConfig) < 3) {
                val tempRealm = DynamicRealm.getInstance(initConfig)
                if (tempRealm!!.version != -1L && tempRealm.version < 3L) {
                    val addressBookUsersDb = tempRealm.where("AddressBookUser").findAll()
                    val addressBookUsers = mutableListOf<AddressBookUser>()
                    for (item in addressBookUsersDb) {
                        val addressBookUser = AddressBookUser(
                                item.getString("address"),
                                item.getString("name"))
                        addressBookUsers.add(addressBookUser)
                    }
                    addressBookUsers.saveAll()

                    val assetBalancesStore = mutableListOf<AssetBalanceStore>()
                    val assetBalancesDb = tempRealm.where("AssetBalance").findAll()
                    for (item in assetBalancesDb) {
                        val assetId = item.getString("assetId")
                        assetBalancesStore.add(AssetBalanceStore(
                                assetId = assetId,
                                isHidden = item.getBoolean("isHidden"),
                                position = item.getInt("position"),
                                isFavorite = item.getBoolean("isFavorite")))
                    }
                    assetBalancesStore.saveAll()

                    val newMarketResponses = mutableListOf<MarketResponseDb>()
                    val marketResponses = tempRealm.where("MarketResponse").findAll()
                    for (item in marketResponses) {
                        newMarketResponses.add(MarketResponseDb(
                                id = item.getString("id"),
                                amountAsset = item.getString("amountAsset"),
                                amountAssetName = item.getString("amountAssetName"),
                                amountAssetShortName = item.getString("amountAssetShortName"),
                                amountAssetLongName = item.getString("amountAssetLongName"),
                                amountAssetDecimals = item.getInt("amountAssetDecimals"),
                                // amountAssetInfo = item.getObject("amountAssetInfo"),
                                priceAsset = item.getString("priceAsset"),
                                priceAssetName = item.getString("priceAssetName"),
                                priceAssetShortName = item.getString("priceAssetShortName"),
                                priceAssetLongName = item.getString("priceAssetLongName"),
                                // priceAssetInfo = item.getObject("priceAssetInfo"),
                                priceAssetDecimals = item.getInt("priceAssetDecimals"),
                                created = item.getLong("created"),
                                checked = item.getBoolean("checked"),
                                popular = item.getBoolean("popular"),
                                position = item.getInt("position"),
                                currentTimeFrame = item.getInt("currentTimeFrame")))
                    }
                    newMarketResponses.saveAll()

                    tempRealm.close()
                } else {
                    tempRealm.close()
                }
            }
        }
    }
}