package com.wavesplatform.wallet.v2.ui.home.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseBottomSheetDialogFragment
import com.wavesplatform.wallet.v2.ui.home.profile.addresses.create.CreateAliasActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.addresses_and_keys_bottom_sheet_dialog_layout.view.*
import pers.victor.ext.click


class AddressesAndKeysBottomSheetFragment : BaseBottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var rootView = inflater.inflate(R.layout.addresses_and_keys_bottom_sheet_dialog_layout, container, false)

        rootView.button_create_alias.click {
            launchActivity<CreateAliasActivity> {  }
        }

        return rootView
    }
}