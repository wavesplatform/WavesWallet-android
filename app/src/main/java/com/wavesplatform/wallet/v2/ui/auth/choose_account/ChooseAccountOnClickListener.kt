package com.wavesplatform.wallet.v2.ui.auth.choose_account

import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser

interface ChooseAccountOnClickListener {
    fun onEditClicked(position: Int)
    fun onDeleteClicked()
    fun onItemClicked(item: AddressBookUser)
}
