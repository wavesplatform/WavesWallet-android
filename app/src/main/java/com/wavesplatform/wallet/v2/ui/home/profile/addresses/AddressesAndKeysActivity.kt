package com.wavesplatform.wallet.v2.ui.home.profile.addresses

import android.content.Intent
import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.jakewharton.rxbinding2.view.RxView
import com.vicpin.krealmextensions.queryAllAsync
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.data.auth.WavesWallet
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.Alias
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.EnterPassCodeActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.profile.addresses.alias.AddressesAndKeysBottomSheetFragment
import com.wavesplatform.wallet.v2.util.copyToClipboard
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.notNull
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_profile_addresses_and_keys.*
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.visiable
import pyxis.uzuki.live.richutilskt.utils.runAsync
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AddressesAndKeysActivity : BaseActivity(), AddressesAndKeysView {

    @Inject
    @InjectPresenter
    lateinit var andKeysPresenter: AddressesAndKeysPresenter

    @ProvidePresenter
    fun providePresenter(): AddressesAndKeysPresenter = andKeysPresenter

    override fun configLayoutRes(): Int = R.layout.activity_profile_addresses_and_keys

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, true, getString(R.string.addresses_and_keys_toolbar_title), R.drawable.ic_toolbar_back_black)

        text_address.text = App.getAccessManager().getWallet()?.address
        text_public_key.text = App.getAccessManager().getWallet()?.publicKeyStr

        runAsync {
            queryAllAsync<Alias> { aliases ->
                val ownAliases = aliases.filter { it.own }
                text_alias_count.text = String.format(getString(R.string.alias_dialog_you_have), ownAliases.size)
                relative_alias.click {
                    val bottomSheetFragment = AddressesAndKeysBottomSheetFragment()
                    if (ownAliases.isEmpty()) {
                        bottomSheetFragment.type = AddressesAndKeysBottomSheetFragment.TYPE_EMPTY
                    } else {
                        bottomSheetFragment.type = AddressesAndKeysBottomSheetFragment.TYPE_CONTENT
                    }
                    bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
                }
            }
        }

        eventSubscriptions.add(RxView.clicks(image_address_copy)
                .throttleFirst(1500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    image_address_copy.copyToClipboard(text_address.text.toString())
                })

        eventSubscriptions.add(RxView.clicks(image_public_key_copy)
                .throttleFirst(1500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    image_public_key_copy.copyToClipboard(text_public_key.text.toString())
                })

        eventSubscriptions.add(RxView.clicks(image_private_key_copy)
                .throttleFirst(1500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    image_private_key_copy.copyToClipboard(text_private_key.text.toString())
                })

        button_show.click {
            launchActivity<EnterPassCodeActivity>(
                    requestCode = EnterPassCodeActivity.REQUEST_ENTER_PASS_CODE) { }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            EnterPassCodeActivity.REQUEST_ENTER_PASS_CODE -> {
                if (resultCode == Constants.RESULT_OK) {
                    button_show.gone()
                    val password = data!!.extras.getString(NewAccountActivity.KEY_INTENT_PASSWORD)
                    val wallet = WavesWallet(App.getAccessManager()
                            .getCurrentWavesWalletEncryptedData(), password)
                    text_private_key.text = (wallet.privateKeyStr)
                    relative_private_key_block.visiable()
                }
            }
        }
    }
}
