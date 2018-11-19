package com.wavesplatform.wallet.v2.util

import com.vicpin.krealmextensions.save
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser
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
                    for (i in 0 until names.size) {
                        AddressBookUser(addresses[i], names[i]).save()
                    }
                    prefs.removeGlobalValue(guid + KEY_AB_NAMES)
                    prefs.removeGlobalValue(guid + KEY_AB_ADDRESSES)
                }
            } else {
                prefs.removeGlobalValue(guid + KEY_AB_NAMES)
                prefs.removeGlobalValue(guid + KEY_AB_ADDRESSES)
            }
        }
    }
}