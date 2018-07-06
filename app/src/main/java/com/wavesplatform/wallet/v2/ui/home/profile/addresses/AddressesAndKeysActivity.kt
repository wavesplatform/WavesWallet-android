package com.wavesplatform.wallet.v2.ui.home.profile.addresses

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.history.adapter.HistoryItem
import com.wavesplatform.wallet.v2.ui.home.history.details.HistoryDetailsBottomSheetFragment
import com.wavesplatform.wallet.v2.ui.home.profile.AddressesAndKeysBottomSheetFragment
import com.wavesplatform.wallet.v2.ui.home.profile.addresses.adapter.AliasesAdapter
import com.wavesplatform.wallet.v2.ui.home.profile.addresses.create.CreateAliasActivity
import com.wavesplatform.wallet.v2.util.copyToClipboard
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.activity_profile_addresses_and_keys.*
import pers.victor.ext.click
import java.util.*
import javax.inject.Inject

class AddressesAndKeysActivity : BaseActivity(), ProfileAddressesView {

    @Inject
    @InjectPresenter
    lateinit var presenter: ProfileAddressesPresenter

    @ProvidePresenter
    fun providePresenter(): ProfileAddressesPresenter = presenter

    @Inject
    lateinit var adapter: AliasesAdapter

    override fun configLayoutRes(): Int = R.layout.activity_profile_addresses_and_keys

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, getString(R.string.addresses_and_keys_toolbar_title), R.drawable.ic_toolbar_back_black)


        relative_alias.click {
            val bottomSheetFragment = AddressesAndKeysBottomSheetFragment()
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }

        image_address_copy.click{
            text_address.copyToClipboard(it)
        }

        image_public_key_copy.click {
            text_public_key.copyToClipboard(it)
        }
    }

}
