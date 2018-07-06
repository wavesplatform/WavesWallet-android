package com.wavesplatform.wallet.v2.ui.home.profile

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v4.view.ViewPager
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.AppCompatTextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.home.history.TestObject
import com.wavesplatform.wallet.v2.ui.home.history.details.AddressModel
import com.wavesplatform.wallet.v2.ui.home.history.details.HistoryDetailsAdapter
import com.wavesplatform.wallet.v2.ui.home.history.details.HistoryTypeEnum
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressTestObject
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.add.AddAddressActivity
import com.wavesplatform.wallet.v2.ui.home.profile.addresses.create.CreateAliasActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.addresses_and_keys_bottom_sheet_dialog_layout.view.*
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.visiable
import java.util.*
import kotlin.collections.ArrayList


class AddressesAndKeysBottomSheetFragment : BottomSheetDialogFragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var rootView = inflater.inflate(R.layout.addresses_and_keys_bottom_sheet_dialog_layout, container, false)

        rootView.button_create_alias.click {
            launchActivity<CreateAliasActivity> {  }
        }

        return rootView
    }
}