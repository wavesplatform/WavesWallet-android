package com.wavesplatform.wallet.v2.ui.home.profile

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.Language
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity
import com.wavesplatform.wallet.v2.ui.home.profile.addresses.AddressesAndKeysActivity
import com.wavesplatform.wallet.v2.ui.language.change_welcome.ChangeLanguageActivity
import com.wavesplatform.wallet.v2.ui.home.profile.backup.BackupPhraseActivity
import com.wavesplatform.wallet.v2.ui.home.profile.change_password.ChangePasswordActivity
import com.wavesplatform.wallet.v2.ui.home.profile.network.NetworkActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.fragment_profile.*
import pers.victor.ext.click
import pers.victor.ext.toast
import javax.inject.Inject

class ProfileFragment : BaseFragment(), ProfileView {

    @Inject
    @InjectPresenter
    lateinit var presenter: ProfilePresenter

    @ProvidePresenter
    fun providePresenter(): ProfilePresenter = presenter

    companion object {

        /**
         * @return ProfileFragment instance
         * */
        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }

    override fun configLayoutRes(): Int = R.layout.fragment_profile

    override fun onViewReady(savedInstanceState: Bundle?) {
        card_address_book.click {
            launchActivity<AddressBookActivity> {  }
        }

        card_addresses_and_keys.click {
            launchActivity<AddressesAndKeysActivity> {  }
        }
        card_backup_phrase.click {
            launchActivity<BackupPhraseActivity> {  }
        }

        card_language.click {
            launchActivity<ChangeLanguageActivity> {  }
        }
        card_change_password.click {
            launchActivity<ChangePasswordActivity> {  }
        }
        card_network.click {
            launchActivity<NetworkActivity> {  }
        }
    }

    override fun onResume() {
        super.onResume()
        setCurrentLangFlag()
    }

    private fun setCurrentLangFlag() {
        val languageItemByCode = Language.getLanguageItemByCode(presenter.preferenceHelper.getLanguage())
        image_language_flag.setImageResource(languageItemByCode.language.image)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_profile, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_logout -> {
                toast(item.title)
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
