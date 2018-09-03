package com.wavesplatform.wallet.v2.ui.home.profile

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.*
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.mtramin.rxfingerprint.RxFingerprint
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.data.access.AccessState
import com.wavesplatform.wallet.v1.data.auth.WavesWallet
import com.wavesplatform.wallet.v1.ui.home.MainActivity
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.local.Language
import com.wavesplatform.wallet.v2.ui.auth.fingerprint.UseFingerprintActivity
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.create.CreatePasscodeActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.EnterPasscodeActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity
import com.wavesplatform.wallet.v2.ui.home.profile.addresses.AddressesAndKeysActivity
import com.wavesplatform.wallet.v2.ui.home.profile.backup.BackupPhraseActivity
import com.wavesplatform.wallet.v2.ui.home.profile.change_password.ChangePasswordActivity
import com.wavesplatform.wallet.v2.ui.home.profile.network.NetworkActivity
import com.wavesplatform.wallet.v2.ui.language.change_welcome.ChangeLanguageActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.makeStyled
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

        const val KEY_INTENT_SET_BACKUP = "intent_set_backup"
        const val REQUEST_ENTER_PASSCODE_FOR_CHANGE = 5551
        const val REQUEST_ENTER_PASSCODE_FOR_FINGERPRINT = 5552
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
            launchActivity<BackupPhraseActivity> {
                putExtra(KEY_INTENT_SET_BACKUP, true)
            }
        }

        card_language.click {
            launchActivity<ChangeLanguageActivity> {  }
        }
        card_change_password.click {
            launchActivity<ChangePasswordActivity> {  }
        }

        card_change_passcode.click {
            launchActivity<EnterPasscodeActivity>(requestCode = REQUEST_ENTER_PASSCODE_FOR_CHANGE)
        }

        card_network.click {
            launchActivity<NetworkActivity> {  }
        }
        button_delete_account.click {
            val alertDialog = AlertDialog.Builder(baseActivity).create()
            alertDialog.setTitle(getString(R.string.profile_general_delete_account_dialog_title))
            alertDialog.setMessage(getString(R.string.profile_general_delete_account_dialog_description))
            if (AccessState.getInstance().isCurrentAccountBackupSkipped) {
                alertDialog.setView(LayoutInflater.from(baseActivity)
                        .inflate(R.layout.delete_account_warning_layout, null))
            }
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.profile_general_delete_account_dialog_delete)) { dialog, _ ->
                dialog.dismiss()
                AccessState.getInstance().deleteCurrentWavesWallet()
                toast(getString(R.string.profile_general_delete_account_dialog_deleted))
                launchActivity<MainActivity> {  }
            }
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.profile_general_delete_account_dialog_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            alertDialog.show()
            alertDialog.makeStyled()
        }

        if (RxFingerprint.isAvailable(context!!)) {
            fingerprint_switch.isChecked = AccessState.getInstance().isUseFingerPrint
            fingerprint_switch.setOnCheckedChangeListener { _, isChecked->
                launchActivity<EnterPasscodeActivity>(
                        requestCode = REQUEST_ENTER_PASSCODE_FOR_FINGERPRINT)
            }
        } else {
            card_fingerprint.visibility = View.GONE
        }


        if (AccessState.getInstance().isCurrentAccountBackupSkipped) {
            skip_backup_indicator.setBackgroundColor(ContextCompat
                    .getColor(context!!, R.color.error500))
            skip_backup_indicator_image.setImageDrawable(ContextCompat
                    .getDrawable(context!!, R.drawable.ic_info_error_500))
        } else {
            skip_backup_indicator.setBackgroundColor(ContextCompat
                    .getColor(context!!, R.color.success400))
            skip_backup_indicator_image.setImageDrawable(ContextCompat
                    .getDrawable(context!!, R.drawable.ic_check_18_success_400))
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ENTER_PASSCODE_FOR_FINGERPRINT -> {
                if (resultCode == Constants.RESULT_OK) {
                    val passCode = data!!.extras.getString(EnterPasscodeActivity.KEY_PASS_CODE)
                    launchActivity<UseFingerprintActivity>(clear = true) {
                        putExtra(CreatePasscodeActivity.KEY_PASS_CODE, passCode)
                    }
                }
            }
            REQUEST_ENTER_PASSCODE_FOR_CHANGE -> {
                if (resultCode == Constants.RESULT_OK) {

                    val passCode = data!!.extras.getString(EnterPasscodeActivity.KEY_PASS_CODE)
                    val password = data.extras.getString(NewAccountActivity.KEY_INTENT_PASSWORD)

                    launchActivity<CreatePasscodeActivity>(clear = true) {
                        putExtra(CreatePasscodeActivity.KEY_CHANGE_PASS_CODE, true)
                        putExtra(NewAccountActivity.KEY_INTENT_PASSWORD, password)
                        putExtra(EnterPasscodeActivity.KEY_PASS_CODE, passCode)
                    }
                }
            }
        }
    }
}
