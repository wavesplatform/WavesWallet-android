package com.wavesplatform.wallet.v2.util

import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser
import io.realm.Realm
import java.io.File
import javax.inject.Inject

class MigrationUtil @Inject constructor() {


    companion object {

        private const val KEY_AB_NAMES = "address_book_names"
        private const val KEY_AB_ADDRESSES = "address_book_addresses"

        @JvmStatic
        fun checkOldAddressBook(prefs: PrefsUtil, guid: String) {
            if (prefs.has(guid + KEY_AB_NAMES)
                    && prefs.has(guid + KEY_AB_ADDRESSES)) {
                val names = prefs.getGlobalValueList(
                        guid + KEY_AB_NAMES)
                val addresses = prefs.getGlobalValueList(
                        guid + KEY_AB_ADDRESSES)
                if (names.isNotEmpty() && addresses.isNotEmpty()
                        && names.size == addresses.size) {
                    val addressBookUsers = arrayListOf<AddressBookUser>()
                    for (i in 0 until names.size) {
                        addressBookUsers.add(AddressBookUser(addresses[i], names[i]))
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
    }
}