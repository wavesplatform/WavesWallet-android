package com.wavesplatform.wallet.v2.util

import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalanceStore
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser
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
                    val addressBookUsers = prefs.allAddressBookUsers
                    for (i in 0 until names.size) {
                        val addressBookUser = AddressBookUser(addresses[i], names[i])
                        addressBookUsers.add(addressBookUser)
                    }
                    prefs.setAddressBookUsers(addressBookUsers)
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
        fun copyPrefDataFromDb(prefsUtil: PrefsUtil, guid: String) {
            val initConfig = RealmConfiguration.Builder()
                    .name(String.format("%s.realm", guid))
                    .build()
            val tempRealm = DynamicRealm.getInstance(initConfig)
            if (tempRealm!!.version < 3) {
                val addressBookUsersDb = tempRealm.where("AddressBookUser").findAll()
                val addressBookUsers = prefsUtil.allAddressBookUsers
                for (item in addressBookUsersDb) {
                    val addressBookUser = AddressBookUser(
                            item.getString("address"),
                            item.getString("name"))
                    addressBookUsers.add(addressBookUser)
                }
                prefsUtil.setAddressBookUsers(addressBookUsers)

                val assetBalances = hashMapOf<String, AssetBalanceStore>()
                val assetBalancesDb = tempRealm.where("AssetBalance").findAll()
                for (item in assetBalancesDb) {
                    val assetId = item.getString("assetId")
                    assetBalances[assetId] =
                            AssetBalanceStore(
                                    assetId = assetId,
                                    isHidden = item.getBoolean("isHidden"),
                                    position = item.getInt("position"),
                                    isFavorite = item.getBoolean("isFavorite"))
                }
                prefsUtil.saveAssetBalances(assetBalances)

                tempRealm.close()
                App.getAccessManager().deleteRealm(guid)
            } else {
                tempRealm.close()
            }
        }
    }
}